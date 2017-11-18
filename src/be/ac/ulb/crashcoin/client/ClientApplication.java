package be.ac.ulb.crashcoin.client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

import be.ac.ulb.crashcoin.data.Transaction;

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

import java.util.Random;

/*
 * Handle IO from user 
 * Handle network communication between nodes and wallet  
 */

public class ClientApplication {

    private Scanner reader = new Scanner(System.in);
    private Wallet wallet;

    public ClientApplication() throws IOException, NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, ClassNotFoundException {
        int choice;
        do {
            System.out.println("Menu");
            System.out.println("----\n");
            System.out.println("1. Sign in");
            System.out.println("2. Sign up");
            System.out.println("3. Exit\n");
            System.out.print("Please enter your choice : ");

            choice = reader.nextInt();
            if(choice == 1)
                signIn();
            else if(choice == 2)
                signUp();

        } while(choice != 3);

        reader.close();
    }

    public void signUp() throws NoSuchProviderException, NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, InvalidParameterSpecException, IllegalBlockSizeException, BadPaddingException, InvalidAlgorithmParameterException, FileNotFoundException, IOException {
    	
    	System.out.println("\n");
        System.out.println("Sign up");
        System.out.println("-------\n");
        
    	// Ask the user to specify a wallet identifier
        System.out.print("Please choose a wallet identifier : ");
        String accountName = reader.next();
        
        File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        if(f.exists()) { 
        	
        	System.out.println("The wallet identifier that you specified already exists, please sign in");
        	
        }
        else {
        	       	
        	// Ask a password from the user (// TODO hidden password + confirmation)
        	System.out.print("Please enter the desired password: ");
        	String userPassword = reader.next();
        	
        	// Create a new empty wallet and generate a key pair
            this.wallet = new Wallet();
            KeyPair keyPair = this.wallet.generateKeys();
            
            PublicKey publicKey = keyPair.getPublic();
            PrivateKey privateKey = keyPair.getPrivate();
            
            // Get the bytes representation of the keys
            byte[] publicKeyBytes = publicKey.getEncoded();
            byte[] privateKeyBytes = privateKey.getEncoded();
            
  
            // Encrypt the private key using AES-128 protocol with the user password
            
            // Compute a salt to avoid dictionary attacks (to turn a password into a secret key)
            // The salt is not kept secret but is needed for decryption
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[Parameters.SALT_SIZE];
            random.nextBytes(salt);
            
            SecretKey encryptionKey = this.computeSecretKey(userPassword, salt);
            
            // Encrypt the private key with the encryption key generated from the user password
            
            // Initialize a cipher to the encryption mode with the encryptionKey
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, encryptionKey);
            
            // Get the IV necessary to decrypt the message later
            // In CBC mode, each block is XORed with the output of the previous block
            // The IV represents the arbitrary "previous block" to be used for the first block
            AlgorithmParameters parameters = cipher.getParameters();
            byte[] iv = parameters.getParameterSpec(IvParameterSpec.class).getIV();
            
            // Encrypt the private key
            byte[] encryptedPrivateKey = cipher.doFinal(privateKeyBytes);
            
            // Write wallet information in the wallet file
            WalletInformation walletInformation = new WalletInformation(salt, iv, encryptedPrivateKey, publicKeyBytes);
            this.writeWalletFile(walletInformation, accountName);
            
            System.out.println("The creation of your wallet completed successfully");
            System.out.println("Please sign in and start crashing coins");
          
        }
        
    }
    
    private SecretKey computeSecretKey(String userPassword, byte[] salt) throws NoSuchAlgorithmException, InvalidKeySpecException {
    	
    	/*
    	 * Compute an encryption / decryption key (they are the same) from the password and the salt
    	 * 
    	 * Information: PBKDF2 is a password-based key derivation function
        	 			Used PBKDF2WithHmacSHA1 instead of PBKDF2WithHmacSHA256 because of some problems 
        				if we run the project on java <= 7 (plus the guidelines say 128 bits so it's ok) 
    	 */
    	
        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
        KeySpec spec = new PBEKeySpec(userPassword.toCharArray(), salt, Parameters.KEY_DERIVATION_ITERATION, Parameters.KEY_SIZE);
        SecretKey tmpKey = factory.generateSecret(spec);
        SecretKey secretKey = new SecretKeySpec(tmpKey.getEncoded(), "AES");
        
        return(secretKey);
    	
    }
    
    public void writeWalletFile(WalletInformation walletInformation, String accountName) throws FileNotFoundException, IOException {
    	// Creates wallet folder if not exists
        File walletFolder = new File(Parameters.WALLETS_PATH);
        if (!walletFolder.exists()) walletFolder.mkdir();
        
        // Creates new wallet file
    	File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
    	ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(f));
    	oos.writeObject(walletInformation);
    	oos.flush();
    	oos.close();
    	
    }
    
    public WalletInformation readWalletFile(File f) throws FileNotFoundException, IOException, ClassNotFoundException {
    	
    	ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
    	WalletInformation walletInformation = (WalletInformation) ois.readObject();
    	ois.close();
    	
    	return(walletInformation);
    }
    
    private KeyPair createKeyPairFromEncodedKeys(byte[] publicKeyBytes, byte[] privateKeyBytes) throws NoSuchAlgorithmException, InvalidKeySpecException {
    	
    	/*
    	 * Convert encoded private and public keys (bytes) to Private / PublicKey interfaces
    	 * and generate a KeyPair from them in order to construct a Wallet object in the signIn method
    	 * 
    	 * /!\ Two different encoding: publicKey: X509 encoding  -  privateKey: PKCS8 encoding /!\
    	 */
        
    	// Generate specs
    	X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicKeyBytes);
        PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(privateKeyBytes);
        
        KeyFactory factory = KeyFactory.getInstance("DSA");
        
        // Create PublicKey and PrivateKey interfaces using the factory
        PrivateKey privateKey = factory.generatePrivate(privateKeySpec);
        PublicKey publicKey = factory.generatePublic(publicKeySpec);
        
        return(new KeyPair(publicKey, privateKey));
        
    }

    public void signIn() throws FileNotFoundException, ClassNotFoundException, IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException, InvalidAlgorithmParameterException, InvalidKeySpecException, IllegalBlockSizeException, BadPaddingException, NoSuchProviderException {
    	
        System.out.println("\n");
        System.out.println("Sign in");
        System.out.println("-------\n");
        
        // Ask the user to enter his wallet identifier
        System.out.print("Please enter your wallet identifier : ");
        String accountName = reader.next();
        
        File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        if(f.exists()) { 
        	
        	// Retrieve wallet information stored on disk 
        	
        	WalletInformation walletInformation = readWalletFile(f);
        	byte[] salt = walletInformation.getSalt();
        	byte[] iv = walletInformation.getIv();
        	byte[] encryptedPrivateKey = walletInformation.getEncryptedPrivateKey();
        	byte[] publicKeyBytes = walletInformation.getPublicKey();	
        	
        	// Ask the password of the user (// TODO hidden password)
        	System.out.print("Please enter your password: ");
        	String userPassword = reader.next();
        	
        	SecretKey decryptionKey = this.computeSecretKey(userPassword, salt);
        	
        	// Decrypt the private key with the decryption key generated from the user password
            
            // Initialize a cipher to the decryption mode with the decryptionKey
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            cipher.init(Cipher.DECRYPT_MODE, decryptionKey, new IvParameterSpec(iv));
        	
            // Decrypt the private key
            try {
            	
            	byte[] privateKeyBytes = cipher.doFinal(encryptedPrivateKey);
            	
            	// Create a PairKey with the encoded public and private keys
                KeyPair keyPair = this.createKeyPairFromEncodedKeys(publicKeyBytes, privateKeyBytes);          
                
                
                // Create a Wallet object with the KeyPair
                this.wallet = new Wallet(keyPair);
                
                
                // Verify the private key generated by the password entered by the user
                
                if(this.verifyPrivateKey(keyPair)) {
                	
                	System.out.println("Welcome in your wallet!");
                	
                } 
                else {
                	
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
            
        }
        else {
        	
        	System.out.println("The wallet identifier that you entered cannot be found");
        	
        }

    }

    public Boolean verifyPrivateKey(KeyPair keyPair) {
    	
    	/*
         * The objective of this method is to verify that the private key that we've just decrypted
         * in the login method using the user password is the valid one. If the user entered a wrong
         * password the decryption cipher would still produce some results, i.e. a wrong private key
         * 
         * To solve this, Antoine proposed to create a fake local trasaction with the private key
         * that we've just decrypted and verify it with the public key associated with the user.
         * If the transaction cannot be verified with the public key, then the private key and the 
         * password were wrong.
         * 
         */

    	Boolean verified;
    	PrivateKey privateKey = keyPair.getPrivate();
    	PublicKey publicKey = keyPair.getPublic();
    	
    	// Create dummy transaction
    	byte[] dummyTransaction = new byte[50];
    	new Random().nextBytes(dummyTransaction);
    	
    	// Sign the dummy transaction with the private key that we want to verify
    	byte[] dummySignature = this.wallet.signTransaction(privateKey, dummyTransaction);
    	
    	// Verify the signature using the public key and the specific Wallet method
    	verified = this.wallet.verifySignature(publicKey, dummyTransaction, dummySignature);
    	
    	return(verified);

    }
    
    public Transaction createTransaction() {
		    	
    	/*
         * Get input from client and return a transaction object
         * 
         * TODO
         */
    	
    	return null;
    	
    }

}
