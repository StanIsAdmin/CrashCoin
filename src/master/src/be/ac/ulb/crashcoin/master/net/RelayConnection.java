package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import be.ac.ulb.crashcoin.master.BlockChainManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Connection to a Relay
 */
public class RelayConnection extends AbstractConnection {

    private static final HashSet<RelayConnection> allRelay = new HashSet<>();

    // Initializes the BlockChain manager (the sooner the better)
    private static final BlockChainManager bcManager = BlockChainManager.getInstance();

    protected RelayConnection(final Socket acceptedSock) throws UnsupportedEncodingException, IOException {
        super("relay", acceptedSock);
        allRelay.add(this);
        start();

        final BlockChain blockChain = bcManager.getBlockChain();
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Send BlockChain of size {0} to relay ({1})", 
                new Object[]{blockChain.size(), _ip});
        sendData(blockChain);
    }

    @Override
    protected void receiveData(final JSONable data) {
        
        if (data instanceof Block) {
            final Block block = (Block) data;

            // Local blockChain management
            final BlockChain chain = bcManager.getBlockChain();
            
            // If block could be add
            if (chain.add(block)) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Save Block to BlockChain:\n{0}", 
                    new Object[]{block.toString()});
                //TODO make blockChain "observable" or go through manager to add blocks ?
                bcManager.saveBlockChain();
                
                // Broadcast the block to all the relay nodes
                sendToAll(data);
                
            } else {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Block invalid:\n{0}", 
                    new Object[]{block.toString()});
            } 
            // TODO ? Inform Relay (and Miner that the block has been rejected) ?
        } else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Get unknowed value from relay ({0}): {1}", 
                new Object[]{_ip, data});
        }
    }

    @Override
    protected void close() {
        super.close();
        allRelay.remove(this);
    }

    public static void sendToAll(final JSONable data) {
        for (final RelayConnection relay : allRelay) {
            relay.sendData(data);
        }
    }

}
