package be.ac.ulb.crashcoin.relay;

import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.relay.net.MasterConnection;
import be.ac.ulb.crashcoin.relay.net.MinerListener;
import be.ac.ulb.crashcoin.relay.net.WalletListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public class Main {
    
    private static BlockChain _blockChain;
    
    public static void main(final String[] main) {
        // TODO
        _blockChain = new BlockChain();
            
        // Enable listener
        try {
            MinerListener.getListener();
            WalletListener.getListener();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        // Connect to master
        try {
            MasterConnection.getMasterConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static BlockChain getBlockChain() {
        return _blockChain;
    }
    
}
