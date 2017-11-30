package be.ac.ulb.crashcoin.common;

import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.bind.DatatypeConverter;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;
import org.json.JSONArray;
import org.json.JSONObject;

public class Address implements JSONable {

    private PublicKey key; // Public key
    private byte[] value; // CrashCoin address, derived from the public key

    public Address(final PublicKey key) {
        super();
        this.key = key;
        this.value = Cryptography.deriveKey(key);
    }
    
    /** Create Address instance from a JSON representation
     * @param json 
     */
    public Address(final JSONObject json) {
        String bytesStr = json.getString("key");
        byte keyArray[] = DatatypeConverter.parseBase64Binary(bytesStr);
        X509EncodedKeySpec ks = new X509EncodedKeySpec(keyArray);
        KeyFactory kf;
        try {
            kf = KeyFactory.getInstance("DSA");
            this.key = kf.generatePublic(ks);
            this.value = applyRIPEMD160(this.key);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        }
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
