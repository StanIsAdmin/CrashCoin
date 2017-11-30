package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import java.io.IOException;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.sql.Timestamp;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import be.ac.ulb.crashcoin.common.Transaction;
import java.io.Console;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.util.ArrayList;
import java.util.Arrays;


/**
 * Handle IO from user 
 * Handle network communication between nodes and wallet  
 */
public class ClientApplication {

    private final Console console;
    private final Scanner reader = new Scanner(System.in);
    private Wallet wallet;
    private boolean registered;
    
    public ClientApplication() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, 
            InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, 
            IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ClassNotFoundException {
        
        console = System.console();
        registered = false;
        int choice;
        do {
            printMenu();
            
            if(reader.hasNextInt()) {
                choice = reader.nextInt();
            } else {
                choice = -1;
                System.out.println("You must choose a number !\n");
                reader.next();
            }
            
            if(choice > 0) {
                if(!registered) { // If not register/login
                    actionMenuNotRegistered(choice);
                } else { // If login/register
                    actionMenuRegistred(choice);
                }
            }
            
        } while(choice != 3);
        reader.close();
    }
    
    private void actionMenuNotRegistered(final int choice) throws ClassNotFoundException, IOException, FileNotFoundException, 
            NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, 
            InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException, 
            InvalidParameterSpecException {
        switch(choice) {
            case 1:
                signIn();
                registered = true;
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
        switch(choice) {
            case 1:
                createTransaction();
                break;

            case 2:
                showWallet();
                break;

            case 4:
                registered = false;
                break;

            case 3: // close with condition in while
                break;

            default:
                System.out.println("Unknow choice " + choice + "\n");
                break;
        }
    }
    
    private void printMenu() {
        System.out.println("Menu");
        System.out.println("----\n");
        if (!registered) {
            System.out.println("1. Sign in");
            System.out.println("2. Sign up");
            System.out.println("3. Exit");
        } else {
            System.out.println("1. New transaction");
            System.out.println("2. Show wallet");
            System.out.println("3. Exit");
            System.out.println("4. Disconnect");
        }
        System.out.println(""); // Add space
        System.out.print("Please enter your choice : ");
    }
    

    public void signUp() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, 
            NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, 
            BadPaddingException, InvalidAlgorithmParameterException, FileNotFoundException, IOException {
    	
    	System.out.println("\n");
        System.out.println("Sign up");
        System.out.println("-------\n");
        
    	// Ask the user to specify a wallet identifier
        System.out.print("Please choose a wallet identifier : ");
        final String accountName = reader.next();
        
        final File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        if(f.exists()) { 
        	System.out.println("The wallet identifier that you specified already exists, please sign in");
        	
        } else {
            
            // Ask a password from the user
            char[] userPassword = null;
            boolean check = false;
            while (!check) {
                char[] passwordChecker;
                System.out.print("Password : ");
                if (console != null) {
                    userPassword = console.readPassword();
                } else {
                    userPassword = reader.next().toCharArray();
                }
                System.out.print("Confirm password : ");
                if (console != null) {
                    passwordChecker = console.readPassword();
                } else {
                    passwordChecker = reader.next().toCharArray();
                }                
                check = Arrays.equals(userPassword, passwordChecker);
                check = check && (userPassword != null);
            }
        	
            // Create a new empty wallet and generate a key pair
            this.wallet = new Wallet();
            wallet.writeWalletFile(userPassword, accountName);
        }
        
    }

    public void signIn() throws FileNotFoundException, ClassNotFoundException, IOException, NoSuchAlgorithmException, 
            NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, 
            IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
    	
        System.out.println("\n");
        System.out.println("Sign in");
        System.out.println("-------\n");
        
        // Ask the user to enter his wallet identifier
        System.out.print("Please enter your wallet identifier : ");
        final String accountName = reader.next();
        
        final File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        if(f.exists()) {
            // Ask the password of the user, and hides input
            // Using Console.readPassword because it is safer than reader.next() for 2 reasons:
            // 1) Hides user input, to protect from an attacker who potentially monitors the screen
            // 2) Returns a char array (no temporary String), to shorten the password lifetime
            //    in RAM, in case an attacker has access to it
            // Note: we use Console.readPassword only in console since IDEs
            //       do not work with consoles
            char[] userPassword;
            if (console != null) { // If using a console
                userPassword = console.readPassword("Enter your secret password: ");
            } else { // Is using an IDE
                System.out.print("Please enter your password: ");
                userPassword = reader.next().toCharArray();   
            }
            this.wallet = Wallet.readWalletFile(f, userPassword);  
        } else {
        	System.out.println("The wallet identifier that you entered cannot be found");
        	
        }

    }
    
    // TODO method for debug and must me be move
    public static String byteArrayToHex(final byte[] a) {
        final StringBuilder sb = new StringBuilder(a.length * 2);
        for(final byte b: a) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    // TODO method for debug and must me be move
    public static byte[] hexToByteArray(final String strOfHex) {
        // TODO
        return new byte[] {};
    }


    
    
    /**
     * Ask the user to create the transaction.<br>
     * It returns the checked transaction and -1 in the case that the 
     * transaction was aborded.
     * 
     * @return The created transaction
     */
    public Transaction createTransaction() {
        Transaction result = null;
        Transaction transaction;
        int amount = 0;
        byte[] destAdress = null;
        
        do {
            System.out.println("Please enter the amount of the transaction,");
            System.out.println("Or enter -1 to escape the curent transaction.");
            System.out.print("Amount : ");
            amount = reader.nextInt();
            
            System.out.print("Destination : ");
            String strDest = reader.next();
            destAdress = hexToByteArray(strDest);
            
            final Address srcAddress = new Address(wallet.getPublicKey());
            final Timestamp lockTime = new Timestamp(System.currentTimeMillis());
            // TODO create output
            // Transaction.Output
            transaction = new Transaction(srcAddress, amount, lockTime);
        } while (!(transaction.isValid()) && amount != -1);
        
        if (amount != -1) {
            result = transaction;
        }
        
    	return result;
    }
    
    public void showWallet() {
        ArrayList<Transaction> transactionList = wallet.getTransactions();
        for (Transaction transaction: transactionList) {
            System.out.println(transaction.toString());
        }
    }

}
