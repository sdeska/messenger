package fi.sdeska.messenger.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles listening for and reacting to messages coming from the connected server.
 * This allows for exchanges of data which have been initiated by the server instead of the client.
 */
public class ListeningThread extends Thread {
    
    private static UtilityFunctions util = new UtilityFunctions();

    private MessengerClient client = null;
    private DataInputStream in = null;
    private DataOutputStream out = null;

    private boolean running;

    /**
     * The constructor simply saves the client for which this thread is listening for incoming communication and initializes any required variables.
     * @param client the client which uses this thread.
     */
    ListeningThread(MessengerClient client) {

        running = true;
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
            // Close thread if variable 'running' is false.
            if (!running) {
                break;
            }
            String received = "";
            try {
                received = util.readStringData(in);
            } catch (IOException e) {
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
            else if (received.contains("Message")) {
                var senderAndMessage = received.replace("Message:", "");
                var parameters = senderAndMessage.split(":");
                client.addMessage(parameters[0], parameters[1]);
            }
        }
    }

    /**
     * Ends the thread by setting running to false.
     */
    public void endListeningThread() {
        running = false;
    }

}
