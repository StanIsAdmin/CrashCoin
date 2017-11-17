package be.ac.ulb.crashcoin.client;

import java.io.IOException;
import java.io.File;
import java.util.Base64;
import java.util.Scanner;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;

/*
 * Handle IO from user
 * Handle network communication between nodes and wallet  
 */

public class ClientApplication {

    private Scanner reader = new Scanner(System.in);
    private Wallet wallet;

    public ClientApplication() throws IOException, NoSuchProviderException, NoSuchAlgorithmException {
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

    public void signUp() throws NoSuchProviderException, NoSuchAlgorithmException {
    	
    	System.out.println("\n");
        System.out.println("Sign up");
        System.out.println("-------\n");
        
        Boolean notExists = true;
                
    	// Ask the user to specify a wallet identifier
        System.out.print("Please specify a wallet identifier : ");
        String accountName = reader.next();
        
        File f = new File(Parameters.WALLETS_PATH + accountName + ".wallet");
        if(f.exists()) { 
            System.out.println("The wallet identifier that you specified already exists, please sign in");
            notExists = false;
        }
        
        if(notExists){
        	
        	// Create a new empty wallet and generate a key pair
            this.wallet = new Wallet();
            KeyPair keyPair = this.wallet.generateKeys();	// TODO change to generateKeys after commit

            Base64.Encoder encoder = Base64.getEncoder();
            Base64.Decoder decoder = Base64.getDecoder();
            
            // publicKey : X509 encoding  -  privateKey : PKCS8 encoding
            
            // ce qui doit être chiffré c'est : keyPair.getPrivate().getEncoded 
            String publicKey = encoder.encodeToString(keyPair.getPublic().getEncoded());
            
            System.out.println("Your public key is : " + publicKey);
        	
        }
        
    }

    public void signIn() {
        System.out.println("\n");
        System.out.println("Sign in");
        System.out.println("-------");

    }

    public void signOut() {
        // TODO
    }

    public void createTransaction() {

        /*
         * get input from client send transaction object + keypair to Wallet
         */

    }

}
