package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.client.net.RelayConnection;
import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Message;
import be.ac.ulb.crashcoin.common.Parameters;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;

import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.TransactionOutput;
import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.io.Console;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handle IO from user and network communication between nodes and wallet.
 */
public class ClientApplication {
    
    private static ClientApplication instance = null;
    
    private final Console console;
    private final Scanner reader = new Scanner(System.in);
    private WalletClient wallet;

    public ClientApplication() throws IOException,
            InvalidKeySpecException, InvalidKeyException, InvalidParameterSpecException,
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ClassNotFoundException {
        instance = this;
        wallet = null;
        
        console = System.console();
        int choice;
        do {
            printMenu();

            if (reader.hasNextInt()) {
                choice = reader.nextInt();
            } else {
                choice = -1;
                System.out.println("You must choose a number !\n");
                reader.next();
            }

            if (choice > 0) {
                if (wallet == null) { // If not register/login
                    actionMenuNotRegistered(choice);
                } else { // If login/register
                    actionMenuRegistred(choice);
                }
            }
            
        } while (choice != 3);
        System.out.println("Bye!");
        reader.close();
        System.exit(0);
    }

    private void actionMenuNotRegistered(final int choice) throws ClassNotFoundException, IOException, FileNotFoundException,
            InvalidKeyException, InvalidAlgorithmParameterException,
            InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, InvalidParameterSpecException {
        switch (choice) {
            case 1:
                signIn();
                break;

            case 2:
                signUp();
                break;
                
            case 3: // close with condition in while
                break;

            default:
                System.out.println("Unknow choice " + choice + "\n");
                break;

        }
    }

    private void actionMenuRegistred(final int choice) {
        switch (choice) {
            case 1:
                createTransaction();
                break;

            case 2:
                showWallet();
                break;

            case 3: // close with condition in while
                break;

            case 4: // Disconnect
                wallet = null;
                break;

            default:
                System.out.println("Unknow choice " + choice + "\n");
                break;
        }
    }

    private void printMenu() {
        System.out.println("Menu");
        System.out.println("----\n");
        if (wallet == null) {
            System.out.println("1. Sign in");
            System.out.println("2. Sign up");
            System.out.println("3. Exit");
        } else {
            System.out.println("1. New transaction");
            System.out.println("2. Show wallet");
            System.out.println("3. Exit");
            System.out.println("4. Disconnect");
        }
        System.out.println(""); // Add empty line
        System.out.print("Please enter your choice : ");
    }
    
    private char[] askPassword(final String message) {
        char[] userPassword;
        if (console != null) {
            userPassword = console.readPassword(message);
        } else {
            System.out.print(message);
            userPassword = reader.next().toCharArray();
        }
        return userPassword;
    }

    public void signUp() throws InvalidKeySpecException, InvalidKeyException,
            InvalidParameterSpecException, IllegalBlockSizeException,
            BadPaddingException, FileNotFoundException, IOException {
        System.out.println("\n");
        System.out.println("Sign up");
        System.out.println("-------\n");

        // Ask the user to specify a wallet identifier
        System.out.print("Please choose a wallet identifier : ");
        final String accountName = reader.next();

        final File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        if (f.exists()) {
            System.out.println("The wallet identifier that you specified already exists, please sign in");

        } else {

            // Ask a password from the user
            char[] userPassword = null;
            boolean check = false;
            while (!check) {
                userPassword = this.askPassword("Enter your secret password: ");
                final char[] passwordChecker = this.askPassword("Confirm password : ");
                check = Arrays.equals(userPassword, passwordChecker);
                check = check && (userPassword != null);
            }

            // Create a new empty wallet and generate a key pair
            WalletClient.writeWalletFile(userPassword, accountName, Cryptography.generateKeyPair());
        }

    }

