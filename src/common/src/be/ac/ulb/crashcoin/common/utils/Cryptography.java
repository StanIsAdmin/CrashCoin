package be.ac.ulb.crashcoin.common.utils;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
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
    
    private static Cipher cipher = null;
    
    private static SecretKeyFactory factory = null;
    
    /**
     * Secure random number/bytes generators.
     */
    private static SecureRandom secureRandom = null;
    
    /**
     * DSA key pair generator.
     */
    private static KeyPairGenerator dsaKeyGen = null;
    
    /**
     * DSA public/private key constructor from bytes.
     */
    private static KeyFactory dsaKeyFactory = null;

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
                logAndAbort("Unable to use SHA-256 hash... Abort!", ex);
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
    
    public static Cipher getCipher() {
        if(cipher == null) {
            try {
                cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
                logAndAbort("Unable to get cipher: \"AES/CBC/PKCS5Padding\". Abort!", ex);
            }
        }
        return cipher;
    }
    
    /**
     * Convert encoded private and public keys (bytes) to Private / PublicKey
     * interfaces and generate a KeyPair from them in order to construct a
     * Wallet object in the signIn method<br>
     * <b>Two different encoding</b>
     *
     * @param publicKeyBytes the public key with encoding X509
     * @param privateKeyBytes the private key with encoding PKCS8
     * @return the key pair
     */
    public static KeyPair createKeyPairFromEncodedKeys(byte[] publicKeyBytes, byte[] privateKeyBytes) {
        try {
            // Generate specs
            final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
            final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
            
            dsaKeyFactory = Cryptography.getDsaKeyFactory();
            
            // Create PublicKey and PrivateKey interfaces using the factory
            final PrivateKey privateKey = dsaKeyFactory.generatePrivate(privateKeySpec);
            final PublicKey publicKey = dsaKeyFactory.generatePublic(publicKeySpec);
            
            return (new KeyPair(publicKey, privateKey));
        } catch (InvalidKeySpecException ex) {
            logAndAbort("Unable to create key pair. Abort!", ex);
        }
        return null;
    }
    
    public static SecretKeyFactory getSecretKeyFactory() {
        if(factory == null) {
            try {
                factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
            } catch (NoSuchAlgorithmException ex) {
                logAndAbort("Unable to create SecretKeyFactory. Abort!", ex);
            }
        }
        return factory;
    }
    
    public static SecureRandom getSecureRandom() {
        if(secureRandom == null) {
            try {
                secureRandom = SecureRandom.getInstance("SHA1PRNG", "SUN");
            } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
                logAndAbort("Unable to create Secure Random \"SHA1PRNG\". Abort!", ex);
            }
        }
        return secureRandom;
    }
    
    /**
     * Compute an encryption / decryption key (they are the same) from the
     * password and the salt<br>
     *
     * @param userPassword password of the user
     * @param salt extended string
     * @return SecretKey
     */
    public static SecretKey computeSecretKey(final char[] userPassword, final byte[] salt) {

        try {
            factory = Cryptography.getSecretKeyFactory();
            final KeySpec spec = new PBEKeySpec(userPassword, salt, Parameters.KEY_DERIVATION_ITERATION,
                    Parameters.KEY_SIZE);
            final SecretKey tmpKey = factory.generateSecret(spec);
            final SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");
            
            return secretKey;
        } catch (InvalidKeySpecException ex) {
            logAndAbort("Unable to compute Secret Key. Abort!", ex);
        }
        return null;
    }
    
    public static KeyPairGenerator getDsaKeyGen() {
        if(dsaKeyGen == null) {
            try {
                dsaKeyGen = KeyPairGenerator.getInstance("DSA", "SUN");
            } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
                logAndAbort("unable to create DSA Keygen. Abort!", ex);
            }
        }
        return dsaKeyGen;
    }
    
    /**
     * Transforms a byte array into a PrivateKey.
     * 
     * @param privateKeyBytes the bytes to transform into a private key
     * @return the private key associated to the byte array
     */
    public static PrivateKey getPrivateKeyFomBytes(final byte[] privateKeyBytes) {
        final X509EncodedKeySpec ks = new X509EncodedKeySpec(privateKeyBytes);
        dsaKeyFactory = Cryptography.getDsaKeyFactory();
        PrivateKey pv = null;
        try {
            pv = dsaKeyFactory.generatePrivate(ks);
        } catch (InvalidKeySpecException ex) {
            logAndAbort("Unable to generate private key from bytes. Abort!", ex);
        }
        return pv;
    }
    
    /**
     * Transforms a byte array into a PublicKey.
     * 
     * @param publicKeyBytes the bytes to transform into a public key
     * @return the public key associated to the byte array
     */
    public static PublicKey getPublicKeyFromBytes(final byte[] publicKeyBytes) {
        dsaKeyFactory = Cryptography.getDsaKeyFactory();
        final X509EncodedKeySpec ks = new X509EncodedKeySpec(publicKeyBytes);
        try {
            return dsaKeyFactory.generatePublic(ks);
        } catch (InvalidKeySpecException ex) {
            logAndAbort("Unable to generate public key from bytes. Abort!", ex);
        }
        return null;
    }
    
    ///// private
    
    private static KeyFactory getDsaKeyFactory() {
        if(dsaKeyFactory == null) {
            try {
                dsaKeyFactory = KeyFactory.getInstance("DSA");
            } catch (NoSuchAlgorithmException ex) {
                logAndAbort("Unable to create DSA key factory. Abort!", ex);
            }
        }
        return dsaKeyFactory;
    }
    
    /**
     * Logs a message and the exception that goes with it, then aborts the program.
     * 
     * @param message The message to log (null if none)
     * @param exception The exception that has been thrown
     */
    private static void logAndAbort(final String message, final Throwable exception) {
        Logger.getLogger(Cryptography.class.getName()).log(Level.SEVERE, message, exception);
        System.exit(1);
    }
}
