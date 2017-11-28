package be.ac.ulb.crashcoin.common.utils;

import be.ac.ulb.crashcoin.common.Parameters;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class containing the cryptographic methods needed in the common package.
 */
public class Cryptography {
    
    /** Transaction hasher */
    private static MessageDigest hasher = null;
    
    /**
     * Performs SHA-256 hash of the transaction
     * 
     * @param data the byte array to hash
     * @return A 32 byte long byte[] with the SHA-256 of the transaction
     * @throws NoSuchAlgorithmException if the machine is unable to perform SHA-256
     */
    public static byte[] hashBytes(byte[] data) throws NoSuchAlgorithmException {
        if (hasher == null) {
            hasher = MessageDigest.getInstance(Parameters.HASH_ALGORITHM);
        }
        hasher.update(data);
        return hasher.digest();
    }
}
