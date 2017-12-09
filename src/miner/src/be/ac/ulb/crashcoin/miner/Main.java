package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Wallet;
import be.ac.ulb.crashcoin.miner.net.RelayConnection;
import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.IllegalBlockSizeException;

/**
 * Entry point of the miner program.
 */
public class Main {
    
    private static Wallet userWallet;

    public static void main(final String[] args) {
        
        userWallet = new Wallet();
        try {
            userWallet.readWalletFile(args[1], args[0]);
        } catch (IOException | ClassNotFoundException | InvalidKeySpecException 
                | InvalidKeyException | InvalidAlgorithmParameterException 
                | IllegalBlockSizeException | ArrayIndexOutOfBoundsException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
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
    
    public static Wallet getUserWallet() {
        return userWallet;
    }
}
