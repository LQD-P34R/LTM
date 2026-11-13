package com.voicechat.network;

import com.voicechat.common.model.ControlMessage;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.time.DateTimeException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

public class TcpService {
    private static final JsonFactory JSON = new JsonFactory().enable(JsonParser.Feature.STRICT_DUPLICATE_DETECTION);
    private static final int MAX_FRAME_SIZE = 512 * 1024;

    public ServerSocket startListening(int port) throws IOException {
        ServerSocket server = new ServerSocket(port);

        Thread.ofVirtual().name("tcp-accept").start(() -> {
            while (!server.isClosed()) {
                try {
                    Socket peer = server.accept();
                    Thread.ofVirtual().name("tcp-peer-reader")
                            .start(() -> readFromPeer(peer));
                } catch (IOException e) {
                    if (!server.isClosed()) {
                        System.err.println("Loi nhan ket noi TCP: " + e.getMessage());
                    }
                    break;
                }
            }
        });

        return server;
    }

    public Socket connect(String host, int port) throws IOException {
        Socket socket = connectSocket(host, port);
        Thread.ofVirtual().name("tcp-peer-reader")
                .start(() -> readFromPeer(socket));
        return socket;
    }

    /** Opens a connection whose input and output streams are owned by the caller. */
    public Socket connectSocket(String host, int port) throws IOException {
        InetSocketAddress address = new InetSocketAddress(host, port);
        Socket socket = new Socket();
        try {
            socket.connect(address, 3000);
            return socket;
        } catch (IOException | RuntimeException e) {
            try {
                socket.close();
            } catch (IOException closeFailure) {
                e.addSuppressed(closeFailure);
            }
            throw e;
        }
    }

    private void readFromPeer(Socket peer) {
        try (peer; var input = new DataInputStream(peer.getInputStream())) {
            System.out.println("Da ket noi: " + peer.getRemoteSocketAddress());

            while (true) {
                String message = input.readUTF();
                System.out.println("Nhan: " + message);
            }
        } catch (EOFException e) {
            System.out.println("Peer da ngat ket noi");
        } catch (IOException e) {
            System.err.println("Loi doc TCP: " + e.getMessage());
        }
    }

    record Hello(String id, int udpPort) {}

    static void writeHello(DataOutputStream output, String id, int udpPort) throws IOException {
        writeControl(output, new ControlMessage(ControlMessage.Type.HELLO, id, null, null,
                "{\"udpPort\":" + udpPort + "}", Instant.now()));
    }

    static Hello readHello(DataInputStream input) throws IOException {
        ControlMessage message = readControl(input);
        if (message == null || message.type() != ControlMessage.Type.HELLO
                || message.senderId() == null || message.senderId().isBlank() || message.payload() == null) {
            throw new IOException("Expected HELLO with senderId and UDP port");
        }
        try (JsonParser parser = JSON.createParser(message.payload())) {
            if (parser.nextToken() != JsonToken.START_OBJECT || parser.nextToken() != JsonToken.FIELD_NAME
                    || !"udpPort".equals(parser.currentName()) || parser.nextToken() != JsonToken.VALUE_NUMBER_INT) {
                throw new IOException("Expected HELLO payload with integer udpPort");
            }
            int port = parser.getIntValue();
            if (port < 1 || port > 65535 || parser.nextToken() != JsonToken.END_OBJECT
                    || parser.nextToken() != null) {
                throw new IOException("Invalid HELLO UDP port or payload");
            }
            return new Hello(message.senderId(), port);
        }
    }

    static byte[] encodeControl(ControlMessage message) throws IOException {
        if (message.type() == null) {
            throw new IOException("Control message requires a type");
        }
        var buffer = new ByteArrayOutputStream();
        try (var json = JSON.createGenerator(buffer)) {
            json.writeStartObject();
            json.writeStringField("type", message.type().name());
            json.writeStringField("senderId", message.senderId());
            json.writeStringField("targetId", message.targetId());
            json.writeStringField("roomId", message.roomId());
            json.writeStringField("payload", message.payload());
            json.writeStringField("sentAt", message.sentAt() == null ? null : message.sentAt().toString());
            json.writeEndObject();
        }
        if (buffer.size() > MAX_FRAME_SIZE) {
            throw new IOException("TCP frame exceeds " + MAX_FRAME_SIZE + " bytes");
        }
        return buffer.toByteArray();
    }

    static void writeControl(DataOutputStream output, ControlMessage message) throws IOException {
        writeFrame(output, message == null ? new byte[0] : encodeControl(message));
    }

    static void writeFrame(DataOutputStream output, byte[] bytes) throws IOException {
        synchronized (output) {
            output.writeInt(bytes.length);
            output.write(bytes);
            output.flush();
        }
    }

    static ControlMessage readControl(DataInputStream input) throws IOException {
        int length = input.readInt();
        if (length == 0) {
            return null;
        }
        if (length < 0 || length > MAX_FRAME_SIZE) {
            throw new IOException("Invalid TCP frame length: " + length);
        }
        byte[] bytes = input.readNBytes(length);
        if (bytes.length != length) {
            throw new EOFException("Incomplete TCP frame");
        }
        String json = StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
        try (JsonParser parser = JSON.createParser(json)) {
            if (parser.nextToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected JSON control object");
            }
            Map<String, String> fields = new HashMap<>();
            while (parser.nextToken() == JsonToken.FIELD_NAME) {
                String name = parser.currentName();
                JsonToken value = parser.nextToken();
                if (value != JsonToken.VALUE_STRING && value != JsonToken.VALUE_NULL) {
                    throw new IOException("Expected string or null for " + name);
                }
                fields.put(name, value == JsonToken.VALUE_NULL ? null : parser.getText());
            }
            if (parser.currentToken() != JsonToken.END_OBJECT || parser.nextToken() != null
                    || fields.get("type") == null) {
                throw new IOException("Invalid JSON control object");
            }
            String sentAt = fields.get("sentAt");
            return new ControlMessage(ControlMessage.Type.valueOf(fields.get("type")),
                    fields.get("senderId"), fields.get("targetId"), fields.get("roomId"),
                    fields.get("payload"), sentAt == null ? null : Instant.parse(sentAt));
        } catch (IllegalArgumentException | DateTimeException e) {
            throw new IOException("Invalid control message", e);
        }
    }

    static void close(Closeable resource) {
        try {
            resource.close();
        } catch (IOException e) {
            System.err.println("TCP close: " + e.getMessage());
        }
    }

}
