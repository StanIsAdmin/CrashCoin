package be.ac.ulb.crashcoin.client;

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
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

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
    
    public synchronized void removeNotValidTransaction(final Transaction transaction) {
        if(this.unacceptedTransactionsList.contains(transaction)) {
            this.unacceptedTransactionsList.remove(transaction);
        }
        if(this.acceptedTransactionsList.contains(transaction)) {
            this.acceptedTransactionsList.remove(transaction);
        }
    }
    
    public ArrayList<Transaction> getAllTransactions() {
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
        List<TransactionOutput> transactions = new ArrayList<>();
        int total = 0;
        for(final TransactionOutput transaction : getActiveTransactions()) {
            if(total < amount) {
                total += transaction.getAmount();
                transactions.add(transaction);
            } else {
                break;
            }
        }
        if(total < amount) {
            transactions = null;
        }
        
        return transactions;
    }
    
    public List<TransactionOutput> getActiveTransactions() {
        final List<TransactionOutput> transactions = new ArrayList<>();
        for (final Transaction transaction: getAllTransactions()) {
            
            final TransactionOutput transactionOut;
            // Get destination address
            if(transaction.getDestAddress().equals(getAddress())) {
                transactionOut = transaction.getTransactionOutput();
                
            // Get the address of the change back (the source user)
            } else if(transaction.getChangeOutput().getDestinationAddress().equals(getAddress())) {
                transactionOut = transaction.getChangeOutput();
                
            } else {
                continue;
            }
            
            if(!alreadyUsed(transactionOut.getHashBytes())) {
                transactions.add(transactionOut);
            }
        }
        return transactions;
    }
    
    public int getTotalAmount() {
        int total = 0;
        for(final TransactionOutput transaction : getActiveTransactions()) {
            total += transaction.getAmount();
        }
        return total;
    }
    
    private boolean alreadyUsed(final byte[] hashTransaction) {
        for (final Transaction transaction: getAllTransactions()) {
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
        
        // Write wallet information in the wallet file
        final WalletInformation walletInformation = Cryptography.walletInformationFromKeyPair(userPassword, keyPair);

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
