package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import org.json.JSONArray;
import org.json.JSONObject;

public class Transaction implements JSONable {

    private final Timestamp lockTime;
    
    private ArrayList<Input> inputs;
    private Output transactionOutput;
    private Output changeOutput;
    
    private byte[] signature;

    /**
     * Creates a reward transaction (transaction with no source address).
     * The resulting transaction is without signature.
     *
     * @param destAddress Address of the destination
     * @param amount Number of CrashCoins
     * @param lockTime Transaction timestamp
     * @param inputs List of referenced outputs, used as inputs
     * @throws java.security.NoSuchAlgorithmException
     */    
    public Transaction(final Address destAddress, final Integer amount, final Timestamp lockTime, final List<Output> inputs) throws NoSuchAlgorithmException {
        this(null, destAddress, amount, lockTime, inputs);
    }
    
    /**
     * Creates a transaction without a signature.
     * 
     * @param srcAddress Source address of the transaction
     * @param destAddress Destination address of the transaction
     * @param amount Number of CrashCoins received by the destination address
     * @param lockTime Time of the transaction creation
     * @param referencedOutputs List of referenced outputs, used as inputs
     * @throws java.security.NoSuchAlgorithmException
     */
    public Transaction(final Address srcAddress, final Address destAddress, final Integer amount, final Timestamp lockTime, final List<Output> referencedOutputs) throws NoSuchAlgorithmException {
        // Add lock time
        this.lockTime = lockTime;

        // Create inputs
        this.inputs = new ArrayList<>();
        Integer inputAmount = 0;
        for (final Output output : referencedOutputs) {
            inputs.add(new Input(output));
            inputAmount += output.getAmount();
        }
        
        // Create outputs
        this.transactionOutput = new Output(destAddress, amount);
        this.changeOutput = new Output(srcAddress, inputAmount - amount);
        
        // Instantiate signature
        this.signature = null;
    }

    /**
     * Create Transaction instance from a JSON representation.
     * This transaction already contains a signature.
     * 
     * @param json
     */
    public Transaction(final JSONObject json) {
        // Add lock time
        this.lockTime = new Timestamp(json.getLong("lockTime"));
        
        // Add inputs
        this.inputs = new ArrayList<>();
        for (Object input : json.getJSONArray("inputs")) {
            this.inputs.add(new Input((JSONObject) input));
        }
        
        // Add outputs
        this.transactionOutput = new Output((JSONObject) json.get("transactionOutput"));
        this.changeOutput = new Output((JSONObject) json.get("changeOutput"));
        
        // Add signature
        this.signature = JsonUtils.decodeBytes(json.getString("signature"));
    }

    /**
     * Returns a JSON representation of the Address instance.
     */
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("lockTime", lockTime.getTime());
        
