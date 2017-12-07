package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.util.Arrays;
import org.json.JSONObject;

/**
 * Input of a transaction, from the doc
 * https://en.bitcoin.it/wiki/Transaction
 */
public class TransactionInput implements JSONable {

    private final byte[] previousOutputHash; // Hash value of a previous transaction
    private final Integer previousOutputAmount; // Amount of the previous output

    public TransactionInput(final TransactionOutput output) {
        this.previousOutputHash = Cryptography.hashBytes(output.toBytes());
        this.previousOutputAmount = output.getAmount();
    }

    public TransactionInput(final JSONObject json) {
        this.previousOutputHash = JsonUtils.decodeBytes(json.getString("previousOutputHash"));
        this.previousOutputAmount = json.getInt("previousOutputAmount");
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
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
        return Arrays.equals(this.previousOutputHash, other.previousOutputHash) && this.previousOutputAmount.equals(other.previousOutputAmount);
    }
    
}
