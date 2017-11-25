package be.ac.ulb.crashcoin.common;

import java.security.NoSuchAlgorithmException;
import java.security.MessageDigest;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Block that compose BlockChain.
 * 
 * Shape of the block:
 * + 4 byte long of magic number to mark the beginning of a block
 * + 4 byte long giving the block size in bytes
 * + 32 byte (256 bit) long hash of the previous block
 * + 32 byte long merkle root
 * + 4 byte long timestamp
 * + 4 byte long difficulty (expected nb of 0s at the beginning of the hash)
 * + 4 byte long Nonce
 * + variable size(?): Transaction list
 */
public class Block extends ArrayList<Transaction> implements JSONable {
    
    private Long nonce = 0L;
    
    public Block() {
        super();
        //TODO
    }
    
    /** 
     * Create Address instance from a JSON representation
     * @param json 
     */
    public Block(final JSONObject json) {
        this(); //TODO pass json values as parameters to Block() ctr
    }
    
    @Override
    public boolean add(final Transaction transaction) {
        boolean res = false;
        if(this.size() < Parameters.NB_TRANSACTIONS_PER_BLOCK) {
            res = super.add(transaction);
        }
        return res;
    }
    
    /** Get a JSON representation of the Block instance **/
    @Override
    public JSONObject toJSON() {
        final JSONObject jObject = new JSONObject();
        try {
            final JSONArray jArray = new JSONArray();
            for(final Transaction trans : this) {
                jArray.put(trans.toJSON());
            }
            jObject.put("block", jArray);
        } catch (JSONException jse) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, jse);
        }
        return jObject;
    }
    
    public byte[] hash() throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance(Parameters.MINING_HASH_ALGORITHM);
        sha.update(toBytes());
        return sha.digest();
    }
    
    public byte[] hashHeader() throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance(Parameters.MINING_HASH_ALGORITHM);
        sha.update(headerToBytes());
        return sha.digest();
    }
    
    /**
     * Set the header into a single byte array.
     * 
     * @return a byte[] representing the header
     * @throws NoSuchAlgorithmException if unable to hash
     */
    public byte[] headerToBytes() throws NoSuchAlgorithmException {
        ByteBuffer buffer = ByteBuffer.allocate(Parameters.BLOCK_HEADER_SIZE);
        // insert magic number (4 bytes)
        buffer.putLong(Parameters.MAGIC_NUMBER);
        // insert block size (4 bytes)
        buffer.putLong(getTotalSize());
        // TODO: complete the following lines starting with '/////'
        // insert reference to previous block (32 bytes)
        ///// buffer.put( HASH OF PREVIOUS BLOCK )
        // insert merkle root
        ////// for(final Transaction transaction : this)
        //////    buffer.put(transaction.hash());
        // insert timestamp (4 bytes)
        ///// ...
        buffer.putLong(nonce);
        return buffer.array();
    }
    
    /**
     * Set the transactions list in a single byte array.
     * 
     * @return a byte[] representing the transactions list
     */
    public byte[] transactionsToBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(Transaction.getSize() * this.size()
                +  Parameters.NONCE_N_BYTES);
        for(final Transaction transaction : this)
            buffer.put(transaction.toBytes());
        for(int i = 0; i < Parameters.NONCE_N_BYTES; ++i)
            buffer.put((byte)((this.nonce & (0xFF << Byte.SIZE * i)) >> (Byte.SIZE * i)));
        return buffer.array();
    }
    
    /**
     * Set the whole block in a single byte array.
     * 
     * @return a byte[] representing the block
     * @throws NoSuchAlgorithmException if unable to perform hashing
     */
    public byte[] toBytes() throws NoSuchAlgorithmException {
        byte[] headerBytes = headerToBytes();
        byte[] transactionBytes = transactionsToBytes();
        ByteBuffer buffer = ByteBuffer.allocate(headerBytes.length + transactionBytes.length);
        buffer.put(headerBytes);
        buffer.put(transactionBytes);
        return buffer.array();
    }
    
    private Long getTotalSize() {
        return Parameters.BLOCK_HEADER_SIZE + (long)this.size()*Transaction.getSize();  
    }
    
    /**
     * Changes the nonce of the block. Should only be called when mining!
     * 
     * @param nonce The new nonce to set
     */
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }

    /** Used for test purposes **/
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
        return true; //TODO compare attributes
    }
    
    
}
