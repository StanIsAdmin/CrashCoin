package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.JsonUtils;
import be.ac.ulb.crashcoin.common.utils.Cryptography;
import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Entry point of the miner program.
 */
public class Main {

    // TODO store this at the right place
    private static final int difficulty = Parameters.MINING_DIFFICULTY;
    
    // TODO place in the configuration
    private static String userPrivateKeyStr = "";
    
    private static String userPseudonym;
    private static String userPassWord;
    
    public static PrivateKey privateKey() {
        final byte[] keyBytes = JsonUtils.decodeBytes(userPrivateKeyStr);
        return Cryptography.getPrivateKeyFomBytes(keyBytes);
    }

    // Temporay --- for test purposes
    public static Address getAddress() {
        KeyPairGenerator kpg = Cryptography.getDsaKeyGen();
        final KeyPair kp = kpg.generateKeyPair();
        final PublicKey pk = kp.getPublic();
        return new Address(pk);
    }

    public static void main(final String[] args) {
        
        try {
            userPseudonym = args[0];
            userPseudonym = args[1];
        } catch (ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
        
        RelayConnection connection;
        // Connect to relay
        try {
            connection = RelayConnection.getRelayConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
            return;
        }

        // create a miner... And start mining... Whut else?
        Miner miner;
        try {
            miner = Miner.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
            return;
        }
        try {
            miner.startMining();
        } catch (InterruptedException  ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
        }
    }

    public static int getDifficulty() {
        return difficulty;
    }

}
