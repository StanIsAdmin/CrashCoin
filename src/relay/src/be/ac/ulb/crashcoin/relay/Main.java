package be.ac.ulb.crashcoin.relay;

import be.ac.ulb.crashcoin.relay.net.MasterConnection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public class Main {
    
    public static void main(final String main) {
        // TODO
        
        // Init listener
        try {
            MasterConnection.getMasterConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
