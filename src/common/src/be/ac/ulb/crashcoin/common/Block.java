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
 * Block that compose BlockChain
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
    
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(Transaction.getSize() * this.size()
                +  Parameters.NONCE_N_BYTES);
        for(final Transaction transaction : this)
            buffer.put(transaction.toBytes());
        for(int i = 0; i < Parameters.NONCE_N_BYTES; ++i)
            buffer.put((byte)((this.nonce & (0xFF << Byte.SIZE * i)) >> (Byte.SIZE * i)));
        return buffer.array();
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
