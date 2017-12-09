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

    /**
     * Creates a BlockChain which contains only the genesis block.
     *
     * The genesis block is created through the createGenesisBlock() method.
     * @see #createGenesisBlock
     */
    public BlockChain()  {
        this.availableInputs = new HashMap<>();
        final Block genesis = createGenesisBlock();
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

    /**
     * Verifies the block, add it to the end of the blockChain if it is valid
     * and update the set of transactions available as inputs.
     *
     * @param block
     * @return whether the block was added to the blockchain.
     */
    @Override
    public final boolean add(final Block block) {
        boolean valid = isValidNextBlock(block, Parameters.MINING_DIFFICULTY);
        if (valid) {
            super.add(block);
            updateAvailableInputs(block);
        } else {
            Logger.getLogger(BlockChain.class.getName()).log(Level.WARNING, "Invalid block discarded");
        }
        return valid;
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
    private synchronized void updateAvailableInputs(final Block addedBlock)  {
        for (final Transaction addedTransaction : addedBlock) {

            for (final TransactionInput usedInput : addedTransaction.getInputs()) {
                this.availableInputs.remove(usedInput.toBytes());
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
        for (final Transaction transaction: block) {
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
     * - have only previously-unused inputs that belong to the sender
     *
     * @see Transaction.isValid
     * @param transaction
     * @return true if the transaction is valid as defined, false otherwise
     */
    private boolean isValidTransaction(final Transaction transaction)  {
        // Verify the transaction value and signature (if necessary)
        if (! transaction.isValid())
            return false;

        if(!transaction.isReward()) {
            // Verify that each input is available and belongs to the sender
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

    // TODO Should it be moved to Block?
    // Used by [master node]
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
        boolean result = block.isHashValid() // check that the hash corresponds the indicated difficulty
                && difficulty == block.getDifficulty() // check that the indicated difficulty corresponds to the required difficulty
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
