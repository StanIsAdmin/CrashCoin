package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Message;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import be.ac.ulb.crashcoin.master.BlockChainManager;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;

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
                bcManager.saveBlockChain();

                // Broadcast the block to all the relay nodes
                sendToAll(data);
            } else {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "Block invalid:\n{0}", 
                    new Object[]{block.toString()});
                sendNotValidTransactions(block);
            }
            // TODO ? Inform Relay (and Miner that the block has been rejected) ?
            
        } else if(data instanceof Transaction) {
            // Broadcast "pool" transaction to all nodes
            sendToAll(data);
            
        } else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING,
                    "Unexpected {0} received from relay {1} ({2})", 
                    new Object[]{data.getClass().getName(), _ip, data});
        }
    }

    @Override
    protected void close() {
        super.close();
        allRelay.remove(this);
    }
    
    /**
     * Send the transactions that are not valid to all the relays.
     * 
     * @param block 
     */
    private void sendNotValidTransactions(final Block block) {
        final HashSet<Transaction> allBadTransactions = bcManager.getBlockChain().getBadTransactions(block);
        if(allBadTransactions.isEmpty())
            return;
        final JSONArray badTransactions = new JSONArray();
        for(final Transaction badTransaction : allBadTransactions) {
            badTransactions.put(badTransaction.toJSON());
        }
        final JSONObject option = new JSONObject();
        option.put("transactions", badTransactions);
        final Message transactionsNotValid = new Message(Message.TRANSACTIONS_NOT_VALID, option);
        sendToAll(transactionsNotValid);
    }

    public static void sendToAll(final JSONable data) {
        for (final RelayConnection relay : allRelay) {
            relay.sendData(data);
        }
    }

}
