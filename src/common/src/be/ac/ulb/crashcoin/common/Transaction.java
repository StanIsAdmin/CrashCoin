package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
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
    
    private ArrayList<TransactionInput> inputs = null;
    private TransactionOutput transactionOutput;
    /**
     * Output of the transaction that contains the remaining value after payement.
     * 
     * The amount of the output is 0 if not all crashcoins have been used,
     * strictly positive if there is change, and the output is null if the
     * transaction is self-rewarding.
     */
    private TransactionOutput changeOutput;
    
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
     */    
    public Transaction(final Address destAddress, final Timestamp lockTime)  {
        // Add lock time
        this.lockTime = lockTime;
        
        transactionOutput = new TransactionOutput(destAddress, Parameters.MINING_REWARD);
        changeOutput = null;
    }
    
    /**
     * Creates a transaction without a signature.
     * 
     * @param srcAddress Source address of the transaction
     * @param destAddress Destination address of the transaction
     * @param amount Number of CrashCoins received by the destination address
     * @param referencedOutputs List of referenced outputs, used as inputs
     */
    public Transaction(final Address srcAddress, final Address destAddress, final Integer amount, 
            final List<TransactionOutput> referencedOutputs)  {
        // Add lock time
        this.lockTime = new Timestamp(System.currentTimeMillis());

        // Create inputs
        this.inputs = new ArrayList<>();
        Integer inputAmount = 0;
        for (final TransactionOutput output : referencedOutputs) {
            inputs.add(new TransactionInput(output));
            inputAmount += output.getAmount();
        }
        
        // Create outputs
        this.transactionOutput = new TransactionOutput(destAddress, amount);
        this.changeOutput = new TransactionOutput(srcAddress, inputAmount - amount);
        
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
        if(!json.getBoolean("isReward")) {
            // Add inputs
            this.inputs = new ArrayList<>();
            for (final Object input : json.getJSONArray("inputs")) {
                this.inputs.add(new TransactionInput((JSONObject) input));
            }
            
            // change output is present only for non-reward transactions
            this.changeOutput = new TransactionOutput((JSONObject) json.get("changeOutput"));
        }
        
        // Add transaction output
        this.transactionOutput = new TransactionOutput((JSONObject) json.get("transactionOutput"));
        
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
        json.put("isReward", isReward());
        
        if(!isReward()) {
            final JSONArray jsonInputs = new JSONArray();
            for (final TransactionInput input : inputs) {
                jsonInputs.put(input.toJSON());
            }
            json.put("inputs", jsonInputs);
            json.put("changeOutput", this.changeOutput.toJSON());
        }
        json.put("transactionOutput", this.transactionOutput.toJSON());
        json.put("signature", JsonUtils.encodeBytes(signature));
        return json;
    }

    public void sign(final PrivateKey privateKey) {
        this.signature = Cryptography.signTransaction(privateKey, this.toBytes());
    }
    
    // Checks that the transaction is older that the other transaction
    public boolean before(final Transaction other) {
        return lockTime.before(other.lockTime);
    }
    
    public ArrayList<TransactionInput> getInputTransactions() {
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
        for(final TransactionInput input : this.inputs) {
            sum += input.getAmount();
        }
        return (this.transactionOutput.getAmount() > 0 && this.changeOutput.getAmount() >= 0)
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
            if(!isReward()) {
                for(final TransactionInput input : inputs)
                    byteBuffer.write(input.toBytes());
                byteBuffer.write(this.changeOutput.toBytes());
            }
            byteBuffer.write(this.transactionOutput.toBytes());
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

    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }
    
    public TransactionOutput getTransactionOutput() {
        return this.transactionOutput;
    }
    
    public TransactionOutput getChangeOutput() {
        return this.changeOutput;
    }

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
