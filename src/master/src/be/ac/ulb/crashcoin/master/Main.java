package be.ac.ulb.crashcoin.master;

import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.master.net.RelayListener;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * 
 */
public class Main {
    
    public static void main(final String[] args) {
        // Init listener
        try {
            RelayListener.getListener();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
