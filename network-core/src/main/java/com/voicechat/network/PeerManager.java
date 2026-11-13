package com.voicechat.network;

import com.voicechat.common.api.NetworkGateway;
import com.voicechat.common.model.ControlMessage;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/** Manages TCP control connections and a shared local UDP endpoint. */
public final class PeerManager implements NetworkGateway {
    private static final int HANDSHAKE_TIMEOUT_MS = 3000;
    private static final int HEARTBEAT_INTERVAL_MS = 2000;
    private static final int READ_TIMEOUT_MS = 8000;

    private final TcpService tcp = new TcpService();
    private final String localId;
    private final Map<String, Peer> peers = new HashMap<>();
    private final Set<Socket> connections = new HashSet<>();
    private ServerSocket listener;
    private UdpService udp;
    private volatile Consumer<ControlMessage> controlCallback = message -> {};
    private volatile BiConsumer<String, byte[]> voiceCallback = (id, bytes) -> {};

    public PeerManager() {
        this(UUID.randomUUID().toString());
    }

    public PeerManager(String localId) {
        if (Objects.requireNonNull(localId).isBlank()) {
            throw new IllegalArgumentException("Peer ID is required");
        }
        this.localId = localId;
    }

    public String localPeerId() {
        return localId;
    }

    /** Starts accepting peers. Pass zero for either port to let the OS choose. */
    public synchronized int startListening(int tcpPort, int udpPort) throws IOException {
        if (listener != null) {
            throw new IOException("Already listening");
        }
        if (udp != null && udpPort != 0 && udp.localPort() != udpPort) {
            throw new IOException("UDP already bound to port " + udp.localPort());
        }
        ServerSocket server = new ServerSocket(tcpPort);
        UdpService transport;
        try {
            transport = ensureUdp(udpPort);
        } catch (IOException | RuntimeException e) {
            TcpService.close(server);
            throw e;
        }
        listener = server;
        Thread.ofVirtual().name("peer-accept").start(() -> accept(server, transport));
        return server.getLocalPort();
    }

    private void accept(ServerSocket server, UdpService transport) {
        try (server) {
            while (!server.isClosed()) {
                Socket socket = server.accept();
                synchronized (this) {
                    if (listener != server) {
                        TcpService.close(socket);
                        return;
                    }
                    connections.add(socket);
                }
                Thread.ofVirtual().name("peer-handshake").start(() -> {
                    try {
                        establish(socket, transport, false);
                    } catch (IOException e) {
                        if (!server.isClosed()) {
                            System.err.println("TCP handshake: " + e.getMessage());
                        }
                    }
                });
            }
        } catch (IOException e) {
            synchronized (this) {
                if (listener == server) {
                    listener = null;
                    System.err.println("TCP accept: " + e.getMessage());
                }
            }
        }
    }

    @Override
    public void connect(String host, int tcpPort) throws IOException {
        UdpService transport;
        synchronized (this) {
            transport = ensureUdp(0);
        }
        Socket socket = tcp.connectSocket(host, tcpPort);
        synchronized (this) {
            if (udp != transport) {
                TcpService.close(socket);
                throw new IOException("Disconnected during TCP connect");
            }
            connections.add(socket);
        }
        establish(socket, transport, true);
    }

    private void establish(Socket socket, UdpService transport, boolean outgoing) throws IOException {
        boolean established = false;
        try {
            socket.setSoTimeout(HANDSHAKE_TIMEOUT_MS);
            var input = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
            var output = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

            if (outgoing) {
                TcpService.writeHello(output, localId, transport.localPort());
            }
            TcpService.Hello remote = TcpService.readHello(input);
            Peer peer = new Peer(remote.id(), socket,
                    new InetSocketAddress(socket.getInetAddress(), remote.udpPort()), output, outgoing);
            socket.setSoTimeout(READ_TIMEOUT_MS);

            synchronized (this) {
                if (udp != transport || !connections.contains(socket) || localId.equals(peer.id)) {
                    throw new IOException("Invalid peer session: " + peer.id);
                }
                Peer existing = peers.get(peer.id);
                boolean preferOutgoing = localId.compareTo(peer.id) < 0;
                if (existing != null && (existing.outgoing == peer.outgoing
                        || existing.outgoing == preferOutgoing)) {
                    throw new IOException("Peer already connected: " + peer.id);
                }
                if (!outgoing) {
                    TcpService.writeHello(output, localId, transport.localPort());
                }
                peers.put(peer.id, peer);
                if (existing != null) {
                    TcpService.close(existing.socket);
                }
            }
            established = true;
            Thread.ofVirtual().name("peer-reader").start(() -> read(peer, input));
        } catch (SocketTimeoutException e) {
            throw new IOException("Timed out waiting for TCP HELLO from " + socket.getRemoteSocketAddress(), e);
        } finally {
            if (!established) {
                release(socket);
            }
        }
    }

