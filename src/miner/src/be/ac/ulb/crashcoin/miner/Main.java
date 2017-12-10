package be.ac.ulb.crashcoin.miner;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Parameters;
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
    
    private static String ip = Parameters.RELAY_IP;
    private static int port = Parameters.RELAY_PORT_MINER_LISTENER;
    private static Wallet userWallet;

    public static void main(final String[] args) {
            
        final String user;
        final String password;
        
        if(args.length < 2) {
            Logger.getLogger(Main.class.getName()).warning("You must specified the user and the user who will "
                    + "receive the reward");
            Logger.getLogger(Main.class.getName()).info("Use: java -jar dist/miner.jar <user> <password> [ip] [port]");
            return;
        } else {
            user = args[0];
            password = args[1];
            
            if(args.length >= 3) {
                ip = args[2];
                
                if(args.length >= 4) {
                    port = Integer.parseInt(args[3]);
                }
                
            } else {
                Logger.getLogger(Main.class.getName()).log(Level.INFO, "Default ip and port were applied.");
            }
        }
        
        try {
            userWallet = new Wallet(user, password.toCharArray());
        } catch (IOException | ClassNotFoundException | InvalidKeySpecException 
                | InvalidKeyException | InvalidAlgorithmParameterException 
                | IllegalBlockSizeException | InstantiationException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
            return;
        }
        
        // Connect to relay
        try {
            RelayConnection.getRelayConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
            System.exit(1);
        }

        // create a miner... And start mining... Whut else?
        final Miner miner;
        try {
            miner = Miner.getInstance();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
            System.exit(1);
            return;
        }
        try {
            miner.startMining();
        } catch (InterruptedException  ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, ex.getMessage());
            System.exit(1);
        }
    }
    
    public static Address getUserAddress() {
        return userWallet.getAddress();
    }
    
    public static String getIp() {
        return ip;
    }

    public static int getPort() {
        return port;
    }
}
