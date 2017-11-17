package be.ac.ulb.crashcoin.client;

import java.io.IOException;
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
			if (choice == 1)
				signIn();
			else if (choice == 2)
				signUp();

		} while (choice != 3);

		reader.close();
	}

	public void signUp() throws NoSuchProviderException, NoSuchAlgorithmException {

		// Create a new empty wallet and generate a key pair
		this.wallet = new Wallet();
		KeyPair keyPair = this.wallet.generateKey();

		// Account name = public key in hexa
		Base64.Encoder encoder = Base64.getEncoder();

		String publicKey = encoder.encodeToString(keyPair.getPublic().getEncoded());

		System.out.println("\n");
		System.out.println("Sign up");
		System.out.println("-------\n");
		System.out.println("Your public key is : " + publicKey);

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
