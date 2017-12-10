package be.ac.ulb.crashcoin.master;

import be.ac.ulb.crashcoin.common.BlockChain;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

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
        final String fileContent;
        try {
            fileContent = readFile(BLOCKCHAIN_SAVE_PATH);
        } catch (IOException ex) {
            // Returns a new BlockChain if none has been saved
            return new BlockChain();
        }
        
        final JSONObject jsonBlockChain = new JSONObject(fileContent);
        return new BlockChain(jsonBlockChain);
    }
    
    private static String readFile(final String filename) throws FileNotFoundException, IOException {
        String result = "";
        final BufferedReader br = new BufferedReader(new FileReader(filename));
        String line = br.readLine();
        while (line != null) {
            result += line;
            line = br.readLine();
        }
        return result;
    }

    /**
     * Save the blockchain to the BLOCKCHAIN_SAVE_PATH file.
     */
    public void saveBlockChain() {
        final File file = new File(BLOCKCHAIN_SAVE_PATH);
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error when create folder: {0}", 
                        ex.getMessage());
            }
        }
        
        final FileWriter fw;
        try {
            fw = new FileWriter(file);
        } catch (IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not write blockchain: {0}", ex.getMessage());
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
