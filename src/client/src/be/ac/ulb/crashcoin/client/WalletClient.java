package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.TransactionInput;
import be.ac.ulb.crashcoin.common.TransactionOutput;
import be.ac.ulb.crashcoin.common.Wallet;
import be.ac.ulb.crashcoin.common.WalletInformation;
import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;
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
    
    private final ArrayList<Transaction> acceptedTransactionsList;
    private final ArrayList<Transaction> unacceptedTransactionsList;
    
    public WalletClient(final File f, final char[] userPassword) throws IOException, 
            FileNotFoundException, ClassNotFoundException, InvalidKeySpecException, 
            InvalidKeyException, InvalidAlgorithmParameterException, IllegalBlockSizeException, InstantiationException {
        super(f, userPassword);
        
        acceptedTransactionsList = new ArrayList<>();
        unacceptedTransactionsList = new ArrayList<>();
    }
    
    @Override
    protected void actOnCorrectAuthentication() {
        System.out.println("Welcome in your wallet!");
//        Uncomment if you will get private and public key (currently used to get the private key of master)
        System.out.println("Your public key :");
        System.out.println(JsonUtils.encodeBytes(this.publicKey.getEncoded()));
//        System.out.println("Your private key:");
//        System.out.println(JsonUtils.encodeBytes(keyPair.getPrivate().getEncoded()));
        System.out.println("");
    }
    
    public synchronized void addAcceptedTransaction(final Transaction transaction) {
        this.unacceptedTransactionsList.remove(transaction);
        this.acceptedTransactionsList.add(transaction);
    }
    
    public void addUnacceptedTransaction(final Transaction transaction) {
        this.unacceptedTransactionsList.add(transaction);
    }
    
    public synchronized void updateTransactionStatus(final Transaction transaction) {
        if(this.unacceptedTransactionsList.contains(transaction)) {
            this.unacceptedTransactionsList.remove(transaction);
        }
        // if already contained in accepted transactions list (just in case of)
        if(!this.acceptedTransactionsList.contains(transaction)) {
            this.acceptedTransactionsList.add(transaction);
        }
    }
    
    public ArrayList<Transaction> getAllTransaction() {
        final ArrayList<Transaction> allTransaction = new ArrayList<>();
        allTransaction.addAll(unacceptedTransactionsList);
        allTransaction.addAll(acceptedTransactionsList);
        return allTransaction;
    }

    public ArrayList<Transaction> getAcceptedTransactions() {
        return this.acceptedTransactionsList;
    }
    
    public ArrayList<Transaction> getUnacceptedTransactions() {
        return this.unacceptedTransactionsList;
    }
    
    public List<TransactionOutput> getUsefulTransactions(final int amount) {
        final List<TransactionOutput> transactions = new ArrayList<>();
        final Address srcAddress = new Address(this.publicKey);
        int total = 0;
        for (final Transaction transaction: getAllTransaction()) {
            
            final TransactionOutput transactionOut;
            // Get destination address
            if(transaction.getDestAddress().equals(srcAddress)) {
                transactionOut = transaction.getTransactionOutput();
                
            // Get the address of the change back (the source user)
            } else if(transaction.getChangeOutput().getDestinationAddress().equals(srcAddress)) {
                transactionOut = transaction.getChangeOutput();
                
            } else {
                continue;
            }
            
            if(!alreadyUsed(transactionOut.getHashBytes())) {
                total += transactionOut.getAmount();
                transactions.add(transactionOut);
            }
            
            if (total >= amount) {
                return transactions;
            }
        }
        return null;
    }
    
    private boolean alreadyUsed(final byte[] hashTransaction) {
        for (final Transaction transaction: getAllTransaction()) {
            if(transaction.getInputs() != null) {
                for(final TransactionInput transInput : transaction.getInputs()) {
                    if(Arrays.equals(transInput.getHashBytes(), hashTransaction)) {
                        return true;
                    }
                }
            }
        }
        return false;
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
    public static void writeWalletFile(final char[] userPassword, final String accountName, final KeyPair keyPair) 
            throws InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, FileNotFoundException,
            IOException {
        writeWalletFile(userPassword, accountName, keyPair.getPublic().getEncoded(), keyPair.getPrivate().getEncoded());
    }
    
    
    public static void writeWalletFile(final char[] userPassword, final String accountName, 
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

}