    private void read(Peer peer, DataInputStream input) {
        Thread heartbeat = Thread.ofVirtual().name("peer-heartbeat").start(() -> heartbeat(peer));
        try {
            while (!peer.socket.isClosed()) {
                ControlMessage message = TcpService.readControl(input);
                if (message != null && message.type() != ControlMessage.Type.HELLO) {
                    controlCallback.accept(message);
                }
            }
        } catch (IOException e) {
            if (!peer.socket.isClosed()) {
                System.err.println("TCP peer " + peer.id + ": " + e);
            }
        } finally {
            heartbeat.interrupt();
            synchronized (this) {
                peers.remove(peer.id, peer);
                release(peer.socket);
            }
        }
    }

    private void heartbeat(Peer peer) {
        try {
            while (!peer.socket.isClosed()) {
                Thread.sleep(HEARTBEAT_INTERVAL_MS);
                TcpService.writeControl(peer.output, null);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } catch (IOException e) {
            TcpService.close(peer.socket);
        }
    }

    @Override
    public void sendControl(String peerId, ControlMessage message) throws IOException {
        Objects.requireNonNull(message);
        Peer peer;
        synchronized (this) {
            peer = requirePeer(peerId);
        }
        if (message.type() == ControlMessage.Type.HELLO) {
            throw new IOException("HELLO is reserved for the TCP handshake");
        }
        byte[] frame = TcpService.encodeControl(message);
        try {
            TcpService.writeFrame(peer.output, frame);
        } catch (IOException e) {
            TcpService.close(peer.socket);
            throw e;
        }
    }

    @Override
    public synchronized void sendVoice(String peerId, byte[] packet) throws IOException {
        Peer peer = requirePeer(peerId);
        udp.send(peer.udpAddress, packet);
    }

    private Peer requirePeer(String id) throws IOException {
        Peer peer = peers.get(id);
        if (peer == null || peer.socket.isClosed()) {
            throw new IOException("Peer not connected: " + id);
        }
        return peer;
    }

    private UdpService ensureUdp(int port) throws IOException {
        if (udp == null) {
            UdpService transport = new UdpService(port);
            udp = transport;
            Thread.ofVirtual().name("peer-udp").start(() -> receiveVoice(transport));
        }
        return udp;
    }

    private void receiveVoice(UdpService transport) {
        try {
            transport.receive((address, bytes) -> {
                String sender;
                synchronized (this) {
                    if (udp != transport) {
                        return;
                    }
                    sender = peers.values().stream()
                            .filter(peer -> !peer.socket.isClosed() && peer.udpAddress.equals(address))
                            .map(Peer::id).findFirst().orElse(null);
                }
                if (sender != null) {
                    try {
                        voiceCallback.accept(sender, bytes);
                    } catch (RuntimeException e) {
                        System.err.println("UDP callback: " + e.getMessage());
                    }
                }
            });
        } catch (IOException e) {
            synchronized (this) {
                if (udp == transport) {
                    System.err.println("UDP receive: " + e.getMessage());
                    disconnect();
                }
            }
        }
    }

    @Override
    public void onControlMessage(Consumer<ControlMessage> callback) {
        controlCallback = Objects.requireNonNull(callback);
    }

    @Override
    public void onVoicePacket(BiConsumer<String, byte[]> callback) {
        voiceCallback = Objects.requireNonNull(callback);
    }

    private synchronized void release(Socket socket) {
        connections.remove(socket);
        TcpService.close(socket);
    }

    @Override
    public synchronized void disconnect() {
        if (listener != null) {
            TcpService.close(listener);
            listener = null;
        }
        if (udp != null) {
            udp.close();
            udp = null;
        }
        connections.forEach(TcpService::close);
        connections.clear();
        peers.clear();
    }

    private record Peer(String id, Socket socket, InetSocketAddress udpAddress,
                        DataOutputStream output, boolean outgoing) {}
}
