package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;
import java.sql.Timestamp;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import be.ac.ulb.crashcoin.common.Transaction;
import java.io.Console;

import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.InvalidParameterSpecException;
import java.security.spec.KeySpec;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Arrays;

import java.util.Random;

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
            
            choice = reader.nextInt();
            
            if(!registered) { // If not register/login
                actionMenuNotRegistered(choice);
                
            } else {
                choice = actionMenuRegistred(choice);
            }
            
        } while(!( choice == 3 && registered == false));
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
                System.out.println("Unknow choice " + choice);
                break;

        }
    }
    
    private int actionMenuRegistred(int choice) {
        switch(choice) {
            case 1:
                createTransaction();
                break;

            case 2:
                // TODO : show wallet;
                break;

            case 4:
                registered = false;
                choice = 0;
                break;

            case 3: // close with condition in while
                break;

            default:
                System.out.println("Unknow choice " + choice);
                break;
        }
        return choice;
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
        System.out.println("3. Exit\n");
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
            final KeyPair keyPair = this.wallet.generateKeys();
            
            final PublicKey publicKey = keyPair.getPublic();
            final PrivateKey privateKey = keyPair.getPrivate();
            
            // Get the bytes representation of the keys
            final byte[] publicKeyBytes = publicKey.getEncoded();
            final byte[] privateKeyBytes = privateKey.getEncoded();
            
  
            // Encrypt the private key using AES-128 protocol with the user password
            
            // Compute a salt to avoid dictionary attacks (to turn a password into a secret key)
            // The salt is not kept secret but is needed for decryption
            final SecureRandom random = new SecureRandom();
            final byte[] salt = new byte[Parameters.SALT_SIZE];
            random.nextBytes(salt);
            
            final SecretKey encryptionKey = this.computeSecretKey(userPassword, salt);
            
            // Encrypt the private key with the encryption key generated from the user password
            
            // Initialize a cipher to the encryption mode with the encryptionKey
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
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
            this.writeWalletFile(walletInformation, accountName);
            
            System.out.println("The creation of your wallet completed successfully");
            System.out.println("Please sign in and start crashing coins");
          
        }
        
    }
    
    
    /**
     * Compute an encryption / decryption key (they are the same) from the password and the salt<br>
     * 
     * Information: PBKDF2 is a password-based key derivation function
     *             Used PBKDF2WithHmacSHA1 instead of PBKDF2WithHmacSHA256 because of some problems 
     *             if we run the project on java <= 7 (plus the guidelines say 128 bits so it's ok) 
     * 
     * @param userPassword password of user
     * @param salt extended string
     * @return SecretKey
     */
    private SecretKey computeSecretKey(final char[] userPassword, final byte[] salt) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
    	
        final SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        final KeySpec spec = new PBEKeySpec(userPassword, salt, Parameters.KEY_DERIVATION_ITERATION, Parameters.KEY_SIZE);
        final SecretKey tmpKey = factory.generateSecret(spec);
        final SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");
        
        return(secretKey);
    }
    
    public void writeWalletFile(final WalletInformation walletInformation, final String accountName) 
            throws FileNotFoundException, IOException {
    	// Creates wallet folder if not exists
        final File walletFolder = new File(Parameters.WALLETS_PATH);
        if (!walletFolder.exists()) walletFolder.mkdir();
        
        // Creates new wallet file
    	final File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
    	final ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
    	oos.writeObject(walletInformation);
    	oos.flush();
    	oos.close();
    }
    
    public WalletInformation readWalletFile(final File f) throws FileNotFoundException, IOException, ClassNotFoundException {
    	final ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
    	final WalletInformation walletInformation = (WalletInformation) ois.readObject();
    	ois.close();
    	
    	return(walletInformation);
    }
    
    /**
     * Convert encoded private and public keys (bytes) to Private / PublicKey interfaces
     * and generate a KeyPair from them in order to construct a Wallet object in the signIn method<br>
     * <b>Two different encoding</b>
     * 
     * @param publicKeyBytes the public key with encoding X509
     * @param privateKeyBytes the private key with encoding PKCS8
     * @return the key pair
     */
    private KeyPair createKeyPairFromEncodedKeys(final byte[] publicKeyBytes, final byte[] privateKeyBytes) 
            throws NoSuchAlgorithmException, InvalidKeySpecException {
    	
    	// Generate specs
    	final X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        final PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        
        final KeyFactory factory = KeyFactory.getInstance("DSA");
        
        // Create PublicKey and PrivateKey interfaces using the factory
        final PrivateKey privateKey = factory.generatePrivate(privateKeySpec);
        final PublicKey publicKey = factory.generatePublic(publicKeySpec);
        
        return(new KeyPair(publicKey, privateKey));
        
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
        	
        	// Retrieve wallet information stored on disk 
        	
        	final WalletInformation walletInformation = readWalletFile(f);
        	final byte[] salt = walletInformation.getSalt();
        	final byte[] iv = walletInformation.getIv();
        	final byte[] encryptedPrivateKey = walletInformation.getEncryptedPrivateKey();
        	final byte[] publicKeyBytes = walletInformation.getPublicKey();	
        	
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
        	
        	final SecretKey decryptionKey = this.computeSecretKey(userPassword, salt);
        	
        	// Decrypt the private key with the decryption key generated from the user password
            
            // Initialize a cipher to the decryption mode with the decryptionKey
            final Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new IvParameterSpec(iv));
        	
            // Decrypt the private key
            try {
            	
            	final byte[] privateKeyBytes = cipher.doFinal(encryptedPrivateKey);
            	
            	// Create a PairKey with the encoded public and private keys
                final KeyPair keyPair = this.createKeyPairFromEncodedKeys(publicKeyBytes, privateKeyBytes);          
                
                
                // Create a Wallet object with the KeyPair
                this.wallet = new Wallet(keyPair);
                
                
                // Verify the private key generated by the password entered by the user
                
                if(this.verifyPrivateKey(keyPair)) {
                	
                	System.out.println("Welcome in your wallet!");
                	
                } else {
                	
                	System.out.println("The password you entered is incorrect");
                	
                }
            	
            } catch(BadPaddingException e) {
            	
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
            
        } else {
        	System.out.println("The wallet identifier that you entered cannot be found");
        	
        }

    }

    /**
     * The objective of this method is to verify that the private key that we've just decrypted
     * in the login method using the user password is the valid one. If the user entered a wrong
     * password the decryption cipher would still produce some results, i.e. a wrong private key
     * 
     * To solve this, Antoine proposed to create a fake local trasaction with the private key
     * that we've just decrypted and verify it with the public key associated with the user.
     * If the transaction cannot be verified with the public key, then the private key and the 
     * password were wrong.
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
    	final byte[] dummySignature = this.wallet.signTransaction(privateKey, dummyTransaction);
    	
    	// Verify the signature using the public key and the specific Wallet method
    	verified = this.wallet.verifySignature(publicKey, dummyTransaction, dummySignature);
    	
    	return(verified);
    }
    
    public Transaction createTransaction() {
	/**
         * Ask the user to create the transaction.
         * It returns the checked transaction and -1 in the case that the 
         * transaction was aborded.
         */
        Transaction transaction = null;
        int amount = 0;
        do {
            System.out.println("Please enter the amount of the transaction,");
            System.out.println("Or enter -1 to escape the curent transaction.");
            System.out.println("Amount : ");
            amount = reader.nextInt();
            final Address srcAddress = new Address(wallet.getPublicKey());
            final Timestamp lockTime = new Timestamp(System.currentTimeMillis());
            transaction = new Transaction(srcAddress, amount, lockTime);
        } while (!(transaction.isValid()) && amount != -1);
        if (amount != -1) {
            return transaction;
        }    	
    	return null;
    	
    }

}
