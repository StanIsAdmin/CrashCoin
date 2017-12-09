package be.ac.ulb.crashcoin.common;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PublicKey;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.PrivateKey;
import java.security.spec.InvalidKeySpecException;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Wallet {

    protected PublicKey publicKey;
    protected KeyPairGenerator dsaKeyGen;

    /**
     * Constructs an empty wallet. This constructor behaves differently if one
     * passes a Keypair to it.
     */
    public Wallet() {
        dsaKeyGen = Cryptography.getDsaKeyGen();
    }

    /**
     * Constructs a wallet from a key pair. Only the public key is stored. For
     * that reason, the key pair needs to be passed to signTransaction to be
     * able to sign a transaction. After constructing a wallet using this
     * constructor, one cannot generate keys with the same wallet anymore.
     *
     * @param keyPair Pair of keys
     * @see signTransaction
     */
    public Wallet(final KeyPair keyPair) {
        this();
        this.publicKey = keyPair.getPublic();
    }
    
    protected void actOnCorrectAuthentication() {
        System.out.println("Authentication completed");
    }
    
    public void readWalletFile(final String walletPath, final String userPassword) throws FileNotFoundException,
            IOException, ClassNotFoundException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        this.readWalletFile(new File(walletPath), userPassword.toCharArray());
    }

    protected boolean readWalletFile(final File f, final char[] userPassword) throws FileNotFoundException,
            IOException, ClassNotFoundException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        boolean allIsOk = false;
        final WalletInformation walletInformation;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f))) {
            walletInformation = (WalletInformation) ois.readObject();
        } catch(IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error with file {0}", ex.getMessage());
            return allIsOk;
        }

        // Retrieve wallet information stored on disk
        final byte[] salt = walletInformation.getSalt();
        final byte[] iv = walletInformation.getIv();
        final byte[] encryptedPrivateKey = walletInformation.getEncryptedPrivateKey();
        final byte[] publicKeyBytes = walletInformation.getPublicKey();

        final SecretKey decryptionKey = Cryptography.computeSecretKey(userPassword, salt);

        // Decrypt the private key with the decryption key generated from the user password
        // Initialize a cipher to the decryption mode with the decryptionKey
        final Cipher cipher = Cryptography.getCipher();
        cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new IvParameterSpec(iv));

        // Decrypt the private key
        try {

            final byte[] privateKeyBytes = cipher.doFinal(encryptedPrivateKey);

            // Create a PairKey with the encoded public and private keys
            final KeyPair keyPair = Cryptography.createKeyPairFromEncodedKeys(publicKeyBytes, privateKeyBytes);

            // Verify the private key generated by the password entered by the user
            if (this.verifyPrivateKey(keyPair)) {
                this.publicKey = keyPair.getPublic();
                this.actOnCorrectAuthentication();
                allIsOk = true;

            } else {
                System.out.println("The password you entered is incorrect");
            }

        } catch (BadPaddingException e) {

            /*
            * Normally raised by the line:
            *  
            * byte[] privateKeyBytes = cipher.doFinal(encryptedPrivateKey);
            * 
            * In case of a wrong passowrd.
            * 
            * PKCS 5 Padding has a specific structure preventing the decryption
            * to complete with the wrong key (raising a BadPaddingException)
            * It's a sane check but not secure enough. If by chance the wrong
            * password produce the same PKCS5 padding as the right one, no
            * exception will be raised. The safety check can't then rely solely
            * on a bad padding exception.            	 * 
            * 
            * A second test with the public will be performed afterward to ensure
            * that the private key is indeed the right one (see hereunder).
            * 
             */
            System.out.println("The password you entered is incorrect");
        }
        return allIsOk;
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
    public Boolean verifyPrivateKey(final KeyPair keyPair) {
        final Boolean verified;
        final PrivateKey privateKey = keyPair.getPrivate();
        final PublicKey publicKey = keyPair.getPublic();

        // Create dummy transaction
        final byte[] dummyTransaction = new byte[50];
        new Random().nextBytes(dummyTransaction);

        // Sign the dummy transaction with the private key that we want to verify
        final byte[] dummySignature = Cryptography.signTransaction(privateKey, dummyTransaction);

        // Verify the signature using the public key and the specific Wallet method
        verified = Cryptography.verifySignature(publicKey, dummyTransaction, dummySignature);

        return (verified);
    }
    
    /**
     * Get the unique public key
     *
     * @return The public key
     */
    public PublicKey getPublicKey() {
        return publicKey;
    }
    
    public Address getAddress() {
        return new Address(publicKey);
    }
    
}
