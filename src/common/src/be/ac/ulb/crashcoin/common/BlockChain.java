package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stock block
 */
public class BlockChain extends ArrayList<Block> implements JSONable {

    // Used by [Relay Node]
    public BlockChain(final JSONObject json) {
        this(); // Creates BC containing genesis bloc
        final JSONArray blockArray = json.getJSONArray("blockArray");

        for (int i = 0; i < blockArray.length(); ++i) {
            final Object type = blockArray.get(i);
            if (type instanceof JSONObject) {
                this.add(new Block((JSONObject) type));
            } else {
                throw new IllegalArgumentException("Unknow object in blockArray ! " + type);
            }
        }
        // TODO
    }

    // Used by [Master node]
    public BlockChain() {
        final Block genesis = createGenesisBlock();
        super.add(genesis); // call to super does not perform validity check
    }

    @Override
    public boolean add(final Block block) {
        try {
            if (isValidNextBlock(block, Parameters.MINING_DIFFICULTY)) {
                super.add(block);
                return true;
            } else {
                Logger.getLogger(BlockChain.class.getName()).log(Level.WARNING, "Invalid block discarded");
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    public Block getLastBlock() {
        return this.get(this.size() - 1);
    }

    private byte[] getLastBlockToBytes() throws NoSuchAlgorithmException {
        return Cryptography.hashBytes(get(this.size() - 1).headerToBytes());
    }
    
    /**
     * Gets the first bad transaction that it finds, otherwise returns null.
     * A transaction is considered bad if it spends the same input as another
     * transaction from the blockchain, and has a posterior timestamp (time priority).
     * Also it is considered bad if it has a timestamp prior to one of its input.
     * This prevents clients from double spending their inputs.
     * 
     * TODO: Rémy you can use this
     * 
     * @param block  Block that needs to be added to the blockchain
     * @return  First bad transaction found if there is one, null otherwise
     */
    protected Transaction getFirstBadTransaction(final Block block) {
        // Looks for each transaction inputs in other blocks and checks that
        // timestamps are not anachronistic
        Set<Transaction.Input> spentInputs = new HashSet<>();
        for (Transaction transaction: block) {
            for (Transaction.Input input: transaction.getInputs()) {
                boolean isPresent = false;
                for (Block previousBlock: this) {
                    Transaction searchResult = previousBlock.findTransaction(input.toBytes());
                    if ((searchResult != null) && (searchResult.before(transaction))) {
                        isPresent = true;
                    }
                }
                if (!isPresent) return transaction;  
                // Avoid double-spending
                if (spentInputs.contains(input)) {
                    return transaction;
                } else {
                    spentInputs.add(input);
                }
            }
        }
        return null;
    }

    // Must may be move to Block
    // Used by [master node]
    protected boolean isValidNextBlock(final Block block, final int difficulty) throws NoSuchAlgorithmException {
        boolean result = block.isHashValid()
                && difficulty == block.getDifficulty()
                && // Previous hash block is valid
                Arrays.equals(block.getPreviousBlock(), this.getLastBlockToBytes());
        result &= (getFirstBadTransaction(block) != null);
        return result;
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();

        try {
            final JSONArray jArray = new JSONArray();
            // Add every block except for the genesis block
            for (final Block block : this.subList(1, this.size())) {
                jArray.put(block.toJSON());
            }
            json.put("blockArray", jArray);
        } catch (JSONException jse) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, jse);
        }
        return json;
    }

    protected static Block createGenesisBlock() {
        Block genesisBlock = new Block(new byte[0], 0);
        PublicKey masterPublicKey = Cryptography.createPublicKeyFromBytes(Parameters.MASTER_WALLET_PUBLIC_KEY);
        Address masterWallet = new Address(masterPublicKey);
        Timestamp genesisTime = new Timestamp(0L);
        Transaction reward = new Transaction(masterWallet, Parameters.MINING_REWARD, genesisTime);
        genesisBlock.add(reward);
        return genesisBlock;
    }

}
