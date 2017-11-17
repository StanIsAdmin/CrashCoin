package be.ac.ulb.crashcoin.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.util.Random;

import org.junit.jupiter.api.Test;

class TestWallet {
    /**
     * Creates an array of random bytes
     * 
     * @param nBytes Number of bytes to generate
     * @return Array of random bytes
     */
    public byte[] randomBytes(Integer nBytes) {
        byte[] bytes = new byte[nBytes];
        new Random().nextBytes(bytes);
        return bytes;
    }
    
    /**
     * Instantiates a wallet and returns it.
     * This is to save space in test methods.
     * 
     * @return A wallet
     */
    private Wallet createWallet() {
        Wallet wallet = null;
        try {
            wallet = new Wallet();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return wallet;
    }
    
    /**
     * Generates a pair of keys from a wallet and returns it.
     * This is to save space in test methods.
     * 
     * @return A pair of keys
     */
    private KeyPair createKeyPair(Wallet wallet) {
        KeyPair keyPair = null;
        try {
            keyPair = wallet.generateKeys();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchProviderException e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    @Test
    public void testValidSignature() {
        Wallet wallet = createWallet();
        KeyPair keyPair = createKeyPair(wallet);  
        
        byte[] transaction = randomBytes(50);
        byte[] signature = wallet.signTransaction(keyPair.getPrivate(), transaction);
        
        assertEquals(wallet.verifySignature(keyPair.getPublic(), transaction, signature), true);
    }
    
    @Test
    public void testBadPrivateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        // Let wallet and keyPairs be the wallet and the pair of keys associated to user's account
        // and stored on the hard drive.
        Wallet wallet = createWallet();
        KeyPair keyPair = createKeyPair(wallet);

        // Let's suppose that an attacker entered a bad password and thus, got a bad DSA private key from
        // the decryption algorithm.
        PrivateKey badPrivateKey = createWallet().generateKeys().getPrivate();
        
        // The offline software must check whether this key is wrong or not. Let's do this by signing a
        // test transaction (it can be anything, let's write random bytes) and verify the signature.
        byte[] transaction = randomBytes(156);
        byte[] badSignature = wallet.signTransaction(badPrivateKey, transaction);
        assertEquals(wallet.verifySignature(keyPair.getPublic(), transaction, badSignature), false);
    }
}