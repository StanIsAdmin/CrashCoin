package be.ac.ulb.crashcoin.common;

import java.security.InvalidKeyException;
import java.security.KeyPair;
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

public class Wallet {

    protected PublicKey publicKey;
    private final File file;
    
    /**
     * Constructs an empty wallet. This constructor behaves differently if one
     * passes a Keypair to it.
     * 
     * @param accountName name of the user who owns the wallet
     * @param userPassword user password
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.FileNotFoundException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws java.security.InvalidKeyException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws java.lang.InstantiationException
     */
    public Wallet(final String accountName, final char[] userPassword) throws IOException, FileNotFoundException, 
            ClassNotFoundException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, InstantiationException {
        this(new File(Parameters.WALLETS_PATH + accountName + ".wallet"), userPassword);
    }
    
    /**
     * Constructs an empty wallet. This constructor behaves differently if one
     * passes a Keypair to it.
     * 
     * @param f file that contains the wallet data
     * @param userPassword user password
     * @throws java.lang.ClassNotFoundException
     * @throws java.io.FileNotFoundException
     * @throws java.security.InvalidAlgorithmParameterException
     * @throws java.security.InvalidKeyException
     * @throws java.security.spec.InvalidKeySpecException
     * @throws javax.crypto.IllegalBlockSizeException
     * @throws java.lang.InstantiationException
     */
    public Wallet(final File f, final char[] userPassword) throws IOException, FileNotFoundException, 
            ClassNotFoundException, InvalidKeySpecException, InvalidKeyException, InvalidAlgorithmParameterException, 
            IllegalBlockSizeException, InstantiationException {
        file = f;
        
        if(!canReadWalletFile(userPassword)) {
            throw new InstantiationException();
        } else {
            this.actOnCorrectAuthentication();
        }
    }
    
    protected void actOnCorrectAuthentication() {
        Logger.getLogger(getClass().getName()).info("Authentication completed");
    }
    
    private boolean canReadWalletFile(final char[] userPassword) throws FileNotFoundException,
            IOException, ClassNotFoundException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        return readWalletFile(userPassword) != null;
    }
    
    private PrivateKey readWalletFile(final char[] userPassword) throws FileNotFoundException,
            IOException, ClassNotFoundException, InvalidKeySpecException,
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException {
        PrivateKey privateKey = null;
        final WalletInformation walletInformation;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            walletInformation = (WalletInformation) ois.readObject();
        } catch(IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error with file {0}", ex.getMessage());
            return privateKey;
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
            if (Cryptography.verifyPrivateKey(keyPair)) {
                this.publicKey = keyPair.getPublic();
                privateKey = keyPair.getPrivate();

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
        return privateKey;
    }
    
    public boolean signTransaction(final String password, final Transaction transaction) {
        return signTransaction(password.toCharArray(), transaction);
    }
    
    public boolean signTransaction(final char[] password, final Transaction transaction) {
        boolean isValid = false;
        PrivateKey privateKey;
        try {
            privateKey = readWalletFile(password);
            transaction.sign(privateKey);
            isValid = true;
            
        } catch (IOException | ClassNotFoundException | InvalidKeySpecException | InvalidKeyException | 
                InvalidAlgorithmParameterException | IllegalBlockSizeException ex) {
            Logger.getLogger(Wallet.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        return isValid;
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
