package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point of the miner program. 
 */
public class Main {
    
    public static void main(final String[] args) {
        
        // Connect to relay
        try {
            RelayConnection.getRelayConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // create a miner... And start mining... Whut else?
        Miner miner = new Miner();
        miner.startMining();
    }
    
}
