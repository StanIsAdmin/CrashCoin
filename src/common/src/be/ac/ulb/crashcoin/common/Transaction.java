package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Objects;
import org.json.JSONObject;

public class Transaction implements JSONable {

    private static Integer TRANSACTION_SIZE = 16;  // TODO: change this!
    
    private final Address srcAddress;
    private final Integer totalAmount;
    private final Timestamp lockTime;
    private ArrayList<Input> inputs;
    private ArrayList<Output> outputs;
    
    /**
     * Get the size of a single transaction in bytes
     * 
     * @return the size in butes of a transaction
     */
    public static Integer getSize() {
        return TRANSACTION_SIZE;
    }

    /**
     * Constructor for transactions
     * Transaction
     * @param srcAddress CrashCoin address of the source
     * @param totalAmount Number of CrashCoins
     * @param lockTime Transaction timestamp
     */
    public Transaction(final Address srcAddress, final Integer totalAmount, final Timestamp lockTime) {
        super();
        this.srcAddress = srcAddress;
        this.totalAmount = totalAmount;
        this.lockTime = lockTime;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
    }
    
    /** Create Transaction instance from a JSON representation
     * @param json 
     */
    public Transaction(final JSONObject json) {
        this(new Address((JSONObject) json.get("srcAddress")),
                json.getInt("totalAmount"),
                new Timestamp(json.getLong("lockTime")));
    }
    
    /** Get a JSON representation of the Address instance **/
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("srcAddress", srcAddress.toJSON());
        json.put("totalAmount", totalAmount);
        json.put("lockTime", lockTime.getTime());
        return json;
    }
    
    // Create a new transaction to a final destinator
    public boolean createTransaction(final Transaction transaction, 
            final Address dstAddress, final Integer nCrashCoins) throws NoSuchAlgorithmException {
        this.addInputTransaction(transaction);
        this.addOutput(dstAddress, nCrashCoins);
        return this.isValid();
    }
    
    public void addInputTransaction(final Transaction transaction) throws NoSuchAlgorithmException {
        this.inputs.add(new Input(transaction));
    }
    
    public void addOutput(final Address address, final Integer nCrashCoins) {
        this.outputs.add(new Output(address, nCrashCoins));
    }
    
    private boolean isValid() {
        // Check whether sum of inputs is lower than the sum of outputs
        Integer sum = 0;
        for (Output output: this.outputs) {
            sum += output.nCrashCoins;
        }
        // The difference is considered as transaction fee
        return Objects.equals(sum, totalAmount);
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
        // TODO: convert inputs and outputs to bytes
        final byte[] srcAddressBytes = srcAddress.toBytes();
        final ByteBuffer buffer = ByteBuffer
                .allocate(srcAddressBytes.length + Parameters.INTEGER_N_BYTES);
        buffer.putInt(totalAmount);
        buffer.put(srcAddressBytes);
        return buffer.array();
    }

    /** 
     * String representation of a transaction
     * @return String
     */
    @Override
    public String toString() {
        return "src: " + srcAddress + " | amount: " + totalAmount;
    }
    
    /**
     * Input of a transaction, from the doc https://en.bitcoin.it/wiki/Transaction
     */
    public class Input {
        final byte[] previousTx; // Hash value of a previous transaction
        
        public Input(final Transaction previousTransaction) throws NoSuchAlgorithmException {
            this.previousTx = Cryptography.hashBytes(previousTransaction.toBytes());
        }
    }
    
    /**
     * Output of a transaction, from the doc https://en.bitcoin.it/wiki/Transaction
     */
    public class Output {
        final Integer nCrashCoins;
        final Address address;
        
        public Output(final Address address, final Integer nCrashCoins) {
            this.nCrashCoins = nCrashCoins;
            this.address = address;
        }
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
        final Transaction other = (Transaction) obj;
        return this.srcAddress.equals(other.srcAddress) 
                && this.totalAmount.equals(other.totalAmount) 
                && this.lockTime.equals(other.lockTime);
    }
}
