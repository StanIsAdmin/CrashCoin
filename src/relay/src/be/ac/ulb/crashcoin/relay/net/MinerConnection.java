package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import be.ac.ulb.crashcoin.relay.Main;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 * 
 */
class MinerConnection extends AbstractConnection {
    
    private static HashSet<MinerConnection> allMiner = new HashSet<>();
    
    public MinerConnection(final Socket sock) throws IOException {
        super("MinerConnection", sock);
        allMiner.add(this);
        
        start();
    }

    @Override
    protected void receiveData(String data) {
        System.out.println("[DEBUG] get value from miner/client: " + data);
        JSONObject jsonData = new JSONObject(data);
        // Local blockChain management
        // TODO check the received block once again ?
        // TODO place a condition on message type ? (eg. 'minedblock', 'transaction', ...)
        BlockChain chain = Main.getBlockChain();
        Block block = new Block(jsonData);
        chain.add(block);
        // Relay the data to the master node
        try {
            MasterConnection.getMasterConnection().sendData(jsonData);
        } catch (IOException ex) {
            Logger.getLogger(MinerConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    @Override
    protected void close() {
        super.close();
        allMiner.remove(this);
    }
    
    public static void sendToAll(final JSONObject data) {
        for(final MinerConnection relay : allMiner) {
            relay.sendData(data);
        }
    }
    
}
