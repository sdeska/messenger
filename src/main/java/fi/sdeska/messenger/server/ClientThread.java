package fi.sdeska.messenger.server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.net.ssl.SSLSocket;

import fi.sdeska.messenger.utility.UtilityFunctions;

public class ClientThread extends Thread {

    private SSLSocket socket = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;
    private static UtilityFunctions util = null;

    ClientThread(SSLSocket socket) {
        util = new UtilityFunctions();
        this.socket = socket;
        try {
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            System.out.println("Error: Failed to get data streams for client's socket.");
            e.printStackTrace();
        }
        // Sending initial data so that the clientside handshake method does not get blocked.
        util.sendData("", out);
        this.setName(util.readStringData(in));
    }

    public void run() {
        while(true) {
            util.readStringData(in);
        }
    }

}