    public void signIn() throws FileNotFoundException, ClassNotFoundException, IOException,
            InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException,
            IllegalBlockSizeException {

        System.out.println("\n");
        System.out.println("Sign in");
        System.out.println("-------\n");

        // Ask the user to enter his wallet identifier
        System.out.print("Please enter your wallet identifier : ");
        final String accountName = reader.next();

        final File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        if (f.exists()) {
            // Ask the password of the user, and hides input
            // Using Console.readPassword because it is safer than reader.next() for 2 reasons:
            // 1) Hides user input, to protect from an attacker who potentially monitors the screen
            // 2) Returns a char array (no temporary String), to shorten the password lifetime
            //    in RAM, in case an attacker has access to it
            // Note: we use Console.readPassword only in console since IDEs
            //       do not work with consoles
            final char[] userPassword = this.askPassword("Enter your secret password: ");
            
            try {
                this.wallet = new WalletClient(f, userPassword);
            } catch (InstantiationException ex) {
                return; // Error when open wallet
            }
            RelayConnection.getInstance().sendData(new Message(Message.GET_TRANSACTIONS_FROM_WALLET, 
                wallet.getAddress().toJSON()));
        } else {
            System.out.println("The wallet identifier that you entered cannot be found");
        }
        
    }

    /**
     * Ask the user to create the transaction.<br>
     * It returns the checked transaction and -1 in the case that the
     * transaction was aborded.
     */
    public void createTransaction() {
        Transaction transaction = null;
        int amount = 0;
        do {
            System.out.println("Please enter the amount of the transaction,");
            System.out.println("Or enter -1 to join the main menu.");
            System.out.print("Amount : ");
            amount = reader.nextInt();
            if(amount == -1) {
                break;
            }
            
            final List<TransactionOutput> referencedOutput = wallet.getUsefulTransactions(amount);
            if (referencedOutput == null || referencedOutput.isEmpty()) {
                System.out.print("You don't have enough money.");
            } else {
                System.out.print("Destination : ");
                final PublicKey dstPublicKey = this.stringToKey(reader.next());
                if(dstPublicKey == null) {
                    System.out.println("Not a valid address...");
                    break;
                }
                final Address srcAddress = wallet.getAddress();
                final Address dstAddress = new Address(dstPublicKey);
                transaction = new Transaction(srcAddress,dstAddress,amount,referencedOutput);
                final char[] password = this.askPassword("Enter your secret password to confirm transaction: ");
                if(!wallet.signTransaction(password, transaction)) {
                    System.err.println("Could not sign transaction");
                } else {
                    try {
                        RelayConnection.getInstance().sendData(transaction);
                    } catch (IOException ex) {
                        Logger.getLogger(ClientApplication.class.getName()).log(Level.SEVERE, ex.getMessage());
                    }
                    wallet.addUnacceptedTransaction(transaction);
                }
            }
        } while (amount != -1);
    }
    
    private PublicKey stringToKey(final String text) {
        final byte[] key = JsonUtils.decodeBytes(text);
        return Cryptography.createPublicKeyFromBytes(key);
    }

    public void showWallet() {
        final ArrayList<Transaction> acceptedTransactionList = wallet.getAcceptedTransactions();
        final ArrayList<Transaction> unacceptedTransactionList = wallet.getUnacceptedTransactions();
        if(acceptedTransactionList.isEmpty() && unacceptedTransactionList.isEmpty()) {
            System.out.println("You are broke... (Wallet is empty)");
        } else {
            if(!acceptedTransactionList.isEmpty()) {
                acceptedTransactionList.forEach((transaction) -> {
                    System.out.println(transaction.toString());
                });
            }
            if(!unacceptedTransactionList.isEmpty()) {
                System.out.println("Unaccepted transaction:");
                unacceptedTransactionList.stream().forEach((unacceptTransaction) -> {
                    System.out.println(unacceptTransaction.toString());
                });
            }
            System.out.println("Total amount: " + wallet.getTotalAmount());
        }
    }
    
    public WalletClient getWallet() {
        return wallet;
    }
    
    public static ClientApplication getInstance() {
        return instance;
    }
    
}
