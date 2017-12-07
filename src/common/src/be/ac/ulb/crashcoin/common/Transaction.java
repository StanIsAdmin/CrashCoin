package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

public class Transaction implements JSONable {

    private final Timestamp lockTime;
    
    private ArrayList<Input> inputs = null;
    private Output transactionOutput;
    /**
     * Output of the transaction that contains the remaining value after payement.
     * 
     * The amount of the output is 0 if not all crashcoins have been used,
     * strictly positive if there is change, and the output is null if the
     * transaction is self-rewarding.
     */
    private Output changeOutput;
    
    private byte[] signature = null;

    public Transaction(final Address destAddress)  {
        this(destAddress, new Timestamp(System.currentTimeMillis()));
    }
    
    /**
     * Creates a reward transaction (transaction with no source address).
     * The resulting transaction is without signature.
     * 
     * @see Parameters.MINING_REWRD
     *
     * @param lockTime time of the creation of the transaction (if null: lockTime
     *      is considered to be current timestamp)
     * @param destAddress Address of the destination
     * @throws java.security.NoSuchAlgorithmException
     */    
    public Transaction(final Address destAddress, final Timestamp lockTime)  {
        // Add lock time
        this.lockTime = lockTime;
        
        transactionOutput = new Output(destAddress, Parameters.MINING_REWARD);
        changeOutput = null;
    }
    
    /**
     * Creates a transaction without a signature.
     * 
     * @param srcAddress Source address of the transaction
     * @param destAddress Destination address of the transaction
     * @param amount Number of CrashCoins received by the destination address
     * @param referencedOutputs List of referenced outputs, used as inputs
     * @throws java.security.NoSuchAlgorithmException
     */
    public Transaction(final Address srcAddress, final Address destAddress, final Integer amount, final List<Output> referencedOutputs)  {
        // Add lock time
        this.lockTime = new Timestamp(System.currentTimeMillis());

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
        if(isReward())
            return this.inputs == null && this.changeOutput == null
                    && this.transactionOutput.getAmount().equals(Parameters.MINING_REWARD);
        // Check whether sum of inputs is equal to the sum of outputs
        Integer sum = 0;
        for(final Input input : this.inputs) {
            sum += input.getAmount();
        }
        return (this.transactionOutput.getAmount() >= 0 && this.changeOutput.getAmount() >= 0)
                && sum == (this.transactionOutput.getAmount() + this.changeOutput.getAmount());
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
        final ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        try {    
            byteBuffer.write(ByteBuffer.allocate(Long.BYTES).putLong(lockTime.getTime()).array());
            for(final Input input : inputs)
                byteBuffer.write(input.toBytes());
            byteBuffer.write(this.transactionOutput.toBytes());
            byteBuffer.write(this.changeOutput.toBytes());
            byteBuffer.write(this.signature);
        } catch (IOException ex) {
            Logger.getLogger(Transaction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return byteBuffer.toByteArray();
    }
    
    public boolean isReward() {
        return this.changeOutput == null;
    }

    /**
     * Return the address of the payee.
     *
     * @return
     */
    public Address getDestAddress() {
        return this.transactionOutput.getDestinationAddress();
    }

    /**
     * Return the address of the payer.
     *
     * @return
     */
    public Address getSrcAddress() {
        return this.changeOutput.getDestinationAddress();
    }

    /**
     * Input of a transaction, from the doc
     * https://en.bitcoin.it/wiki/Transaction
     */
    public class Input {

        private final byte[] previousOutputHash; // Hash value of a previous transaction
        
        private final Integer previousOutputAmount; // Amount of the previous output

        public Input(final Output output)  {
            this.previousOutputHash = Cryptography.hashBytes(output.toBytes());
            this.previousOutputAmount = output.getAmount();
        }
        
        public Input(final JSONObject json) {
            this.previousOutputHash = JsonUtils.decodeBytes(json.getString("previousOutputHash"));
            this.previousOutputAmount = json.getInt("previousOutputAmount");
        }
        
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("previousOutputHash", this.previousOutputHash);
            json.put("previousOutputAmount", this.previousOutputAmount);
            return json;
        }
        
        public Integer getAmount() {
            return this.previousOutputAmount;
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
    }
    
    /**
     * Output of a transaction, from the doc
     * https://en.bitcoin.it/wiki/Transaction
     */
    public class Output {

        private final Integer amount;
        private final Address address;

        public Output(final Address address, final Integer nCrashCoins) {
            this.amount = nCrashCoins;
            this.address = address;
        }
        
        public Output(JSONObject json) {
            this.address = new Address(json.getJSONObject("address"));
            this.amount = json.getInt("amount");
        }
        
        public JSONObject toJSON() {
            JSONObject json = new JSONObject();
            json.put("address", this.address);
            json.put("amount", this.amount);
            return json;
        }
        
        public Integer getAmount() {
            return this.amount;
        }

        public Address getDestinationAddress() {
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
        if (!Objects.equals(this.lockTime, other.lockTime)) {
            return false;
        }
        if (!Objects.equals(this.inputs, other.inputs)) {
            return false;
        }
        if (!Objects.equals(this.transactionOutput, other.transactionOutput)) {
            return false;
        }
        if (!Objects.equals(this.changeOutput, other.changeOutput)) {
            return false;
        }
        if (!Arrays.equals(this.signature, other.signature)) {
            return false;
        }
        return true;
    }
}
