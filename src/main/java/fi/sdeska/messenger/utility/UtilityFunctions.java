package fi.sdeska.messenger.utility;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;

import javax.net.ssl.SSLSocket;

public class UtilityFunctions {
    
    public void sendData(String data, SSLSocket socket) {

        try {
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            out.writeUTF(data);
            out.close();
        }
        catch (IOException e) {
            System.out.println("Failed to send data to server.");
            e.printStackTrace();
        }
        
    }
    
    public String readStringData(InputStream in) {

        var data = new ByteArrayOutputStream();
        var buffer = new byte[1024];
        try {
            for (int length; (length = in.read(buffer)) != -1; ) {
                data.write(buffer, 0, length);
            }
        } catch (SocketException e) {
            System.out.println("Error: Problem in the socket connection.");
            e.printStackTrace();
        }
        catch (IOException e) {
            System.out.println("Error: Unable to read received data.");
            e.printStackTrace();
        }
        try {
            return data.toString("UTF-8");
        } catch (UnsupportedEncodingException e) {
            System.out.println("Error: Unsupported encoding on return data.");
            e.printStackTrace();
            return "";
        }

    }

}
