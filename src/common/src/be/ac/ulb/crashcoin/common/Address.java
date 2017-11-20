package be.ac.ulb.crashcoin.common;

import java.security.PublicKey;
import java.util.Arrays;
import java.util.Objects;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.json.JSONObject;

public class Address extends JSONable {

    private PublicKey key; // Public key
    private byte[] value; // CrashCoin address, derived from the public key

    public Address(PublicKey key) {
        super();
        this.key = key;
        this.value = applyRIPEMD160(key);
    }
    
    /** Create Address instance from a JSON representation **/
    public Address(JSONObject json) {
        super(json);
        this.key = (PublicKey) (json.get("key")); //TODO check if deserialization works properly on key instance.
        this.value = (byte[]) json.get("value");
    }
    
    /** Get a JSON representation of the Address instance **/
    public JSONObject toJSON() {
        JSONObject json = new JSONObject();
        json.put("key", key);
        json.put("value", value);
        return json;
    }

    /**
     * Apply RIPEMD160 algorithm to retrieve the CrashCoin address from the public
     * key.
     * 
     * @param key Public key
     * @return Byte representation of the CrashCoin address
     */
    private byte[] applyRIPEMD160(PublicKey key) {
        byte[] bytes = key.getEncoded();
        RIPEMD160Digest d = new RIPEMD160Digest();
        d.update(bytes, 0, bytes.length); // Copute RIPEMD160 digest
        d.doFinal(bytes, 0); // Copy digest into bytes
        return bytes;
    }

    /** Byte representation of the CrashCoin address */
    public byte[] toBytes() {
        return value;
    }

    /** Get public key, from which the address has been derived */
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