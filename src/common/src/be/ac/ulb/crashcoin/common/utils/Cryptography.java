package be.ac.ulb.crashcoin.common.utils;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.WalletInformation;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
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
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;
import org.bouncycastle.crypto.digests.RIPEMD160Digest;

/**
 * Utility class containing the cryptographic methods needed in the common
 * package.
 */
public class Cryptography {

    /**
     * Transaction hasher.
     */
    private static MessageDigest hasher = null;
    
    /**
     * Secure random number/bytes generators.
     */
    private static SecureRandom randomGenerator = null;

    /**
     * Key derivation.
     */
    private static RIPEMD160Digest ripemdHasher = new RIPEMD160Digest();
    
    private static Cipher cipher = null;
    
    private static SecretKeyFactory secretKeyFactory = null;
    
    /**
     * DSA key pair generator.
     */
    private static KeyPairGenerator dsaKeyGenerator = null;
    
    /**
     * DSA public/private key constructor from bytes.
     */
    private static KeyFactory dsaKeyFactory = null;
    private static Signature dsaSigner;
    
    // Initialization of static attributes
    static {
        // Hashing
        try {
            hasher = MessageDigest.getInstance(Parameters.HASH_ALGORITHM);
        } catch(NoSuchAlgorithmException ex) {
            logAndAbort("Unable to use SHA-256 hash... Abort!", ex);
        }
        // Key derivation
        try {
            cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        } catch (NoSuchAlgorithmException | NoSuchPaddingException ex) {
            logAndAbort("Unable to get cipher: \"AES/CBC/PKCS5Padding\". Abort!", ex);
        }
        try {
            secretKeyFactory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        } catch (NoSuchAlgorithmException ex) {
            logAndAbort("Unable to create SecretKeyFactory. Abort!", ex);
        }
        // Secure random generation
        try {
            randomGenerator = SecureRandom.getInstance("SHA1PRNG", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            logAndAbort("Unable to create Secure Random \"SHA1PRNG\". Abort!", ex);
        }
        // DSA key generation
        try {
            dsaKeyGenerator = KeyPairGenerator.getInstance("DSA", "SUN");
        } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
            logAndAbort("unable to create DSA Keygen. Abort!", ex);
        }
        try {
            dsaKeyFactory = KeyFactory.getInstance("DSA");
        } catch (NoSuchAlgorithmException ex) {
            logAndAbort("Unable to create DSA key factory. Abort!", ex);
        }
        try {
            dsaSigner = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException e) {
            logAndAbort("[Error] Could not find DSA signature algorithm. Abort!", e);
        } catch (NoSuchProviderException e) {
            logAndAbort("[Error] Could not find provider for DSA. Abort!", e);
        }
    }

    /**
     * Performs SHA-256 hash of the transaction
     *
     * @param data the byte array to hash
     * @return A 32 byte long byte[] with the SHA-256 of the transaction
     */
    public static byte[] hashBytes(final byte[] data) {
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
        final X509EncodedKeySpec X509publicKey = new X509EncodedKeySpec(key);
        try {
            return dsaKeyFactory.generatePublic(X509publicKey);
        } catch (InvalidKeySpecException e) {
            logAndAbort("Unable to create public key from bytes. Abort!", e);
        }
        return null;
    }

    /**
     * Returns a transaction signature using DSA algorithm.
     *
     * @param privateKey private key
     * @param data data to sign
     * @return transaction signature
     */
    public static byte[] signData(final PrivateKey privateKey, final byte[] data) {
        // Use private key to initialize DSA signer
        try {
            dsaSigner.initSign(privateKey);
        } catch (InvalidKeyException e1) {
            logAndAbort("Unable to create signature. Abort!", e1);
        }
        // Run the DSA signature generation on the data
        try {
            dsaSigner.update(data, 0, data.length);
            return dsaSigner.sign();
        } catch (SignatureException e) {
            logAndAbort("Unable to sign transaction. Abort!", e);
        }
        return null;
    }

