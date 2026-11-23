package com.voicechat;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

class TcpServerDemo {
    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : 5000;
        try (ServerSocket server = new ServerSocket(port)) {
            System.out.println("B dang cho ket noi o cong " + server.getLocalPort());

            try (Socket socket = server.accept()) {
                socket.setSoTimeout(3000);
                var input = new DataInputStream(socket.getInputStream());
                var output = new DataOutputStream(socket.getOutputStream());

                System.out.println("B nhan: " + input.readUTF());
                output.writeUTF("B da nhan tin");
                output.flush();
            }
        }
    }
}
