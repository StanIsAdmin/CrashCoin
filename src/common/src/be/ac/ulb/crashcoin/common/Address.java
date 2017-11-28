package be.ac.ulb.crashcoin.common;

import static be.ac.ulb.crashcoin.common.utils.Cryptography.deriveKey;
import java.security.PublicKey;
import java.util.Arrays;
import org.json.JSONObject;

public class Address implements JSONable {

    private final PublicKey key; // Public key
    private final byte[] value; // CrashCoin address, derived from the public key

    public Address(final PublicKey key) {
        super();
        this.key = key;
        this.value = deriveKey(key);
    }
    
    /** Create Address instance from a JSON representation
     * @param json 
     */
    public Address(final JSONObject json) {
        this((PublicKey) (json.get("key")));
    }
    
    private String getJsonType() {
        return "Address";
    }
    
    /** Get a JSON representation of the Address instance **/
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("key", key);
        return json;
    }

    /** 
     * Byte representation of the CrashCoin address
     * @return the byte
     */
    public byte[] toBytes() {
        return value;
    }

    /** 
     * Get public key, from which the address has been derived
     * @return the public key
     */
    public PublicKey getPublicKey() {
        return key;
    }

    /** Used for test purposes **/
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
}
