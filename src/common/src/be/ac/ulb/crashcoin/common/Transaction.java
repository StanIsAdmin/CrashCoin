package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.nio.ByteBuffer;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Objects;
import org.json.JSONObject;

public class Transaction implements JSONable {

    private final Address srcAddress;
    private final Address destAddress;
    private final Integer totalAmount;
    private final Timestamp lockTime;
    private byte[] signature;
    private ArrayList<Input> inputs;
    private ArrayList<Output> outputs;

    /**
     * Constructor for transactions Transaction
     *
     * @param srcAddress CrashCoin address of the source
     * @param destAddress CrashCoin address of the destination
     * @param totalAmount Number of CrashCoins
     * @param lockTime Transaction timestamp
     */    
    public Transaction(final Address destAddress, final Integer totalAmount, final Timestamp lockTime) {
        this(destAddress, null, totalAmount, lockTime);
    }
    
    public Transaction(final Address destAddress, final Address srcAddress, final Integer totalAmount, final Timestamp lockTime) {
        this(destAddress, srcAddress, totalAmount, lockTime, null);
    }
    
    public Transaction(final Address destAddress, final Address srcAddress, final Integer totalAmount, final Timestamp lockTime, final byte[] signature) {
        super();
        this.destAddress = destAddress;
        this.srcAddress = srcAddress;
        this.totalAmount = totalAmount;
        this.lockTime = lockTime;
        this.signature = signature;
        this.inputs = new ArrayList<>();
        this.outputs = new ArrayList<>();
    }

    /**
     * Create Transaction instance from a JSON representation
     *
     * @param json
     */
    public Transaction(final JSONObject json) {
        this(new Address((JSONObject) json.get("destAddress")),
                (json.has("srcAddress"))? new Address((JSONObject) json.get("srcAddress")) : null,
                json.getInt("totalAmount"),
                new Timestamp(json.getLong("lockTime")),
                JsonUtils.decodeBytes(json.getString("signature")));
    }

    /**
     * Get a JSON representation of the Address instance * TODO add signature
     */
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("destAddress", destAddress.toJSON());
        json.put("signature", JsonUtils.encodeBytes(signature));
        json.put("totalAmount", totalAmount);
        json.put("lockTime", lockTime.getTime());
        if(srcAddress != null) {
            json.put("srcAddress", srcAddress.toJSON());
        }
        return json;
    }

    public void sign(PrivateKey privateKey) {
        this.signature = Cryptography.signTransaction(privateKey, this.toBytes());
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
        for (Output output : this.outputs) {
            sum += output.nCrashCoins;
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
        // TODO: convert inputs and outputs to bytes
        final byte[] destAddressBytes = destAddress.toBytes();
        Integer totalSize = null;
        ByteBuffer buffer = null;
        if(srcAddress != null) {
            final byte[] srcAddressBytes = srcAddress.toBytes();
            totalSize = destAddressBytes.length + srcAddressBytes.length + Parameters.INTEGER_N_BYTES;
            buffer = ByteBuffer.allocate(totalSize);
            buffer.put(srcAddressBytes);
        } else {
            totalSize = destAddressBytes.length + Parameters.INTEGER_N_BYTES;
            buffer = ByteBuffer.allocate(totalSize);
        }
        buffer.put(destAddressBytes);
        buffer.putInt(totalAmount);
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

        final byte[] previousTx; // Hash value of a previous transaction

        public Input(final Transaction previousTransaction) throws NoSuchAlgorithmException {
            this.previousTx = Cryptography.hashBytes(previousTransaction.toBytes());
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

        final Integer nCrashCoins;
        final Address address;

        public Output(final Address address, final Integer nCrashCoins) {
            this.nCrashCoins = nCrashCoins;
            this.address = address;
        }

        public Address getAddress() {
            return this.address;
        }
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