        JSONArray jsonInputs = new JSONArray();
        for (final Input input : inputs) {
            jsonInputs.put(input.toJSON());
        }
        json.put("inputs", jsonInputs);
        json.put("transactionOutput", this.transactionOutput.toJSON());
        json.put("changeOutput", this.changeOutput.toJSON());
        json.put("signature", JsonUtils.encodeBytes(signature));
        return json;
    }

    public void sign(PrivateKey privateKey) {
        this.signature = Cryptography.signTransaction(privateKey, this.toBytes());
    }
    
    // Checks that the transaction is older that the other transaction
    public boolean before(final Transaction other) {
        return lockTime.before(other.lockTime);
    }
    
    public ArrayList<Input> getInputTransactions() {
        return this.inputs;
    }

    /** Returns true if the standalone transaction is valid, false otherwise.
     * A transaction by itself is valid if it meets all of these conditions :
     * - the sum of inputs equals the sum of outputs
     * - each output value is strictly positive
     * - TODO use input values instead of totalAmount ?
     * 
     * @return true if the transaction is valid as described, false otherwise
     */
    public boolean isValid() {
        // Check whether sum of inputs is lower than the sum of outputs
        Integer sum = 0;
        for (Output output : this.outputs) {
            if (output.amount <= 0) return false;
            sum += output.amount;
        }
        // The difference is considered as transaction fee
        return Objects.equals(sum, totalAmount);
    }

    /**
     * Creates a byte representation of a transaction. The attributes of the
     * transaction are converted to byte arrays and then concatenated. A
     * transaction requires a byte representation to be able to pass it to a
     * signature algorithm.
     *
     * @return Bytes of the transaction
     */
    public byte[] toBytes() {
        // Compute number of bytes required to represent inputs and outputs
        Integer totalSize = 0;
        for (Input input: inputs) {
            totalSize += input.toBytes().length;
        }
        for (Output output: outputs) {
            totalSize += output.toBytes().length;
        }
        totalSize += Parameters.INTEGER_N_BYTES;
        // Add number of crashcoins of current transaction and user's address
        byte[] srcAddressBytes;
        if (srcAddress != null) {
            srcAddressBytes = srcAddress.toBytes();
            totalSize += srcAddressBytes.length;
        }
        final ByteBuffer buffer = ByteBuffer
                .allocate(totalSize);
        buffer.putInt(totalAmount);
        if (srcAddress != null) {
            srcAddressBytes = srcAddress.toBytes();
            buffer.put(srcAddressBytes);
        }
        // Add inputs and outputs as bytes
        for (Input input: inputs) {
            buffer.put(input.toBytes());
        }
        for (Output output: outputs) {
            buffer.put(output.toBytes());
        }
        return buffer.array();
    }

    /**
     * String representation of a transaction
     *
     * @return String
     */
    @Override
    public String toString() {
        return "src: " + srcAddress + " | amount: " + totalAmount;
    }

    /**
     * Input of a transaction, from the doc
     * https://en.bitcoin.it/wiki/Transaction
     */
    public class Input {

        final byte[] previousOutputHash; // Hash value of a previous transaction
        
        final Integer previousOutputAmount; // Amount of the previous output

        public Input(final Output output) throws NoSuchAlgorithmException {
            this.previousOutputHash = Cryptography.hashBytes(output.toBytes());
            this.previousOutputAmount = output.getAmount();
        }
                
        /**
         * Byte representation of a transaction input, which is simply the hash
         * of the input transaction.
         * 
         * @return byte[]  Byte representation of the input
         */
        public byte[] toBytes() {
            return previousOutputHash;
        }
        
        @Override
        public boolean equals(final Object obj) {
            if (this == obj) return true;
            if (obj == null) return false;
            if (getClass() != obj.getClass()) return false;
            Input other = (Input) obj;
            return Arrays.equals(this.previousOutputHash, other.previousOutputHash);
        }
        
        @Override
        public int hashCode() {
            return this.previousOutputHash.hashCode();
        }
    }

    /**
     * Return the address of the payee.
     *
     * @return
     */
    public Address getDestAddress() {
        Address ret = null;
        for (final Output output : outputs) {
            if (output.getAddress().equals(srcAddress)) {
                ret = output.getAddress();
            }
        }
        return ret;
    }

    /**
     * Return the address of the payer.
     *
     * @return
     */
    public Address getSrcAddress() {
        return this.srcAddress;
    }

    /**
     * Output of a transaction, from the doc
     * https://en.bitcoin.it/wiki/Transaction
     */
    public class Output {

        final Integer amount;
        final Address address;

        public Output(final Address address, final Integer nCrashCoins) {
            this.amount = nCrashCoins;
            this.address = address;
        }
        
        public Integer getAmount() {
            return this.amount;
        }

        public Address getAddress() {
            return this.address;
        }
        
        /**
         * Byte representation of a transaction output, which relies on
         * the amount of CrashCoins and the receiver's address.
         * 
         * @return byte[] Byte representation
         */
        public byte[] toBytes() {
            final ByteBuffer buffer = ByteBuffer
                .allocate(address.toBytes().length + Parameters.INTEGER_N_BYTES);
            buffer.putInt(amount);
            buffer.put(address.toBytes());
            return buffer.array();
        }
    }
    
    public ArrayList<Input> getInputs() {
        return inputs;
    }
    
    public Output getTransactionOutput() {
        return this.transactionOutput;
    }
    
    public Output getChangeOutput() {
        return this.changeOutput;
    }

    /**
     * Used for test purposes *
     */
    @Override
    public boolean equals(final Object obj) {
        Boolean res = true;
        if (this == obj) {
            res = true;
        }
        if (obj == null) {
            res = false;
        }
        if (getClass() != obj.getClass()) {
            res = false;
        }
        final Transaction other = (Transaction) obj;
        res &= this.totalAmount.equals(other.totalAmount) && this.lockTime.equals(other.lockTime) && this.destAddress.equals(other.destAddress);
        if(this.srcAddress != null && other.srcAddress != null) {
            res &= this.srcAddress.equals(other.srcAddress);
        }
        return res;
    }
}
