package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
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
    private final Map<byte[], TransactionOutput> availableInputs;

    // Used by [Relay Node]
    public BlockChain(final JSONObject json)  {
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
    public BlockChain()  {
        this.availableInputs = new HashMap<>();
        final Block genesis = createGenesisBlock();
        super.add(genesis); // call to super does not perform validity check
    }

    @Override
    public boolean add(final Block block) {
        if (isValidNextBlock(block, Parameters.MINING_DIFFICULTY)) {
            super.add(block);
            updateAvailableInputs(block);
            return true;
        } else {
            Logger.getLogger(BlockChain.class.getName()).log(Level.WARNING, "Invalid block discarded");
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
    private synchronized void updateAvailableInputs(Block addedBlock)  {
        for (final Transaction addedTransaction : addedBlock) {
            
            for (final TransactionInput usedInput : addedTransaction.getInputs()) {
                availableInputs.remove(usedInput.toBytes());
            }
            
            final TransactionOutput transactionOutput = addedTransaction.getTransactionOutput();
            this.availableInputs.put(Cryptography.hashBytes(transactionOutput.toBytes()), transactionOutput);
            final TransactionOutput changeOutput = addedTransaction.getChangeOutput();
            // only added to the hashmap if change is strictly positive:
            // outputs with 0 change would never be used again, therefore it is
            // not necessary to keep them in memory
            if(!addedTransaction.isReward() && changeOutput.getAmount() > 0)
                this.availableInputs.put(Cryptography.hashBytes(changeOutput.toBytes()), changeOutput);
        }
    }
    
    public Block getLastBlock() {
        return this.get(this.size() - 1);
    }

    private byte[] getLastBlockToBytes()  {
        return Cryptography.hashBytes(get(this.size() - 1).headerToBytes());
    }
    
    /**
     * Gets the first bad transaction that it finds in a block, otherwise returns null.
     * A transaction is considered bad if isValidTransaction(transaction) returns false.
     * 
     * @param block  Block that needs to be added to the blockchain
     * @return  First bad transaction found if there is one, null otherwise
     */
    protected Transaction getFirstBadTransaction(final Block block)  {
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
     * - transaction.isValid() == true
     * - be digitally signed by the sender (TODO)
     * - have only previously-unused inputs that belong to the sender
     * 
     * @see Transaction.isValid
     * @param transaction
     * @return 
     */
    private boolean isValidTransaction(Transaction transaction)  {
        // Verify the transaction value
        if (! transaction.isValid())
            return false;
        
        //TODO verify digital signature of transaction

        if(!transaction.isReward()) {
            // Verify each input is available and belongs to the sender
            for (final TransactionInput input: transaction.getInputs()) {
                final TransactionOutput referencedOutput = this.availableInputs.get(Cryptography.hashBytes(input.toBytes()));
                if (referencedOutput == null)
                    return false;
                // verify that the destination address of the referenced output corrponds to
                // the address of the payer (i.e. destination addess of the change output)
                if(!referencedOutput.getDestinationAddress().equals(transaction.getSrcAddress()))
                    return false;
            }
        }
        
        // All verifications having passed, the transaction is valid
        return true;
    }

    // Must may be move to Block
    // Used by [master node]
    protected boolean isValidNextBlock(final Block block, final int difficulty)  {
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

    protected static Block createGenesisBlock()  {
        Block genesisBlock = new Block(new byte[0], 0);
        PublicKey masterPublicKey = Cryptography.createPublicKeyFromBytes(Parameters.MASTER_WALLET_PUBLIC_KEY);
        Address masterWallet = new Address(masterPublicKey);
        Timestamp genesisTime = new Timestamp(0L);
        Transaction reward = new Transaction(masterWallet, genesisTime);
        genesisBlock.add(reward);
        return genesisBlock;
    }

}
