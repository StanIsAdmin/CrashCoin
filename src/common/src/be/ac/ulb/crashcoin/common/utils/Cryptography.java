package be.ac.ulb.crashcoin.common.utils;

import be.ac.ulb.crashcoin.common.Parameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
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
     */
    public static byte[] hashBytes(byte[] data) {
        if (hasher == null) {
            try {
                hasher = MessageDigest.getInstance(Parameters.HASH_ALGORITHM);
            } catch(NoSuchAlgorithmException ex) {
                Logger.getLogger(Cryptography.class.getName()).log(Level.SEVERE, "Unable to use SHA-256 hash... Abort!", ex);
                System.exit(1);
            }
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
    public static PublicKey createPublicKeyFromBytes(final byte[] key) {
        PublicKey pk = null;
        try {
            final X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(key);
            final KeyFactory kf = KeyFactory.getInstance("DSA");
            pk = kf.generatePublic(X509publicKey);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return pk;
    }
    
    public static Signature dsaFromPrivateKey(final PrivateKey privateKey) {
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[Error] Could not find DSA signature algorithm");
        } catch (NoSuchProviderException e) {
            System.out.println("[Error] Could not find provider for DSA");
        }

        try {
            // Using private key to sign with DSA
            dsa.initSign(privateKey);
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        }
        return dsa;
    }

    public static Signature dsaFromPublicKey(final PublicKey publicKey) {
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e2) {
            e2.printStackTrace();
        }
        try {
            // Using public key to verify signatures
            dsa.initVerify(publicKey);
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        }
        return dsa;
    }

    /**
     * Returns a transaction signature using DSA algorithm.
     *
     * @param privateKey private key
     * @param bytes data to sign
     * @return transaction signature
     */
    public static byte[] signTransaction(final PrivateKey privateKey, final byte[] bytes) {
        final Signature dsa = dsaFromPrivateKey(privateKey);
        byte[] signature = null;
        try {
            // Running DSA
            dsa.update(bytes, 0, bytes.length);
            signature = dsa.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return signature;
    }

    public static boolean verifySignature(final PublicKey publicKey, final byte[] transaction, final byte[] signature) {
        final Signature dsa = Cryptography.dsaFromPublicKey(publicKey);

        boolean verified = false;
        try {
            dsa.update(transaction);
            verified = dsa.verify(signature);
        } catch (SignatureException e) {
            Logger.getLogger(Cryptography.class.getName()).log(Level.SEVERE, e.getMessage());
        }
        return verified;
    }
}
