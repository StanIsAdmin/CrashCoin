package be.ac.ulb.crashcoin.common;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.PublicKey;
import java.util.Arrays;
import org.json.JSONObject;
import be.ac.ulb.crashcoin.common.net.JsonUtils;

public class Address implements JSONable {

    private PublicKey key; // Public key
    private byte[] value; // CrashCoin address, derived from the public key

    public Address(final PublicKey key) {
        super();
        this.key = key;
        this.value = Cryptography.deriveKey(key);
    }

    /**
     * Create Address instance from a JSON representation.
     *
     * @param json the Address JSON representation, compatible with toJSON()
     */
    public Address(final JSONObject json) {
        final byte[] keyBytes = JsonUtils.decodeBytes(json.getString("key"));
        this.key = Cryptography.createPublicKeyFromBytes(keyBytes);
        this.value = Cryptography.deriveKey(this.key);
    }

    /**
     * Get a JSON representation of the Address instance *
     */
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("key", JsonUtils.encodeBytes(key.getEncoded()));
        return json;
    }

    /**
     * Byte representation of the CrashCoin address
     *
     * @return the byte
     */
    public byte[] toBytes() {
        return value;
    }

    /**
     * Get public key, from which the address has been derived
     *
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return key;
    }

    /**
     * Used for test purposes *
     */
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
        final Address other = (Address) obj;
        return this.key.equals(other.key) && Arrays.equals(this.value, other.value);
    }
    
    public String toString() {
        return key.getEncoded().toString();
    }
}
