package be.ac.ulb.crashcoin.client;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;

import be.ac.ulb.crashcoin.data.Transaction;

public class Wallet {
	
	private PublicKey publicKey = null;
	private Signature dsa;
	private KeyPairGenerator dsaKeyGen;
	
	/**
	 * Constructs an empty wallet. This constructor behaves differently 
	 * if one passes a Keypair to it.
	 */
	public Wallet() {
		try {
			dsaKeyGen = KeyPairGenerator.getInstance("DSA");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("[Error] Could not find DSA key-pair generator");
		}
	}
	
	/**
	 * Constructs a wallet from a key pair. Only the public key is stored.
	 * For that reason, the key pair needs to be passed to signTransaction
	 * to be able to sign a transaction. After constructing a wallet using
	 * this constructor, one cannot generate keys with the same wallet
	 * anymore.
	 * 
	 * @param keyPair Pair of keys
	 * @see signTransaction
	 */
	public Wallet(KeyPair keyPair) {
		this();
		this.publicKey = keyPair.getPublic();
	}
	
	/**
	 * Generates a DSA key pair, composed of a public key and a private key.
	 * The key size is defined in parameters.
	 * This method can be called at most one time per wallet.
	 * 
	 * @return Pair of DSA keys
	 */
	public KeyPair generateKey() {
		if (publicKey != null) {
			System.out.println("[Error] Only one key pair can be assigned to a wallet");
			return null;
		}
		dsaKeyGen.initialize(Parameters.DSA_KEYS_N_BITS);
		KeyPair keyPair = dsaKeyGen.generateKeyPair();
		this.publicKey = keyPair.getPublic();
		return keyPair;	
	}
	
	/**
	 * Returns a transaction signature using DSA algorithm.
	 * 
	 * @param keyPair
	 * @param transaction
	 * @return transaction signature
	 */
	public byte[] signTransaction(KeyPair keyPair, Transaction transaction) {
		try {
			dsa = Signature.getInstance("SHA1withDSA", "SUN");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("[Error] Could not find DSA signature algorithm");
		} catch (NoSuchProviderException e) {
			System.out.println("[Error] Could not find provider for DSA");
		}
		
		try {
			dsa.initSign(keyPair.getPrivate());
		} catch (InvalidKeyException e1) {
			e1.printStackTrace();
		}
		
		byte[] signature = null;
		byte[] bytes = transaction.toBytes();
		try {
			dsa.update(bytes, 0, bytes.length);
			signature = dsa.sign();
		} catch (SignatureException e) {
			e.printStackTrace();
		}
		return signature;
	}
	
	/** Get the unique public key */
	public PublicKey getPublicKey() {
		return publicKey;
	}
}