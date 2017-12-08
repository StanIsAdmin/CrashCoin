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

        // When a relay connects, it always wants the blockchain.
        // "Hi relay! How are you doing? Let me pour you a blockchain, as usual."
        sendData(bcManager.getBlockChain());
    }

    @Override
    protected void receiveData(final JSONable data) {
        Logger.getLogger(getClass().getName()).log(Level.FINEST, "get value from relay: {0}", data);
        if (data instanceof Block) {
            final Block block = (Block) data;

            // Local blockChain management
            final BlockChain chain = bcManager.getBlockChain();
            // If block could be add
            if (chain.add(block)) {
                //TODO make blockChain "observable" or go through manager to add blocks ?
                bcManager.saveBlockChain();

                // Broadcast the block to all the relay nodes
                sendToAll(data);
            } // TODO ? Inform Relay (and Miner that the block has been rejected) ?
        }
        else {
            Logger.getLogger(getClass().getName()).log(Level.FINE, "Unexpected {0} received from relay.", data.getClass().getName());
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
