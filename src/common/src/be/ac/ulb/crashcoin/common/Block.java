package be.ac.ulb.crashcoin.common;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import org.json.JSONObject;

/**
 * Block that compose BlockChain
 */
public class Block implements JSONable {
    
    private ArrayList<Transaction> transactions;
    private Long nonce = 0L;
    
    public Block() {
        super();
        this.transactions = new ArrayList<>();
        //TODO
    }
    
    /** Create Address instance from a JSON representation **/
    public Block(JSONObject json) {
        this(); //TODO pass json values as parameters to Block() ctr
    }
    
    /** Get a JSON representation of the Block instance **/
    public JSONObject toJSON() {
        //TODO
        return new JSONObject();
    }
    
    public byte[] hash() throws NoSuchAlgorithmException {
        MessageDigest sha = MessageDigest.getInstance(Parameters.MINING_HASH_ALGORITHM);
        sha.update(toBytes());
        return sha.digest();
    }
    
    public byte[] toBytes() {
        ByteBuffer buffer = ByteBuffer.allocate(Transaction.getSize() * this.transactions.size()
                +  Parameters.NONCE_N_BYTES);
        for(final Transaction transaction : this.transactions)
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
    public boolean equals(Object obj) {
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
