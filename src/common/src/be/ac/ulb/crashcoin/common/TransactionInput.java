package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import java.util.Arrays;
import org.json.JSONObject;

/**
 * Input of a transaction, from the doc
 * https://en.bitcoin.it/wiki/Transaction
 */
public class TransactionInput implements JSONable {

    /** Hash value of a previous output */
    private final byte[] previousOutputHash; 
    /** Number of CrashCoins available in the previous output */
    private final Integer previousOutputAmount;

    public TransactionInput(final TransactionOutput output) {
        this.previousOutputHash = output.getHashBytes();
        this.previousOutputAmount = output.getAmount();
    }

    public TransactionInput(final JSONObject json) {
        this.previousOutputHash = JsonUtils.decodeBytes(json.getString("previousOutputHash"));
        this.previousOutputAmount = json.getInt("previousOutputAmount");
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("previousOutputHash", JsonUtils.encodeBytes(this.previousOutputHash));
        json.put("previousOutputAmount", this.previousOutputAmount);
        return json;
    }

    /**
     * Get the amount of Crashcoin coming from this very input.
     * 
     * @return the number of CrashCoins coming from the input
     */
    public Integer getAmount() {
        return this.previousOutputAmount;
    }

    /**
     * Byte representation of a transaction input, which is simply the hash
     * of the input transaction.
     *
     * @return byte[]  Byte representation of the input
     */
    public byte[] getHashBytes() {
        return previousOutputHash;
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
        final TransactionInput other = (TransactionInput) obj;
        return Arrays.equals(this.previousOutputHash, other.previousOutputHash) && 
                this.previousOutputAmount.equals(other.previousOutputAmount);
    }
    
}
