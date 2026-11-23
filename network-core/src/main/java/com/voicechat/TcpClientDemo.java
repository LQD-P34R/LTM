package com.voicechat;

import com.voicechat.network.TcpService;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.net.Socket;

public class TcpClientDemo {
    public static void main(String[] args) throws Exception {
        String host = args.length > 0 ? args[0] : "127.0.0.1";
        int port = args.length > 1 ? Integer.parseInt(args[1]) : 5000;
        try (Socket socket = new TcpService().connectSocket(host, port)) {
            socket.setSoTimeout(3000);
            var input = new DataInputStream(socket.getInputStream());
            var output = new DataOutputStream(socket.getOutputStream());
            output.writeUTF("Xin chao tu A");
            output.flush();
            System.out.println("A nhan: " + input.readUTF());
        }
    }
}
