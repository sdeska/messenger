package fi.sdeska.messenger;

public class MessengerClient {
    
    private String name;
    
    public boolean setName(String name) {
        
        if (name.isEmpty() || name == null || name == "") {
            return false;
        }
        this.name = name;
        return true;

    }

    public String getName() {
        return this.name;
    }

}
