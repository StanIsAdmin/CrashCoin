package be.ac.ulb.crashcoin.master;

import be.ac.ulb.crashcoin.common.BlockChain;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * Singleton class that manages the master node's BlockChain instance.
 *
 * Used to load it from/save it to a file.
 */
public class BlockChainManager {

    /**
     * Path where the blockchain file is saved
     */
    private static final String BLOCKCHAIN_SAVE_PATH = "./master/blockchain.json";

    /** BlockChain instance */
    private static BlockChain blockChain;

    private static BlockChainManager instance;

    /**
     * Creates a BlockChainManager instance, and its default blockChain.
     * 
     * @see default BlockChain constructor
     */
    private BlockChainManager() {
        blockChain = createBlockChain();
    }

    /**
     * @return The blockChainManager (initially empty)
     */
    public static BlockChainManager getInstance() {
        if (instance == null) {
            instance = new BlockChainManager();
        }
        return instance;
    }

    public BlockChain getBlockChain() {
        return blockChain;
    }

    /**
     * Loads a blockChain from the file and returns it.
     *
     * This does not update this.blockChain
     *
     * @return the blockchain created from BLOCKCHAIN_SAVE_PATH
     */
    private BlockChain createBlockChain() {
        FileReader fr;
        try {
            fr = new FileReader(BLOCKCHAIN_SAVE_PATH);
        } catch (FileNotFoundException ex) {
            // Returns a new BlockChain if none has been saved
            return new BlockChain();
        }

        final JSONParser parser = new JSONParser();
        JSONObject jsonBlockChain;
        try {
            jsonBlockChain = (JSONObject) parser.parse(fr);
        } catch (ParseException | IOException ex) {
            Logger.getLogger(BlockChainManager.class.getName()).log(Level.SEVERE, null, ex);
            return null;
        }

        return new BlockChain(jsonBlockChain);
    }

    /**
     * Save the blockchain to the BLOCKCHAIN_SAVE_PATH file.
     */
    public void saveBlockChain() {
        FileWriter fw;
        try {
            fw = new FileWriter(BLOCKCHAIN_SAVE_PATH);
        } catch (IOException ex) {
            Logger.getLogger(BlockChainManager.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        final JSONObject jsonBlockChain = blockChain.toJSON();
        try {
            fw.write(jsonBlockChain.toString());
            fw.flush();
        } catch (IOException ex) {
            Logger.getLogger(BlockChainManager.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
