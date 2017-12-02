package be.ac.ulb.crashcoin.relay.net;

import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.Block;
import be.ac.ulb.crashcoin.common.BlockChain;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Message;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.AbstractConnection;
import be.ac.ulb.crashcoin.relay.Main;
import java.io.IOException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONObject;

/**
 *
 */
public class WalletConnection extends AbstractConnection {

    public WalletConnection(final Socket sock) throws IOException {
        super("WalletConnection", sock);
        start();
    }

    /**
     * Simply broadcast the transaction to every connected miner
     *
     * @param jsonData
     */
    @Override
    protected void receiveData(final JSONable jsonData) {

        if (jsonData instanceof Transaction) {
            // TODO new transaction management
            final Transaction transaction = (Transaction) jsonData;
            // Broadcast to the miners directly connected to the relay.
            MinerConnection.sendToAll(jsonData);

            // Relay the transaction to the master.
            try {
                MasterConnection.getMasterConnection().sendData(transaction);
            } catch (IOException ex) {
                Logger.getLogger(MinerConnection.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else if (jsonData instanceof Message) {
            final Message message = (Message) jsonData;

            final String request = message.getRequest();
            final JSONObject option = message.getOption();

            switch (request) {
                case Message.GET_TRANSACTIONS_FROM_WALLET:
                    sendTransactions(option);
                    break;

                default:
                    Logger.getLogger(getClass().getName(), "Unknown request: " + request);
                    break;
            }

        }
    }

    /**
     * Send all the transactions related to a given user
     *
     * @param option A JSON object containing the public key of the asking
     * person
     */
    private void sendTransactions(final JSONObject option) {
        if (option == null) {
            Logger.getLogger(getClass().getName(), "Request: '"
                    + Message.GET_TRANSACTIONS_FROM_WALLET + "' but no option provided");
            return;
        }
        final Address walletAddress = new Address(option);
        final BlockChain currentBlockChain = Main.getBlockChain();
        
        int index = 0;
        for (final Block block : currentBlockChain) {
            // Do not test with empty block
            if(!block.isEmpty()) {
                for (final Transaction transaction : block) {
                    // Not send transaction created by the miner
                    if (transaction.getDestAddress().equals(walletAddress)
                            || transaction.getSrcAddress().equals(walletAddress)) {
                        sendData(transaction);
                    }
                }
            } else {
                System.err.println("Empty block (index: " + index + ")");
            }
            ++index;
        }
    }
}
