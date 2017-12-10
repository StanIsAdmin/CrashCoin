package be.ac.ulb.crashcoin.relay;

import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.relay.net.MasterConnection;
import be.ac.ulb.crashcoin.relay.net.MinerListener;
import be.ac.ulb.crashcoin.relay.net.WalletListener;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Main {

    private static BlockChain _blockChain;
    private static String ip;
    private static Integer port;

    public static void main(final String[] argv) {

        _blockChain = new BlockChain();
        
        if(argv.length >= 2) {
            ip = argv[0];
            port = Integer.parseInt(argv[1]);
        } else {
            Logger.getLogger(Main.class.getName()).log(Level.INFO, "Default ip and port were applied.");
            ip = Parameters.MASTER_IP;
            port = Parameters.MASTER_PORT_LISTENER;
        }

        // Enable listener
        try {
            MinerListener.getListener();
            WalletListener.getListener();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }

        // Connect to master
        try {
            MasterConnection.getMasterConnection();
        } catch (IOException ex) {
            Logger.getLogger(Main.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static BlockChain getBlockChain() {
        return _blockChain;
    }

    public static void setBlockChain(final BlockChain blockChain) {
        _blockChain = blockChain;
    }
    
    public static String getIP() {
        return ip;
    }
    
    public static Integer getPort() {
        return port;
    }

}
