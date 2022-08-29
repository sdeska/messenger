package fi.sdeska.messenger;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

public class MessengerServer{

    private final int serverPort = 29999;

    private SSLServerSocket socket = null;
    private List<Socket> connections = null;

    MessengerServer() {

        connections = new ArrayList<Socket>();
        try {
            SSLServerSocketFactory ssf = (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            socket = (SSLServerSocket) ssf.createServerSocket(serverPort);
            System.out.println("Server started.");
        }
        catch (IOException e) {
            System.out.println("Error: Could not create socket.");
            e.printStackTrace();
        }

    }

    public void listenForConnections() {

        try {
            System.out.println("Waiting for connections...");
            Socket client = socket.accept();
            connections.add(client);
            System.out.println("Client connected.");
        }
        catch (Exception e) {
            System.out.println("Error: Setting up connection failed.");
            e.printStackTrace();
        }

    }

    public static void main(String[] args) {

        var server = new MessengerServer(); 
        var listeningThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    server.listenForConnections();
                }
            }
        });
        listeningThread.run();

    }

}
