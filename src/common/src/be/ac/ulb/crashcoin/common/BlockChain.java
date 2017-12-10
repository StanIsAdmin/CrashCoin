package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.PublicKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Represents the CrashCoin BlockChain as a list of Block instances.
 * Manage the BlockChain. Initialises it and valids mined blocks.
 *
 * The add method has been overridden to verify the block to add and
 * each of its transactions.
 *
 * (Note that: In CrashCoin, there is a central trusted node)
 */
public class BlockChain extends ArrayList<Block> implements JSONable {

    /** Maps inputs available for transactions to the Address they belong to */
    private final Map<byte[], TransactionOutput> availableInputs;
    
    /** Maps a new block's used inputs from their hashes */
    private final Map<byte[], TransactionOutput> tempUsedInputs;
    
    /** Maps a new block's generated available inputs from their hashes */
    private final Map<byte[], TransactionOutput> tempAvailableInputs;

    /**
     * Creates a BlockChain which contains only the genesis block.
     *
     * The genesis block is created through the createGenesisBlock() method.
     * @see #createGenesisBlock
     */
    public BlockChain()  {
        this.availableInputs = new HashMap<>();
        this.tempUsedInputs = new HashMap<>();
        this.tempAvailableInputs = new HashMap<>();
        
        final Block genesis = createGenesisBlock();
        addAvailableTransactionOutputs(genesis.get(0)); // add first transaction to the available inputs
        super.add(genesis); // call to super does not perform validity check
    }

    /**
     * Creates a BlockChain instance from its JSON representation.
     *
     * Used by the Master to send the BlockChain the relays.
     *
     * @param json the JSON representation of the BlockChain, compatible with
     * the result of BlockChain.toJSON()
     * @see #toJSON
     */
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
    }

    public Block getLastBlock() {
        return this.get(this.size() - 1);
    }

    private byte[] getLastBlockToBytes()  {
        return Cryptography.hashBytes(get(this.size() - 1).headerToBytes());
    }
    
    /**
     * Verifies the block, add it to the end of the blockChain if it is valid
     * and update the set of transactions available as inputs.
     *
     * @param block
     * @return whether the block was added to the blockchain.
     */
    @Override
    public final boolean add(final Block block) {
        final boolean valid = isValidNextBlock(block, Parameters.MINING_DIFFICULTY);
        if (valid) {
            super.add(block);
        } else {
            // Remove the block's available inputs, put back the ones it used
            revertAvailbleInputs();
            Logger.getLogger(BlockChain.class.getName()).log(Level.WARNING, "Invalid block discarded");
        }
        return valid;
    }
    
    /**
     * Performs checks one the given block and returns whether it may be
     * appended to the Blockchain.
     *
     * - The proof of work realised must correspond to the difficulty indicated
     *   in the block
     * - The difficulty indicated must correspond to the required difficulty
     * - The block must indicate the current last block as previous block
     *
     * @param block The block to check
     * @param difficulty the difficulty required
     * @return wether the block may validly appended to the blockchain.
     */
    protected boolean isValidNextBlock(final Block block, final int difficulty)  {
        return block.isHashValid() // check that the hash corresponds the indicated difficulty
                && difficulty == block.getDifficulty() // check that the indicated difficulty corresponds to the required difficulty
                && // Previous hash block is valid
                Arrays.equals(block.getPreviousBlock(), this.getLastBlockToBytes()) 
                &&  getBadTransactions(block).isEmpty(); // Check the transaction
    }

    /**
     * Gets the first bad transaction that it finds in a block, otherwise returns null.
     * A transaction is considered bad if isValidTransaction(transaction) returns false.
     *
     * @param block  Block that needs to be added to the blockchain
     * @return  First bad transaction found if there is one, null otherwise
     */
    public HashSet<Transaction> getBadTransactions(final Block block)  {
        final HashSet<Transaction> badTransactions = new HashSet<>();
        tempUsedInputs.clear();
        tempAvailableInputs.clear();
        for (final Transaction transaction: block) {
            final boolean isReward = (transaction == block.get(block.size()-1));
            if (! isValidTransaction(transaction, isReward)) {
                badTransactions.add(transaction);
            }
        }
        return badTransactions;
    }

    /**
     * Returns true if transaction is valid, false otherwise.
     * The validity check depends on whether the transaction is meant to be 
     * a reward or not.
     * For a reward transaction to be valid, it has to meet all of these conditions :<br>
     * - transaction.isValidReward() == true<br>
     * <br>
     * For a non-reward transaction to be valid, it has to meet all of these conditions :<br>
     * - transaction.isValidNonReward() == true<br>
     * 
     *
     * @see Transaction#isValidNonReward() 
     * @see Transaction#isValidReward() 
     * @param transaction
     * @return true if the transaction is valid as defined, false otherwise
     */
    private boolean isValidTransaction(final Transaction transaction, boolean isReward)  {
        // Verify the transaction is valid as a reward
        if (isReward)
            return transaction.isValidReward();
        
        // Verify the transaction is valid as a non-reward
        if (! transaction.isValidNonReward())
            return false;

        // Verify that each input is available and belongs to the sender
        for (final TransactionInput input: transaction.getInputs()) {
            // Temporarily remove the input so that it can't be used again
            final byte[] inputHash = input.getHashBytes();

            TransactionOutput referencedOutput = null;
            // Must use loop because "byte[]".equals juste test instance and not value
            for(final byte[] addressAvailable : availableInputs.keySet()) {
                if(Arrays.equals(addressAvailable, inputHash)) {
                    referencedOutput = availableInputs.remove(addressAvailable);
                    break;
                }
            }

            if (referencedOutput == null) {
                return false;
            }
            // Keep a copy of the discarded input, to put back if the block is invalid
            tempUsedInputs.put(inputHash, referencedOutput);

            // verify that the destination address of the referenced output corrponds to
            // the address of the payer (i.e. destination addess of the change output)
            if(!referencedOutput.getDestinationAddress().equals(transaction.getSrcAddress()))
                return false;
        }
        
        // Temporarily add the the outputs to the set of available inputs
        this.addAvailableTransactionOutputs(transaction);

        // All verifications having passed, the transaction is valid
        return true;
    }
    
    /**
     * Adds a transaction's outputs to the set of available inputs.
     * 
     * @param transaction the transaction to (temporarily) validate.
     */
    private void addAvailableTransactionOutputs(final Transaction transaction) {
        // Add the transactino output
        final TransactionOutput transactionOutput = transaction.getTransactionOutput();
        final byte[] transactionHash = transactionOutput.getHashBytes();
        this.availableInputs.put(transactionHash, transactionOutput);
        this.tempAvailableInputs.put(transactionHash, transactionOutput);
        
        // Add the change output, only if it exists and has a strictly positive amount
        // (this avoids keeping useless transactions in memory)
        final TransactionOutput changeOutput = transaction.getTransactionOutput();
        if (! transaction.isReward() && changeOutput.getAmount() > 0) {
            final byte[] changeHash = changeOutput.getHashBytes();
            this.availableInputs.put(changeHash, changeOutput);
            this.tempAvailableInputs.put(changeHash, changeOutput);
        }
    }
    
