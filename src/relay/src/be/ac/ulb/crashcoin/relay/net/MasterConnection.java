package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
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
        // Receive BlockChain (normaly, only at the first connection
        if (jsonData instanceof BlockChain) {
            final BlockChain blockChain = (BlockChain) jsonData;
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Get BlockChain of size {0} from master", 
                    new Object[]{blockChain.size()});
            Main.setBlockChain(blockChain);

        } else if (jsonData instanceof Block) { // Receive a mined block
            // local mined block management
            final Block block = (Block) jsonData;
            Main.getBlockChain().add(block);
            
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Get block from master and save to blockchain: {0}", 
                    new Object[]{block.toString()});

            // Broadcast to the miners the validate/mined block so that they can
            // either remove the mined transaction from their pool or stop
            // the block mining if the transaction is in the block.
            MinerConnection.sendToAll(jsonData);
            
        } else if(jsonData instanceof Transaction) { // Get new transaction from master ("pool" transaction)
            final Transaction transaction = (Transaction) jsonData;
            
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Get new transaction from master and send to miner "
                    + "({0}): {1}", new Object[]{_ip, transaction.toString()});
            
            // Broadcast to the miners directly connected to the relay.
            MinerConnection.sendToAll(jsonData);
            
        } else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Get unknowed value from master ({0}): {1}", 
                new Object[]{_ip, jsonData});
        }
    }

    @Override
    protected boolean canCreateNewInstance() {
        boolean isConnected;
        try {
            instance = new MasterConnection();
            isConnected = true;
        } catch (IOException ex) {
            isConnected = false;
        }
        return isConnected;
    }

    public static MasterConnection getMasterConnection() throws IOException {
        if (instance == null) {
            Logger.getLogger(MasterConnection.class.getName()).log(Level.INFO, "Test new connection to master");
            instance = new MasterConnection();
        }
        return instance;
    }

}
