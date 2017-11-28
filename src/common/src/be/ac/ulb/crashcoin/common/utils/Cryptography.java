package be.ac.ulb.crashcoin.common.utils;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class containing the cryptographic methods needed in the common package.
 */
public class Cryptography {
    
    /** Transaction hasher */
    private static MessageDigest transactionHasher = null;
    
    /**
     * Performs SHA-256 hash of the transaction
     * 
     * @param trans the transaction instance to hash
     * @return A 32 byte long byte[] with the SHA-256 of the transaction
     * @throws NoSuchAlgorithmException if the machine is unable to perform SHA-256
     */
    public static byte[] hashTransaction(Transaction trans) throws NoSuchAlgorithmException {
        if (transactionHasher == null) {
            transactionHasher = MessageDigest.getInstance(Parameters.TRANSACTION_HASH_ALGORITHM);
        }
        transactionHasher.update(trans.toBytes());
        return transactionHasher.digest();
    }
}
