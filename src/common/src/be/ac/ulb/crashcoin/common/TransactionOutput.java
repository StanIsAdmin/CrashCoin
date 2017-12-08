package be.ac.ulb.crashcoin.common;

import java.nio.ByteBuffer;
import java.util.Objects;
import org.json.JSONObject;

/**
 * Output of a transaction, from the doc
 * https://en.bitcoin.it/wiki/Transaction
 */
public class TransactionOutput implements JSONable {

    /** The number of CrashCoins sent in the transaction. */
    private final Integer amount;
    /** 
     * The address of the payee.
     * 
     * This address is either the payee taht receives Crashcoins or the payer if
     * it payed too much (got change back).
     */
    private final Address address;

    public TransactionOutput(final Address address, final Integer nCrashCoins) {
        this.amount = nCrashCoins;
        this.address = address;
    }

    public TransactionOutput(final JSONObject json) {
        this.address = new Address(json.getJSONObject("address"));
        this.amount = json.getInt("amount");
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("address", this.address.toJSON());
        json.put("amount", this.amount);
        return json;
    }

    /**
     * Get the number of Crashcoins that are sent in this very output.
     * 
     * @return the amount of Crashcoins
     */
    public Integer getAmount() {
        return this.amount;
    }

    /**
     * Get the address this very output is sent to.
     * 
     * @return the address of the payee
     */
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
        final ByteBuffer buffer = ByteBuffer.allocate(address.toBytes().length + Parameters.INTEGER_N_BYTES);
        buffer.putInt(amount);
        buffer.put(address.toBytes());
        return buffer.array();
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
        final TransactionOutput other = (TransactionOutput) obj;
        if (!Objects.equals(this.amount, other.amount)) {
            return false;
        }
        if (!Objects.equals(this.address, other.address)) {
            return false;
        }
        return true;
    }
    
}
