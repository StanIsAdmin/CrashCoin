package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Block that compose BlockChain.
 *
 * Shape of the block: + 4 byte long of magic number to mark the beginning of a
 * block + 4 byte long giving the block size in bytes + 32 byte (256 bit) long
 * hash of the previous block + 32 byte long merkle root + 4 byte long timestamp
 * TODO not add (only if needed) + 4 byte long difficulty (expected nb of 0s at
 * the beginning of the hash) + 4 byte long Nonce + variable size(?):
 * Transaction list
 */
public class Block extends ArrayList<Transaction> implements JSONable {

    private Integer nonce = 0;
    private final byte[] previousBlock;
    private final int difficulty;
    private byte[] merkleRoot;
    private MerkleTree merkleTree; // Not included in header

    public Block(final byte[] previousBlock, final int difficulty) {
        this(previousBlock, difficulty, 0, null);
    }

    public Block(final byte[] previousBlock, final int difficulty, final int nonce, final byte[] merkleRoot) {
        super();
        this.previousBlock = previousBlock;
        this.difficulty = difficulty;
        this.nonce = nonce;
        this.merkleRoot = merkleRoot;
    }

    /**
     * Create Block instance from a JSON representation
     *
     * @param json
     */
    public Block(final JSONObject json) {
        this(
            JsonUtils.decodeBytes(json.getString("previousBlock")), 
            json.getInt("difficulty"), 
            json.getInt("nonce"),
            JsonUtils.decodeBytes(json.getString("merkleRoot")));
        final JSONArray transactionsArray = json.getJSONArray("listTransactions");

        for (int i = 0; i < transactionsArray.length(); ++i) {
            final Object type = transactionsArray.get(i);
            if (type instanceof JSONObject) {
                this.add(new Transaction((JSONObject) type));
            } else {
                throw new IllegalArgumentException("Unknow object in listTransactions ! " + type);
            }
        }
    }

    @Override
    public boolean add(final Transaction transaction) {
        boolean res = false;
        if (this.size() < Parameters.NB_TRANSACTIONS_PER_BLOCK) {
            res = super.add(transaction);
        }
        // Update Merkle root
        this.merkleTree = new MerkleTree(this);
        this.merkleRoot = merkleTree.getRoot();
        return res;
    }

    /**
     * Get a JSON representation of the Block instance *
     */
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("previousBlock", JsonUtils.encodeBytes(previousBlock));
        json.put("difficulty", difficulty);
        json.put("nonce", nonce);
        json.put("merkleRoot", JsonUtils.encodeBytes(merkleRoot));

        try {
            final JSONArray jArray = new JSONArray();
            for (final Transaction trans : this) {
                jArray.put(trans.toJSON());
            }
            json.put("listTransactions", jArray);
        } catch (JSONException jse) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, jse);
        }
        return json;
    }
    
    /**
     * Checks that a transaction is indeed present inside of the block,
     * given its hash. Then, if a transaction hash is matched, it returns the
     * corresponding transaction. Otherwise it returns null.
     * 
     * @param hashed  Transaction hash value
     * @return  Whether it is present in the blok or not
     */
    public Transaction findTransaction(byte[] hashed) {
        for (Transaction transaction: this) {
            if (Arrays.equals(transaction.toBytes(), hashed)) {
                return transaction;
            }
        }
        return null;
    }

    /**
     * Checks if a hash satisfies the difficulty
     *
     * @return true if the hash starts with the right amount of null bits and
     * false otherwise
     * @see isValid
     */
    public boolean isHashValid()  {
        return isHashValid(Cryptography.hashBytes(headerToBytes()), this.difficulty);
    }

    /**
     * Checks if a hash satisfies the difficulty
     *
     * @param hash The hash of a transaction to test
     * @param difficulty The number of null bits that are required
     * @return true if the hash starts with the right amount of null bits and
     * false otherwise
     * @see Parameters.MINING_DIFFICULTY
     */
    public boolean isHashValid(final byte[] hash, Integer difficulty) {
        if (hash == null || difficulty > Byte.SIZE * hash.length) {
            return false;
        }
        BitSet bitset = BitSet.valueOf(hash);
        Integer indexOfLastZero = bitset.nextSetBit(0) - 1;
        return (indexOfLastZero >= difficulty);
    }

    /**
     * Set the header into a single byte array.
     *
     * @return a byte[] representing the header
     * @throws NoSuchAlgorithmException if unable to hash
     */
    public byte[] headerToBytes()  {
        final ByteBuffer buffer = ByteBuffer.allocate(Parameters.BLOCK_HEADER_SIZE);
        // insert magic number (4 bytes)
        buffer.putInt(Parameters.MAGIC_NUMBER);
        // insert reference to previous block (32 bytes)
        buffer.put(previousBlock);
        // insert Merkle root
        buffer.put(merkleRoot); // buffer.put(getTransactionHash());
        // insert difficulty (4 bytes)
        buffer.putInt(difficulty);
        // No need to store the number of transactions in byte representation
        // insert nonce (4 bytes)
        buffer.putInt(nonce);
        return buffer.array();
    }

    /**
     * Get hash of all transaction
     *
     * @return 32 bytes !
     * @throws java.security.NoSuchAlgorithmException
     */
    public byte[] getTransactionHash()  {
        return Cryptography.hashBytes(this.transactionsToBytes());
    }

    /**
     * Set the transactions list in a single byte array.
     *
     * @return a byte[] representing the transactions list
     */
    private byte[] transactionsToBytes() {
        final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        for (final Transaction transaction : this) {
            try {
                //buffer.
                buffer.write(transaction.toBytes());
            } catch (IOException ex) {
                Logger.getLogger(Block.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        return buffer.toByteArray();
    }

    /**
     * Set the whole block in a single byte array.
     *
     * @return a byte[] representing the block
     * @throws NoSuchAlgorithmException if unable to perform hashing
     */
    public byte[] toBytes()  {
        final byte[] headerBytes = headerToBytes();
        final byte[] transactionBytes = transactionsToBytes();
        final ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + transactionBytes.length);
        buffer.put(headerBytes);
        buffer.put(transactionBytes);
        return buffer.array();
    }

    /**
     * Changes the nonce of the block. Should only be called when mining!
     *
     * @param nonce The new nonce to set
     */
    public void setNonce(final Integer nonce) {
        this.nonce = nonce;
    }

    public int getDifficulty() {
        return difficulty;
    }

    /**
     * Get the hash of previous block
     *
     * @return The bytes that represent the hash of the previous block
     */
    public byte[] getPreviousBlock() {
        return previousBlock;
    }

    /**
     * Used for test purposes *
     */
    @Override
    public boolean equals(final Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Block other = (Block) obj;
        if (this.difficulty != other.difficulty) {
            return false;
        }
        return containsAll(other) && other.containsAll(this);
    }

}
