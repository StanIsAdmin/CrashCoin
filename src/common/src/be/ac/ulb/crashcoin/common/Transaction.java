package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.security.PrivateKey;
import java.security.PublicKey;
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
    private final TransactionOutput transactionOutput;
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
     * @see Parameters#MINING_REWARD
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
            
            // Add signature
            this.signature = JsonUtils.decodeBytes(json.getString("signature"));
        }

        // Add transaction output
        this.transactionOutput = new TransactionOutput((JSONObject) json.get("transactionOutput"));
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
            json.put("signature", JsonUtils.encodeBytes(signature));
        }
        json.put("transactionOutput", this.transactionOutput.toJSON());
        return json;
    }

    /**
     * Add signature of the payer to the transaction.
     *
     * Transactions signature is performed by DSA.
     *
     * @see Cryptography#signData(java.security.PrivateKey, byte[])
     *
     * @param privateKey the private key of the payer (signer)
     */
    public void sign(final PrivateKey privateKey) {
        this.signature = Cryptography.signData(privateKey, this.toBytes());
    }

    /**
     * Define the signature. <b>Only for genesis block</b>
     *
     * @param signature byte of the signature
     */
    public void setSignature(final byte[] signature) {
        this.signature = signature;
    }

    /**
     * Checks that the transaction is older that the other transaction
     *
     * @param other the transaction which timestamp is tested
     *
     * @return true if this very transaction is older that the other one.
     */
    public boolean before(final Transaction other) {
        return lockTime.before(other.lockTime);
    }
    

    public boolean isReward() {
        return this.changeOutput == null;
    }
    
    /**
     * Returns true if the transaction is a valid mining reward, false otherwise.
     * 
     * A mining reward is valid if it meets all of these conditions :<br>
     * - it is a mining reward (the change output is null)<br>
     * - the input array is null<br>
     * - the amount equals Parameters.MINING_REWARD
     * @see Parameters#MINING_REWARD
     * @return true if the transaction is valid as described, false otherwise
     */
    public boolean isValidReward() {
        return this.isReward()
                && this.inputs == null
                && this.transactionOutput.getAmount().equals(Parameters.MINING_REWARD);
    }

    /**
     * Returns true if the standalone transaction is valid and is not a mining
     * reward, false otherwise.
     *
     * A non-reward transaction is valid if it meets all of these conditions :<br>
     * - the sum of inputs equals the sum of outputs<br>
     * - each output value is strictly positive<br>
     * - the transaction data is digitally signed by the sender<br>
     *
     * @return true if the transaction is valid as described, false otherwise
     */
    public boolean isValidNonReward() {
        // Verify the digital signature with the sender's Public Key
        final PublicKey senderPublicKey = this.getSrcAddress().getPublicKey();
        if (! Cryptography.verifySignature(senderPublicKey, this.toBytes(), this.signature)) {
            Logger.getLogger(getClass().getName()).warning("Error with Signature");
            return false;
        }

        // Check whether sum of inputs is equal to the sum of outputs
        Integer inputSum = 0;
        for(final TransactionInput input : this.inputs) {
            inputSum += input.getAmount();
        }
        return this.transactionOutput.getAmount() > 0 
                && this.changeOutput.getAmount() >= 0
                && inputSum == (this.transactionOutput.getAmount() + this.changeOutput.getAmount());
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
                    byteBuffer.write(input.getHashBytes());
                byteBuffer.write(this.changeOutput.toBytes());
            }
            byteBuffer.write(this.transactionOutput.toBytes());
        } catch (IOException ex) {
            Logger.getLogger(Transaction.class.getName()).log(Level.SEVERE, null, ex);
        }
        return byteBuffer.toByteArray();
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
        if(isReward()) {
            return null;
        }
        return this.changeOutput == null ? null : this.changeOutput.getDestinationAddress();
    }

    /**
     * Get the list of transactions outputs references that are used as inputs
     * in this very transaction.
     *
     * A TransactionInput is basically a hash of a transaction output, and the amount
     * of that output.
     *
     * @see TransactionInput
     * @see TransactionOutput
     *
     * @return an ArrayList of TransactionInputs
     */
    public ArrayList<TransactionInput> getInputs() {
        return inputs;
    }

    /**
     * Get the transaction output.
     *
     * As opposed to the changeOutput, the transaction output is the output of
     * the transaction that is destinated to the payee (receiver of the payement).
     *
     * @see getChangeOutput
     * @see transactionOutput
     * @see changeOutput
     *
     * @return the output to the payee.
     */
    public TransactionOutput getTransactionOutput() {
        return this.transactionOutput;
    }

    /**
     * Get the change output.
     *
     * As opposed to the transactionOutput, the change output is the output
     * representing the change, i.e. when a wallet makes a transaction, it pays
     * a certain amount of Crashcoins. If it pays too much, the remaining
     * Crashcoins are redirected to its address as a change output.
     *
     * @see getChangeOutput
     * @see transactionOutput
     * @see changeOutput
     *
     * @return the change output
     */
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

    @Override
    public String toString() {
        String output = "Transaction: " + JsonUtils.encodeBytes(Cryptography.hashBytes(toBytes()));
        if(this.signature == null) {
            output += " (no signature)";
        }
        output += "\n";
        output += "  From: "+((this.isReward()) ? "Generated" : getReduiceAdresse(this.changeOutput.getDestinationAddress()))+"\n";
        output += "  To  : "+getReduiceAdresse(this.transactionOutput.getDestinationAddress())+"\n";
        output += "  At  : "+this.lockTime.toString() + "\n";
        
        for(int i = 0; i < 2 || (this.inputs != null && i < this.inputs.size()); ++i) {
            output += "  ";
            if(this.inputs != null && i < this.inputs.size()) {
                final TransactionInput currentInput = this.inputs.get(i);
                output += "[" + currentInput.getAmount() + "CC] " + JsonUtils.encodeBytes(currentInput.getHashBytes());
            } else {
                output += "\t\t\t\t\t\t\t";
            }
            if(i == 0) {
                output += " => " + JsonUtils.encodeBytes(this.transactionOutput.getHashBytes()) + " " + 
                        "[" + this.transactionOutput.getAmount() + "CC]";
            } else if(i == 1 && this.changeOutput != null) {
                output += " => " + JsonUtils.encodeBytes(this.changeOutput.getHashBytes()) + " " + 
                        "[" + this.changeOutput.getAmount() + "CC] (change)";
            }
            output += "\n";
        }
        return output;
    }
    
    private String getReduiceAdresse(final Address address) {
        String strAddress = address.toString();
        return strAddress.substring(2) + "..." + strAddress.substring(strAddress.length()-15, strAddress.length());
    }
    
}
