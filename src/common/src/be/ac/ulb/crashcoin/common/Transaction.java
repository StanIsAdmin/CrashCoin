package be.ac.ulb.crashcoin.common;

import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.Timestamp;
import java.util.ArrayList;

public class Transaction {

    private final Address srcAddress;
    private final Integer totalAmount;
    private final Timestamp lockTime;
    private ArrayList<Input> inputs;
    private ArrayList<Output> outputs;
    private Long nonce;  

    /**
     * Constructor for transactions
     * 
     * @param srcAddress CrashCoin address of the source
     * @param totalAmount Number of CrashCoins
     * @param lockTime Transaction timestamp
     */
    public Transaction(final Address srcAddress, final Integer totalAmount, final Timestamp lockTime) {
        this.srcAddress = srcAddress;
        this.totalAmount = totalAmount;
        this.lockTime = lockTime;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
        this.nonce = 0L;
    }
    
    public void addInputTransaction(final Transaction transaction) throws NoSuchAlgorithmException {
        this.inputs.add(new Input(transaction));
    }
    
    public void addOutput(final Address address, final Integer nCrashCoins) {
        this.outputs.add(new Output(address, nCrashCoins));
    }
    
    /**
     * Performs SHA-256 hash of the transaction
     * 
     * @return A 32 byte long byte[] with the SHA-256 of the transaction
     * @throws NoSuchAlgorithmException if the machine is unable to perform SHA-256
     */
    public byte[] hash() throws NoSuchAlgorithmException {
        MessageDigest sha256 = MessageDigest.getInstance(Parameters.MINING_HASH_ALGORITHM);
        sha256.update(toBytes());
        return sha256.digest();
    }
    
    /**
     * Changes the nonce of the transaction. Should only be called when mining!
     * 
     * @param nonce The new nonce to set
     */
    public void setNonce(Long nonce) {
        this.nonce = nonce;
    }
    
    public boolean isValid() {
        // TODO: check whether sum of inputs is lower than the sum of outputs
        // The difference is considered as transaction fee
        return false; // TODO
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
                .allocate(srcAddressBytes.length + Parameters.INTEGER_N_BYTES
                        + Parameters.NONCE_N_BYTES);
        buffer.putInt(totalAmount);
        buffer.put(srcAddressBytes);
        for(int i = 0; i < Parameters.NONCE_N_BYTES; ++i)
            buffer.put((byte)(this.nonce & (0xFF << i)));
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
        
        public Input(Transaction previousTransaction) throws NoSuchAlgorithmException {
            this.previousTx = previousTransaction.hash();
        }
    }
    
    /**
     * Output of a transaction, from the doc https://en.bitcoin.it/wiki/Transaction
     */
    public class Output {
        
        final Integer nCrashCoins;
        final Address address;
        
        public Output(Address address, Integer nCrashCoins) {
            this.nCrashCoins = nCrashCoins;
            this.address = address;
        }
    }
}
