package be.ac.ulb.crashcoin.client;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;

public class Wallet {
	
	private KeyPairGenerator dsaKeyGen;
	
	Wallet() {
		try {
			dsaKeyGen = KeyPairGenerator.getInstance("DSA");
		} catch (NoSuchAlgorithmException e) {
			System.out.println("[Error] Could not find DSA key-pair generator");
		}
	}
	
	KeyPair generateKey() {
		dsaKeyGen.initialize(Parameters.DSA_KEYS_N_BITS);
		KeyPair keyPair = dsaKeyGen.generateKeyPair();
		return keyPair;	
	}
}