    public static boolean verifySignature(final PublicKey publicKey, final byte[] data, final byte[] signature) {
        // Use public key to verify signatures
        try {
            dsaSigner.initVerify(publicKey);
        } catch (InvalidKeyException e1) {
            logAndAbort("Unable to verify signature. Abort!", e1);
        }
        // Run the DSA signature verification
        try {
            dsaSigner.update(data);
            return dsaSigner.verify(signature);
        } catch (SignatureException e) {}
        return false;
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
        // Generate specs
        final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        
        try {
            // Create PublicKey and PrivateKey interfaces using the factory
            final PrivateKey privateKey = dsaKeyFactory.generatePrivate(privateKeySpec);
            final PublicKey publicKey = dsaKeyFactory.generatePublic(publicKeySpec);
            
            return new KeyPair(publicKey, privateKey);
        } catch (InvalidKeySpecException ex) {
            logAndAbort("Unable to create key pair. Abort!", ex);
        }
        return null;
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
            final KeySpec spec = new PBEKeySpec(userPassword, salt, Parameters.KEY_DERIVATION_ITERATION,
                    Parameters.KEY_SIZE);
            final SecretKey tmpKey = secretKeyFactory.generateSecret(spec);
            final SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");
            
            return secretKey;
        } catch (InvalidKeySpecException ex) {
            logAndAbort("Unable to compute Secret Key. Abort!", ex);
        }
        return null;
    }
    
    /**
     * Transforms a byte array into a PrivateKey.
     * 
     * @param privateKeyBytes the bytes to transform into a private key
     * @return the private key associated to the byte array
     */
    public static PrivateKey getPrivateKeyFomBytes(final byte[] privateKeyBytes) {
        final X509EncodedKeySpec ks = new X509EncodedKeySpec(privateKeyBytes);
        try {
            return dsaKeyFactory.generatePrivate(ks);
        } catch (InvalidKeySpecException ex) {
            logAndAbort("Unable to generate private key from bytes. Abort!", ex);
        }
        return null;
    }
    
    /**
     * Generates a DSA key pair, composed of a public key and a private key. The
     * key size is defined in parameters. This method can be called at most one
     * time per wallet.
     *
     * @return Pair of DSA keys
     */
    public static KeyPair generateKeys() {
        dsaKeyGenerator.initialize(Parameters.DSA_KEYS_N_BITS, randomGenerator);
        final KeyPair keyPair = dsaKeyGenerator.generateKeyPair();
        return keyPair;
    }
    
    /**
     * The objective of this method is to verify that the private key that we've
     * just decrypted in the login method using the user password is the valid
     * one. If the user entered a wrong password the decryption cipher would
     * still produce some results, i.e. a wrong private key
     *
     * To solve this, Antoine proposed to create a fake local trasaction with
     * the private key that we've just decrypted and verify it with the public
     * key associated with the user. If the transaction cannot be verified with
     * the public key, then the private key and the password were wrong.
     *
     * @param keyPair the key pair
     * @return True if signature is valid
     */
    public static Boolean verifyPrivateKey(final KeyPair keyPair) {
        final PrivateKey privateKey = keyPair.getPrivate();
        final PublicKey publicKey = keyPair.getPublic();

        // Create dummy transaction
        final byte[] dummyTransaction = new byte[50];
        new Random().nextBytes(dummyTransaction);

        // Sign the dummy transaction with the private key that we want to verify
        final byte[] dummySignature = Cryptography.signData(privateKey, dummyTransaction);

        // Verify the signature using the public key and the specific Wallet method
        return Cryptography.verifySignature(publicKey, dummyTransaction, dummySignature);
    }
    
    ///// private
    
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

    /**
     * Creates a WalletInformation from a password and KeyPair.
     * The private key is encrypted with the password.
     * 
     * @param userPassword the user-selected password, used to encrypt the private key
     * @param keyPair the public and private key pair
     * @return a WalletInformation instance
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException 
     */
    public static WalletInformation walletInformationFromKeyPair(final char[] userPassword, final KeyPair keyPair) 
            throws InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException {
        final byte[] privateKeyBytes = keyPair.getPrivate().getEncoded();
        final byte[] publicKeyBytes = keyPair.getPublic().getEncoded();
        // Encrypt the private key using AES-128 protocol with the user password
        // Compute a salt to avoid dictionary attacks (to turn a password into a secret key)
        // The salt is not kept secret but is needed for decryption
        final SecureRandom random = new SecureRandom();
        final byte[] salt = new byte[Parameters.SALT_SIZE];
        random.nextBytes(salt);

        final SecretKey encryptionKey = Cryptography.computeSecretKey(userPassword, salt);

        // Encrypt the private key with the encryption key generated from the user password
        // Initialize a cipher to the encryption mode with the encryptionKey
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);

        // Get the IV necessary to decrypt the message later
        // In CBC mode, each block is XORed with the output of the previous block
        // The IV represents the arbitrary "previous block" to be used for the first block
        final AlgorithmParameters parameters = cipher.getParameters();
        final byte[] iv = parameters.getParameterSpec(IvParameterSpec.class).getIV();

        // Encrypt the private key
        final byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);
        return new WalletInformation(salt, iv, encryptedPrivateKey, publicKeyBytes);
    }

    /**
     * Creates a KeyPair from a WalletInformation.
     * 
     * The private key is decrypted thanks to the password.
     * @param userPassword the user-selected password used to decrypt the private key
     * @param walletInformation the WalletInformation containing the encrypted private key
     * @return a KeyPair with the keys contained in walletInformation
     * @throws InvalidKeyException
     * @throws InvalidAlgorithmParameterException
     * @throws IllegalBlockSizeException 
     */
    public static KeyPair keyPairFromWalletInformation(final char[] userPassword, WalletInformation walletInformation) 
            throws InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        // Retrieve wallet information stored
        final byte[] salt = walletInformation.getSalt();
        final byte[] iv = walletInformation.getIv();
        final byte[] encryptedPrivateKey = walletInformation.getEncryptedPrivateKey();
        final byte[] publicKeyBytes = walletInformation.getPublicKey();

        final SecretKey decryptionKey = Cryptography.computeSecretKey(userPassword, salt);

        // Decrypt the private key with the decryption key generated from the user password
        // Initialize a cipher to the decryption mode with the decryptionKey
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new IvParameterSpec(iv));
        
        // Decrypt the private key
        KeyPair keyPair;
        try {

            final byte[] privateKeyBytes = cipher.doFinal(encryptedPrivateKey);

            // Create a PairKey with the encoded public and private keys
            keyPair = Cryptography.createKeyPairFromEncodedKeys(publicKeyBytes, privateKeyBytes);

            // Verify the private key generated by the password entered by the user
            if (! Cryptography.verifyPrivateKey(keyPair))
                return null;

        } catch (BadPaddingException e) {
            return null;
        }
        return keyPair;
    }
}
