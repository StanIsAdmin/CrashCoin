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
    
    private static BlockChain blockChain;
    
    private static final String BLOCKCHAIN_SAVE_PATH = "./master/blockchain.json";
    
    public static void main(final String[] args) {
        blockChain = createBlockChain();
        
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
    
    private static BlockChain createBlockChain() {
        FileReader fr;
        try {
            fr = new FileReader(BLOCKCHAIN_SAVE_PATH);
        } catch (FileNotFoundException ex) {
            // Returns a new BlockChain if none has been saved
            return new BlockChain();
        }
        
        JSONParser parser = new JSONParser();
        JSONObject jsonBlockChain;
        try {
            jsonBlockChain = (JSONObject) parser.parse(fr);
        } catch (ParseException | IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }
        
        return new BlockChain(jsonBlockChain);
    }
    
}
