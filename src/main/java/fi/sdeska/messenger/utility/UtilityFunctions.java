package fi.sdeska.messenger.utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;

public class UtilityFunctions {
    
    public void sendData(String data, DataOutputStream out) {

        try {
            out.writeUTF(data);
        }
        catch (IOException e) {
            System.out.println("Error: Failed to send data to server.");
            e.printStackTrace();
        }
        
    }
    
    public String readStringData(DataInputStream in) throws EOFException {

        String data = "";
        try {
            data = in.readUTF();
        } catch (EOFException e) { // Without this IOFException gets catched under IOException instead of throwing to the calling function.
            throw new EOFException("End of stream reached unexpectedly.");
        } catch (IOException e) {
            System.out.println("Error: Unable to read received data.");
            e.printStackTrace();
        }
        return data.toString();

    }

}
