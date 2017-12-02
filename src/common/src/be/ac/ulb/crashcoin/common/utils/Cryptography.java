package be.ac.ulb.crashcoin.common.utils;

import be.ac.ulb.crashcoin.common.Parameters;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

/**
 * Utility class containing the cryptographic methods needed in the common
 * package.
 */
public class Cryptography {

    /**
     * Transaction hasher
     */
    private static MessageDigest hasher = null;

    /**
     * Key derivator
     */
    private static RIPEMD160Digest ripemdHasher = null;

    /**
     * Performs SHA-256 hash of the transaction
     *
     * @param data the byte array to hash
     * @return A 32 byte long byte[] with the SHA-256 of the transaction
     * @throws NoSuchAlgorithmException if the machine is unable to perform
     * SHA-256
     */
    public static byte[] hashBytes(byte[] data) throws NoSuchAlgorithmException {
        if (hasher == null) {
            hasher = MessageDigest.getInstance(Parameters.HASH_ALGORITHM);
        }
        hasher.update(data);
        return hasher.digest();
    }

    /**
     * Applies RIPEMD160 to retrieve the CrashCoin address from a public key.
     *
     * @param key Public key
     * @return Byte representation of the CrashCoin address
     */
    public static byte[] deriveKey(final PublicKey key) {
        if (ripemdHasher == null) {
            ripemdHasher = new RIPEMD160Digest();
        }
        final byte[] bytes = key.getEncoded();
        ripemdHasher.update(bytes, 0, bytes.length); // Copute RIPEMD160 digest
        ripemdHasher.doFinal(bytes, 0); // Copy digest into bytes
        return bytes;
    }

    /**
     * Converts a string representation of a public key to a PublicKey instance.
     *
     * @param key the string representation of the public key
     * @return a PublicKey instance
     */
    public static PublicKey createPublicKeyFromBytes(byte[] key) {
        PublicKey pk = null;
        try {
            X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(key);
            KeyFactory kf = KeyFactory.getInstance("DSA");
            pk = kf.generatePublic(X509publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return pk;
    }
}
