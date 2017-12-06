package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.Transaction.Input;
import be.ac.ulb.crashcoin.common.Transaction.Output;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
    
    /* Maps inputs available for transactions to the Address they belong to */
    private final Map<byte[], Address> availableInputs;

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
        this.availableInputs = new HashMap<>();
        final Block genesis = createGenesisBlock();
        super.add(genesis); // call to super does not perform validity check
    }

    @Override
    public boolean add(final Block block) {
        try {
            if (isValidNextBlock(block, Parameters.MINING_DIFFICULTY)) {
                super.add(block);
                updateAvailableInputs(block);
                return true;
            } else {
                Logger.getLogger(BlockChain.class.getName()).log(Level.WARNING, "Invalid block discarded");
            }
        } catch (NoSuchAlgorithmException ex) {
            Logger.getLogger(BlockChain.class.getName()).log(Level.SEVERE, null, ex);
        }
        return false;
    }
    
    /**
     * Updates the internal representation of transactions available as inputs.
     * 
     * Transactions (outputs) received by an Address can only be used once
     * as inputs for new transactions from the same address.
     * This function assumes the received block is valid, and for each of its 
     * transactions :
     * - removes all used inputs from the pool of available inputs
     * - marks the first output an available input for the receiver
     * - marks the second output (change) as available for the sender, if any
     * @param addedBlock a valid block that has just been added to the blockchain
     */
    private synchronized void updateAvailableInputs(Block addedBlock) {
        for (final Transaction addedTransaction : addedBlock) {
            
            for (final Input usedInput : addedTransaction.getInputs()) {
                availableInputs.remove(usedInput.toBytes());
            }
            
            List<Output> transactionOutputs = addedTransaction.getOutputs();
            
            Output firstOutput = transactionOutputs.get(0);
            availableInputs.put(firstOutput.toBytes(), addedTransaction.getDestAddress());
            
            Output secondOutput = null;
            try {
                secondOutput = transactionOutputs.get(1);
            } catch (NullPointerException ex) {}
            
            if (secondOutput != null) {
                availableInputs.put(secondOutput.toBytes(), addedTransaction.getSrcAddress());
            }
        }
    }
    
    public Block getLastBlock() {
        return this.get(this.size() - 1);
    }

    private byte[] getLastBlockToBytes() throws NoSuchAlgorithmException {
        return Cryptography.hashBytes(get(this.size() - 1).headerToBytes());
    }
    
    /**
     * Gets the first bad transaction that it finds in a block, otherwise returns null.
     * A transaction is considered bad if isValidTransaction(transaction) returns false.
     * 
     * @param block  Block that needs to be added to the blockchain
     * @return  First bad transaction found if there is one, null otherwise
     */
    protected Transaction getFirstBadTransaction(final Block block) {
        for (Transaction transaction: block) {
            if (! isValidTransaction(transaction)) {
                return transaction;
            }
        }
        return null;
    }
    
    /** 
     * Returns true if transaction is valid, false otherwise.
     * For a transaction to be valid, it has to fulfill all of these requirements :
     * - have exactly one or two outputs (TODO)
     * - all output values must be strictly positive (TODO)
     * - the sum of the inputs values must be equal to the sum of output values (TODO)
     * - be digitally signed by the sender (TODO)
     * - have only previously-unused inputs that belong to the sender
     * 
     * @param transaction
     * @return 
     */
    private boolean isValidTransaction(Transaction transaction) {
        // Verify the transaction value
        if (! transaction.isValid()) return false;
        
        //TODO verify digital signature of transaction
        
        // Verify each input is available and belongs to the sender
        for (Transaction.Input input: transaction.getInputs()) {
            Address inputAddress = this.availableInputs.get(input.toBytes());
            if (inputAddress == null) return false;
            if (inputAddress != transaction.getSrcAddress()) return false;
        }
        
        // All verifications having passed, the transaction is valid
        return true;
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
        final Block genesisBlock = new Block(new byte[0], 0);
        final PublicKey masterPublicKey = Cryptography.createPublicKeyFromBytes(Parameters.MASTER_WALLET_PUBLIC_KEY);
        final Address masterWallet = new Address(masterPublicKey);
        final Timestamp genesisTime = new Timestamp(0L);
        final Transaction reward = new Transaction(masterWallet, null, Parameters.MINING_REWARD, genesisTime, 
                Parameters.GENESIS_SIGNATURE);
        reward.addOutput(masterWallet, Parameters.MINING_REWARD);
        genesisBlock.add(reward);
        return genesisBlock;
    }

}
