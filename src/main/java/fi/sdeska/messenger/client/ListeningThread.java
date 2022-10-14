package fi.sdeska.messenger.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles listening for and reacting to messages coming from the connected server.
 * This allows for exchanges of data which have been initiated by the server instead of the client.
 */
public class ListeningThread extends Thread {
    
    private static UtilityFunctions util = null;

    private MessengerClient client = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    /**
     * The constructor simply saves the client for which this thread is listening for incoming communication and initializes any required variables.
     * @param client the client which uses this thread.
     */
    ListeningThread(MessengerClient client) {
    
        util = new UtilityFunctions();
        this.client = client;
        in = client.getIn();
        out = client.getOut();
    
    }

    /**
     * Overridden run() method of Thread class. Allocates an instance of ListeningThread and runs it on its own thread.
     */
    @Override
    public void run() {
        while (true) {
            String received = "";
            try {
                received = util.readStringData(in);
            } catch (EOFException e) {
                System.out.println("Closing listening thread.");
                // Close thread by breaking out of the run() loop.
                break;
            }
            if (received.contains("Client-Addition")) {
                var name = received.replace("Client-Addition:", "");
                client.addClients(name);
            }
            else if (received.contains("Client-Removal")) {
                var name = received.replace("Client-Removal:", "");
                client.removeClient(name);
            }
        }
    }

}
