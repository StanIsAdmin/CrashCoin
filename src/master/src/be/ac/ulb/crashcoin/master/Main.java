package be.ac.ulb.crashcoin.master;

import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.master.net.RelayListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 */
public class Main {
    
    private static BlockChain blockChain;
    
    public static void main(final String[] args) {
        blockChain = new BlockChain();
        
        // Init listener
        try {
            RelayListener.getListener();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static BlockChain getBlockChain() {
        return blockChain;
    }
    
}
