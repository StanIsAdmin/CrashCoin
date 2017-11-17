package be.ac.ulb.crashcoin.client;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;

import be.ac.ulb.crashcoin.data.Transaction;

public class Wallet {

    private PublicKey publicKey = null;
    private KeyPairGenerator dsaKeyGen;

    /**
     * Constructs an empty wallet. This constructor behaves differently if one
     * passes a Keypair to it.
     * 
     * @throws NoSuchProviderException
     */
    public Wallet() throws NoSuchProviderException {
        try {
            dsaKeyGen = KeyPairGenerator.getInstance("DSA", "SUN");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[Error] Could not find DSA key-pair generator");
        }
    }

    /**
     * Constructs a wallet from a key pair. Only the public key is stored. For that
     * reason, the key pair needs to be passed to signTransaction to be able to sign
     * a transaction. After constructing a wallet using this constructor, one cannot
     * generate keys with the same wallet anymore.
     * 
     * @param keyPair
     *            Pair of keys
     * @throws NoSuchProviderException
     * @see signTransaction
     */
    public Wallet(KeyPair keyPair) throws NoSuchProviderException {
        this();
        this.publicKey = keyPair.getPublic();
    }

    /**
     * Generates a DSA key pair, composed of a public key and a private key. The key
     * size is defined in parameters. This method can be called at most one time per
     * wallet.
     * 
     * @return Pair of DSA keys
     * @throws NoSuchAlgorithmException
     * @throws NoSuchProviderException
     */
    public KeyPair generateKeys() throws NoSuchAlgorithmException, NoSuchProviderException {
        if (publicKey != null) {
            System.out.println("[Error] Only one key pair can be assigned to a wallet");
            return null;
        }
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
        dsaKeyGen.initialize(Parameters.DSA_KEYS_N_BITS, random);
        KeyPair keyPair = dsaKeyGen.generateKeyPair();
        this.publicKey = keyPair.getPublic();
        return keyPair;
    }

    public ArrayList<Transaction> getTransactions() {
        // TODO: ask for the blockchain if it is not in memory
        // TODO: look for all the transactions containing my
        // address as src or dest, and return them
        return null; // TODO
    }
    
    public Signature dsaFromPrivateKey(PrivateKey privateKey) {
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException e) {
            System.out.println("[Error] Could not find DSA signature algorithm");
        } catch (NoSuchProviderException e) {
            System.out.println("[Error] Could not find provider for DSA");
        }

        try {
            // Using private key to sign with DSA
            dsa.initSign(privateKey);
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        }
        return dsa;
    }
    
    public Signature dsaFromPublicKey(PublicKey publicKey) {
        Signature dsa = null;
        try {
            dsa = Signature.getInstance("SHA1withDSA", "SUN");
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        } catch (NoSuchProviderException e2) {
            e2.printStackTrace();
        }
        try {
            // Using public key to verify signatures
            dsa.initVerify(publicKey);
        } catch (InvalidKeyException e1) {
            e1.printStackTrace();
        }
        return dsa;
    }

    /**
     * Returns a transaction signature using DSA algorithm.
     * 
     * @param keyPair
     * @param transaction
     * @return transaction signature
     */
    public byte[] signTransaction(PrivateKey privateKey, byte[] bytes) {
        Signature dsa = dsaFromPrivateKey(privateKey);
        byte[] signature = null;
        try {
            // Running DSA
            dsa.update(bytes, 0, bytes.length);
            signature = dsa.sign();
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return signature;
    }
    
    public boolean verifySignature(PublicKey publicKey, byte[] transaction, byte[] signature) {        
        Signature dsa = dsaFromPublicKey(publicKey);
        
        boolean verified = false;
        try {
            dsa.update(transaction, 0, transaction.length);
            verified = dsa.verify(signature);
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return verified;
    }

    /** Get the unique public key */
    public PublicKey getPublicKey() {
        return publicKey;
    }
}
