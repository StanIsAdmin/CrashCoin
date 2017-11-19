package be.ac.ulb.crashcoin.common;

import org.bouncycastle.crypto.digests.RIPEMD160Digest;

import java.security.PublicKey;

public class Address {

    private final PublicKey key; // Public key
    private final byte[] value; // CrashCoin address, derived from the public key

    public Address(final PublicKey key) {
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
}