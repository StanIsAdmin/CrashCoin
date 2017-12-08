/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.TransactionOutput;
import be.ac.ulb.crashcoin.common.Wallet;
import be.ac.ulb.crashcoin.common.WalletInformation;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

/**
 *
 * @author Nathan
 */
public class WalletClient extends Wallet {
    
    private final ArrayList<Transaction> transactionsList;
    private static WalletClient instance;
    
    public WalletClient() {
        super();
        transactionsList = new ArrayList<>();
    }
    
    private WalletClient(KeyPair keypair) {
        super(keypair);
        transactionsList = new ArrayList<>();
    }
    
    // Only to debug !
    public static void resetInstance() {
        WalletClient.instance = null;
    }
    
    public static WalletClient getInstance() {
        if (WalletClient.instance == null) {
            instance = new WalletClient();
        }
        return instance;
    }
    
    public static WalletClient getInstance(KeyPair keypair) {
        if (WalletClient.instance == null) {
            instance = new WalletClient(keypair);
        }
        return instance;
    }
    
    public void addTransaction(final Transaction transaction) {
        this.transactionsList.add(transaction);
    }

    public ArrayList<Transaction> getTransactions() {
        return this.transactionsList;
    }
    
    public List<TransactionOutput> getUsefulTransactions(int amount) {
        List<TransactionOutput> transactions = null;
        Address srcAddress = new Address(this.publicKey);
        int total = 0;
        for (Transaction transaction: transactionsList) {
            if (transaction.getDestAddress() == srcAddress) {
                total += transaction.getTransactionOutput().getAmount();
            } else {
                total -= transaction.getTransactionOutput().getAmount();
            }
            transactions.add(transaction.getTransactionOutput());
            if (total >= amount) {
                return transactions;
            }
        }
        return null;
    }
    
    /**
     * Methods used to write the current ClientWallent into a generated file.
     * @param userPassword
     * @param accountName
     * @param keyPair
     * @throws InvalidKeyException
     * @throws InvalidParameterSpecException
     * @throws IllegalBlockSizeException
     * @throws BadPaddingException
     * @throws FileNotFoundException
     * @throws IOException 
     */
    
    public void writeWalletFile(final char[] userPassword, final String accountName, final KeyPair keyPair) 
            throws InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException,
            IOException {
        publicKey = keyPair.getPublic();

        // Get the bytes representation of the keys
        final byte[] publicKeyBytes = publicKey.getEncoded();
        
        writeWalletFile(userPassword, accountName, publicKeyBytes, keyPair.getPrivate().getEncoded());
    }
    
    
    public void writeWalletFile(final char[] userPassword, final String accountName, 
            final byte[] publicKeyBytes, final byte[] privateKeyBytes) throws InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException, IOException {

        // Encrypt the private key using AES-128 protocol with the user password
        // Compute a salt to avoid dictionary attacks (to turn a password into a secret key)
        // The salt is not kept secret but is needed for decryption
        final SecureRandom random = new SecureRandom();
        final byte[] salt = new byte[Parameters.SALT_SIZE];
        random.nextBytes(salt);

        final SecretKey encryptionKey = Cryptography.computeSecretKey(userPassword, salt);

        // Encrypt the private key with the encryption key generated from the user password
        // Initialize a cipher to the encryption mode with the encryptionKey
        final Cipher cipher = Cryptography.getCipher();
        cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);

        // Get the IV necessary to decrypt the message later
        // In CBC mode, each block is XORed with the output of the previous block
        // The IV represents the arbitrary "previous block" to be used for the first block
        final AlgorithmParameters parameters = cipher.getParameters();
        final byte[] iv = parameters.getParameterSpec(IvParameterSpec.class).getIV();

        // Encrypt the private key
        final byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);
        // Write wallet information in the wallet file
        final WalletInformation walletInformation = new WalletInformation(salt, iv, encryptedPrivateKey, publicKeyBytes);

        // Creates wallet folder if not exists
        final File walletFolder = new File(Parameters.WALLETS_PATH);
        if (!walletFolder.exists()) {
            walletFolder.mkdir();
        }

        // Creates new wallet file
        final File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
        oos.writeObject(walletInformation);
        oos.flush();
        oos.close();

        System.out.println("The creation of your wallet completed successfully");
        System.out.println("Please sign in and start crashing coins");
    }
    
    /**
     * Generates a DSA key pair, composed of a public key and a private key. The
     * key size is defined in parameters. This method can be called at most one
     * time per wallet.
     *
     * @return Pair of DSA keys
     */
    public KeyPair generateKeys() {
        if (publicKey != null) {
            System.out.println("[Error] Only one key pair can be assigned to a wallet");
            return null;
        }
        final SecureRandom random = Cryptography.getSecureRandom();
        dsaKeyGen.initialize(Parameters.DSA_KEYS_N_BITS, random);
        final KeyPair keyPair = dsaKeyGen.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        return keyPair;
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
