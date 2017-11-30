package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import be.ac.ulb.crashcoin.master.Main;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashSet;

/**
 * Connection to a Relay
 */
public class RelayConnection extends AbstractConnection {
    
    private static HashSet<RelayConnection> allRelay = new HashSet<>();
    
    protected RelayConnection(final Socket acceptedSock) throws UnsupportedEncodingException, IOException {
        super("relay", acceptedSock);
        allRelay.add(this);
        start();
        
        sendData(Main.getBlockChain());
    }
    
    @Override
    protected void receiveData(final JSONable data) {
        System.out.println("[DEBUG] get value from relay: " + data);
        if(data instanceof Block) {
            final Block block = (Block) data;
            
            // Local blockChain management
            final BlockChain chain = Main.getBlockChain();
            // If block could be add
            if(chain.add(block)) {
                // Broadcast the block to all the relay nodes
                sendToAll(data);
                
            } // TODO ? Inform Relay (and Miner that block have be rejected) ?
            
        }
    }
    
    @Override
    protected void close() {
        super.close();
        allRelay.remove(this);
    }
    
    
    public static void sendToAll(final JSONable data) {
        for(final RelayConnection relay : allRelay) {
            relay.sendData(data);
        }
    }
    
}
