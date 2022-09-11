package fi.sdeska.messenger.utility;

import java.io.DataInputStream;
import java.io.DataOutputStream;
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
    
    public String readStringData(DataInputStream in) {

        String data = "";
        try {
            data = in.readUTF();
        } catch (IOException e) {
            System.out.println("Error: Unable to read received data.");
            e.printStackTrace();
        }
        return data.toString();

    }

}