/**
     * Updates the internal representation of transactions available as inputs,
     * after a block has been discarded.
     *
     * While a block is verified, the inputs it uses are removed from the set
     * of available inputs, and the outputs it generates are added to that set.
     * This function reverts the process thanks to temporary sets of
     */
    private synchronized void revertAvailbleInputs()  {
        this.availableInputs.keySet().removeAll(this.tempAvailableInputs.keySet());
        this.availableInputs.putAll(this.tempUsedInputs);
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

    @Override
    public boolean equals(final Object equalsObject) {
        boolean result = false;
        if(this == equalsObject) {
            result = true;
        } else if(equalsObject instanceof BlockChain) {
            final BlockChain equalsBlockChain = (BlockChain) equalsObject;
            if(equalsBlockChain.size() == this.size()) {
                result = containsAll(equalsBlockChain) && equalsBlockChain.containsAll(this);
            }
        }
        return result;
    }

    /**
     * Create a block designed to be the first block of the blockchain and
     * returns it.
     *
     * @return a block designed to be the first block of the blockchain
     */
    protected static Block createGenesisBlock()  {
        final Block genesisBlock = new Block(new byte[0], 0);
        final PublicKey masterPublicKey = Cryptography.createPublicKeyFromBytes(Parameters.MASTER_WALLET_PUBLIC_KEY);
        final Address masterWallet = new Address(masterPublicKey);
        final Timestamp genesisTime = new Timestamp(0L);
        final Transaction reward = new Transaction(masterWallet, genesisTime);
        reward.setSignature(Parameters.GENESIS_SIGNATURE);
        genesisBlock.add(reward);
        return genesisBlock;
    }
}
