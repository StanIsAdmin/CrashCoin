package be.ac.ulb.crashcoin.common;

import java.nio.ByteBuffer;
import org.json.JSONObject;

public class Transaction extends JSONable {

    private final Address srcAddress;
    private final Address destAddress;
    private final Integer amount;

    /**
     * Constructor for transactions
     * 
     * @param srcAddress
     *            CrashCoin address of the source
     * @param destAddress
     *            CrashCoin address of the destination
     * @param amount
     *            Number of CrashCoins
     */
    public Transaction(Address srcAddress, Address destAddress, Integer amount) {
        super();
        this.srcAddress = srcAddress;
        this.destAddress = destAddress;
        this.amount = amount;
    }
    
    /** Create Transaction instance from a JSON representation **/
    public Transaction(JSONObject json) {
        this(new Address((JSONObject) json.get("srcAddress")), 
                new Address((JSONObject) json.get("destAddress")),
                (Integer) json.get("amount"));
    }
    
    /** Get a JSON representation of the Address instance **/
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("srcAddress", srcAddress.toJSON());
        json.put("destAddress", destAddress.toJSON());
        json.put("amount", amount);
        return json;
    }

    /**
     * Creates a byte representation of a transaction. The attributes of the
     * transaction are converted to byte arrays and then concatenated. A transaction
     * requires a byte representation to be able to pass it to a signature
     * algorithm.
     * 
     * @return Bytes of the transaction
     */
    public byte[] toBytes() {
        byte[] srcAddressBytes = srcAddress.toBytes();
        byte[] destAddressBytes = destAddress.toBytes();
        ByteBuffer buffer = ByteBuffer
                .allocate(srcAddressBytes.length + destAddressBytes.length + Parameters.INTEGER_N_BYTES);
        buffer.putInt(amount);
        buffer.put(srcAddressBytes);
        buffer.put(destAddressBytes);
        return buffer.array();
    }

    /** String representation of a transaction */
    public String toString() {
        return "src: " + srcAddress + " | dest: " + destAddress + " | amount: " + amount;
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
        final Transaction other = (Transaction) obj;
        return this.srcAddress.equals(other.srcAddress) 
                && this.destAddress.equals(other.destAddress) 
                && this.amount.equals(other.amount);
    }
}
