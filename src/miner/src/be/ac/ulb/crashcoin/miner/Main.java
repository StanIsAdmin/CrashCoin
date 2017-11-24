package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * Entry point of the miner program. 
 */
public class Main {
    
    public static void main(final String[] args) {
        
        RelayConnection connection = null;
        // Connect to relay
        try {
            connection = RelayConnection.getRelayConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Test : transacton sending
        final JSONObject json = new JSONObject();
        json.put("value", "Test");
        connection.sendData(json);
        // -------------------------
        
        // -------------------------
        
        // create a miner... And start mining... Whut else?
        Miner miner;
        try {
            miner = Miner.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            miner.startMining();
        } catch (InterruptedException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
    }
    
}
