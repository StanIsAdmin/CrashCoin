package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

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
    protected void receiveData(final JSONable jsonData) {
        System.out.println("[DEBUG] get value from miner/client: " + jsonData);
        
        if(jsonData instanceof Block) {
            final Block block = (Block) jsonData;
            
            // TODO: voir question #todo Slack - Local blockChain management
//            final BlockChain chain = Main.getBlockChain();
//            chain.add(block);
            
            // Relay the data to the master node
            try {
                MasterConnection.getMasterConnection().sendData(block);
            } catch (IOException ex) {
                Logger.getLogger(MinerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        
    }
    
    @Override
    protected void close() {
        super.close();
        allMiner.remove(this);
    }
    
    public static void sendToAll(final JSONable data) {
        for(final MinerConnection relay : allMiner) {
            relay.sendData(data);
        }
    }
    
}
