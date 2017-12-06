package be.ac.ulb.crashcoin.master;

import be.ac.ulb.crashcoin.master.net.RelayListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main class of Master
 */
public class Main {

    public static void main(final String[] args) {
        System.out.println("Starting Master");
        
        // Init listener
        try {
            RelayListener.getListener();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
