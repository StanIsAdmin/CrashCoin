package be.ac.ulb.crashcoin.client;

import be.ac.ulb.crashcoin.common.utils.Cryptography;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.util.Random;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class TestWallet {

    /**
     * Creates an array of random bytes
     *
     * @param nBytes Number of bytes to generate
     * @return Array of random bytes
     */
    public byte[] randomBytes(final Integer nBytes) {
        final byte[] bytes = new byte[nBytes];
        new Random().nextBytes(bytes);
        return bytes;
    }

    /**
     * Instantiates a wallet and returns it. This is to save space in test
     * methods.
     *
     * @return A wallet
     */
    private Wallet createWallet() {
        return new Wallet();
    }

    /**
     * Generates a pair of keys from a wallet and returns it. This is to save
     * space in test methods.
     *
     * @return A pair of keys
     */
    private KeyPair createKeyPair(final Wallet wallet) {
        KeyPair keyPair = null;
        try {
            keyPair = wallet.generateKeys();
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            e.printStackTrace();
        }
        return keyPair;
    }

    @Test
    public void testValidSignature() {
        final Wallet wallet = createWallet();
        final KeyPair keyPair = createKeyPair(wallet);

        final byte[] transaction = randomBytes(50);
        final byte[] signature = Cryptography.signTransaction(keyPair.getPrivate(), transaction);

        assertEquals(wallet.verifySignature(keyPair.getPublic(), transaction, signature), true);
    }

    @Test
    public void testBadPrivateKey() throws NoSuchAlgorithmException, NoSuchProviderException {
        // Let wallet and keyPairs be the wallet and the pair of keys associated to user's account
        // and stored on the hard drive.
        final Wallet wallet = createWallet();
        final KeyPair keyPair = createKeyPair(wallet);

        // Let's suppose that an attacker entered a bad password and thus, got a bad DSA private key from
        // the decryption algorithm.
        final PrivateKey badPrivateKey = createWallet().generateKeys().getPrivate();

        // The offline software must check whether this key is wrong or not. Let's do this by signing a
        // test transaction (it can be anything, let's write random bytes) and verify the signature.
        final byte[] transaction = randomBytes(156);
        final byte[] badSignature = Cryptography.signTransaction(badPrivateKey, transaction);
        assertEquals(wallet.verifySignature(keyPair.getPublic(), transaction, badSignature), false);
    }
}
