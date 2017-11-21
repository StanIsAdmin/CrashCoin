package be.ac.ulb.crashcoin.common;

import java.security.PublicKey;
import java.util.Arrays;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.json.JSONObject;

public class Address extends JSONable {

    private final PublicKey key; // Public key
    private final byte[] value; // CrashCoin address, derived from the public key

    public Address(final PublicKey key) {
        super();
        this.key = key;
        this.value = applyRIPEMD160(key);
    }
    
    /** Create Address instance from a JSON representation **/
    public Address(JSONObject json) {
        this((PublicKey) (json.get("key")));
    }
    
    /** Get a JSON representation of the Address instance **/
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("key", key);
        return json;
    }

    /**
     * Apply RIPEMD160 algorithm to retrieve the CrashCoin address from the public
     * key.
     * 
     * @param key Public key
     * @return Byte representation of the CrashCoin address
     */
    private byte[] applyRIPEMD160(final PublicKey key) {
        final byte[] bytes = key.getEncoded();
        final RIPEMD160Digest d = new RIPEMD160Digest();
        d.update(bytes, 0, bytes.length); // Copute RIPEMD160 digest
        d.doFinal(bytes, 0); // Copy digest into bytes
        return bytes;
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
        final Address other = (Address) obj;
        return this.key.equals(other.key) && Arrays.equals(this.value, other.value);
    }
}
