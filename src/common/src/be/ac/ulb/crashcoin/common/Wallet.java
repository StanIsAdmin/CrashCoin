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
import javax.crypto.IllegalBlockSizeException;

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
        
        final WalletInformation walletInformation;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
            walletInformation = (WalletInformation) ois.readObject();
        } catch(IOException ex) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Error with file {0}", ex.getMessage());
            return null;
        }
        
        final KeyPair keyPair = Cryptography.keyPairFromWalletInformation(userPassword, walletInformation);
        if (keyPair == null) {
            System.out.println("The password you entered is incorrect");
            return null;
        }
        this.publicKey = keyPair.getPublic();
        return keyPair.getPrivate();
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
