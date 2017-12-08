package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.fail;

/**
 * Entry point of the miner program.
 */
public class Main {

    // TODO store this at the right place
    private static int difficulty = Parameters.MINING_DIFFICULTY;
    
    private static byte[] lastHashBlockInChain;
    
    // TODO place in the configuration
    private static String userPrivateKey = "";
    
    public static PrivateKey privateKey() {
        final byte[] keyBytes = JsonUtils.decodeBytes(userPrivateKey);
        final X509EncodedKeySpec ks = new X509EncodedKeySpec(keyBytes);
        final KeyFactory kf;
        PrivateKey pv = null;
        try {
            kf = KeyFactory.getInstance("DSA");
            pv = kf.generatePrivate(ks);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException ex) {
            Logger.getLogger(Address.class.getName()).log(Level.SEVERE, null, ex);
        }
        return pv;
    }

    // Temporay --- for test purposes
    public static Address getAddress() {
        KeyPairGenerator kpg = null;
        try {
            kpg = KeyPairGenerator.getInstance("DSA");
        } catch (NoSuchAlgorithmException e) {
            fail("Could not create key pair generator");
        }
        final KeyPair kp = kpg.generateKeyPair();
        final PublicKey pk = kp.getPublic();
        return new Address(pk);
    }

    public static void main(final String[] args) {

        RelayConnection connection;
        // Connect to relay
        try {
            connection = RelayConnection.getRelayConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }

        // create a miner... And start mining... Whut else?
        Miner miner;
        try {
            miner = Miner.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
            return;
        }
        try {
            miner.startMining();
        } catch (InterruptedException  ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static int getDifficulty() {
        return difficulty;
    }

}
