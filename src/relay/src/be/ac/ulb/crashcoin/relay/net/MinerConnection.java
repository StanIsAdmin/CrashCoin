package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import be.ac.ulb.crashcoin.relay.Main;
import java.io.IOException;
import java.net.Socket;
import java.util.HashSet;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
class MinerConnection extends AbstractConnection {

    private static final HashSet<MinerConnection> allMiners = new HashSet<>();

    public MinerConnection(final Socket sock) throws IOException {
        super("MinerConnection", sock);
        allMiners.add(this);
        
        // send last block of the blockchain to freshly connected miner
        final Block lastBlock = Main.getBlockChain().getLastBlock();
        Logger.getLogger(getClass().getName()).log(Level.INFO, "Send last block to miner ({0}):\n{1}",
                new Object[]{_ip, lastBlock.toString()});
        sendData(lastBlock);

        start();
    }

    @Override
    protected void receiveData(final JSONable jsonData) {
        if (jsonData instanceof Block) {
            final Block block = (Block) jsonData;
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Get block fomr miner ({0}):\n{1}",
                    new Object[]{_ip, block.toString()});
            
            // Relay the data to the master node
            try {
                MasterConnection.getMasterConnection().sendData(block);
            } catch (IOException ex) {
                Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Could not send block to Master {0}",
                        ex.getMessage());
            }

        } else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Get unknowed value from miner ({0}): {1}",
                    new Object[]{_ip, jsonData});
        }

    }

    @Override
    protected void close() {
        super.close();
        allMiners.remove(this);
    }

    public static void sendToAll(final JSONable data) {
        for (final MinerConnection relay : allMiners) {
            relay.sendData(data);
        }
    }

}
