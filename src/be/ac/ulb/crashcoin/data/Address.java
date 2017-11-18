package be.ac.ulb.crashcoin.data;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.security.PublicKey;

public class Address {

    private PublicKey key; // Public key
    private byte[] value; // CrashCoin address, derived from the public key

    public Address(PublicKey key) {
        this.key = key;
        this.value = applyRIPEMD160(key);
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
}