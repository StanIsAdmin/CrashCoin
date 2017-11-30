package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import be.ac.ulb.crashcoin.relay.Main;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connection to master node<br>
 * It's a thread and a singleton
 */
public class MasterConnection extends AbstractReconnectConnection {
    
    private static MasterConnection instance = null;
    
    private MasterConnection() throws UnsupportedEncodingException, IOException {
        super("master", new Socket(Parameters.MASTER_IP, Parameters.MASTER_PORT_LISTENER));
        start();
    }
    
    @Override
    protected void receiveData(final JSONable jsonData) {
        System.out.println("[DEBUG] get value from master: " + jsonData);
        
        // Receive BlockChain (normaly, only at the first connection
        if(jsonData instanceof BlockChain) {
            Main.setBlockChain((BlockChain) jsonData);
            
        } else if(jsonData instanceof Block) { // Receive a mined block
            // local mined block management
            final Block block = (Block)jsonData;
            Main.getBlockChain().add(block);
            
            // Broadcast to the miners the validate/mined block so that they can
            // either remove the mined transaction from their pool or stop
            // the block mining if the transaction is in the block.
            MinerConnection.sendToAll(jsonData);
        }
        
    }
    
    @Override
    protected boolean canCreateNewInstance() {
        boolean isConnected;
        try {
            instance = new MasterConnection();
            isConnected = true;
        } catch(IOException ex) {
            isConnected = false;
        }
        return isConnected;
    }
    
    public static MasterConnection getMasterConnection() throws IOException {
        if(instance == null) {
            Logger.getLogger(MasterConnection.class.getName()).log(Level.INFO, "Test new connection to master");
            instance = new MasterConnection();
        }
        return instance;
    }
    
}
