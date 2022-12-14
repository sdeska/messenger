package fi.sdeska.messenger.client;

import java.io.DataInputStream;
import java.io.IOException;

import fi.sdeska.messenger.utility.UtilityFunctions;

/**
 * Handles listening for and reacting to messages coming from the connected server.
 * This allows for exchanges of data which have been initiated by the server instead 
 * of the client without blocking client-side execution.
 */
public class ListeningThread extends Thread {
    
    private static UtilityFunctions util = new UtilityFunctions();

    private MessengerClient client = null;
    private DataInputStream in = null;

    private boolean running;

    /**
     * The constructor simply saves the client for which this thread is listening 
     * for incoming communication and initializes any required variables.
     * @param client the client which uses this thread.
     */
    ListeningThread(MessengerClient client) {

        running = true;
        this.client = client;
        in = client.getIn();
    
    }

    /**
     * Overridden run() method of Thread class. Allocates an instance of ListeningThread and runs it on its own thread.
     * Contains the control structure for processing received messages.
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
                System.out.println("Connection lost. Closing listening thread.");
                running = false;
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
                receivedMessage(senderAndMessage);
            }
        }
        
    }

    /**
     * Processes a received message.
     * @param received the string containing the name of the sender and the actual message.
     */
    void receivedMessage(String received) {

        var parameters = received.split(":");
        var messageBuilder = new StringBuilder(parameters[1]);
        if (parameters.length > 2) {
            for (var index = 2; index < parameters.length; index++) {
                messageBuilder.append(":" + parameters[index]);
            }
        }
        client.addMessage(parameters[0], messageBuilder.toString());

    }

    /**
     * Ends the thread by setting running to false.
     */
    void endListeningThread() {
        running = false;
    }

}
