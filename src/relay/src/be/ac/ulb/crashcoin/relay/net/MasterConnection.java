package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import be.ac.ulb.crashcoin.relay.Main;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

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
    protected void receiveData(final String data) {
        System.out.println("[DEBUG] get value from master: " + data);
        JSONObject jsonData = new JSONObject(data);
        if(jsonData.get("value") == "BlockChainUpdate") {
            // TODO add a condition to verify that we receive a blockchain
            // even though it is supposed to be the only kind of message expected ?
            // DO we receive a full block chain or a part of a block chain ?
            BlockChain chain = new BlockChain();
            BlockChain localChain = Main.getBlockChain();
            // localChain.update(chain);
        }
        else { // jsonData.get("value") != "BlockChainUpdate"
            System.out.println("[DEBUG] get value from master: " + data);
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
