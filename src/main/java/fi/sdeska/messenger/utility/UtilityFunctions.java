package fi.sdeska.messenger.utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * Contains any general use and utility functions, which can be used by both the client and the server.
 */
public class UtilityFunctions {
    
    /**
     * Writes string data to a specified output stream.
     * @param data The string data to send.
     * @param out The DataOutputStream to write the data to.
     */
    public void sendData(String data, DataOutputStream out) {

        try {
            out.writeUTF(data);
        }
        catch (IOException e) {
            System.out.println("Error: Failed to write data to the specified output stream.");
            e.printStackTrace();
        }
        
    }
    
    /**
     * Reads data from an input stream. Blocks execution until data is written to the input stream.
     * @param in The DataInputStream from which to read data from.
     * @return The data read from the stream.
     * @throws EOFException Thrown if the stream connection is unexpectedly closed.
     * This most likely means that the connection was lost and the socket may be closed on the reader side.
     */
    public String readStringData(DataInputStream in) throws EOFException {

        String data = "";
        try {
            data = in.readUTF();
        } catch (EOFException e) { // Without this IOFException gets caught under IOException instead of throwing to the calling function.
            throw new EOFException("End of stream reached unexpectedly.");
        } catch (IOException e) {
            System.out.println("Error: Unable to read received data.");
            e.printStackTrace();
        }
        return data;

    }

}
