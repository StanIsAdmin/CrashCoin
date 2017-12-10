package be.ac.ulb.crashcoin.client.net;

import be.ac.ulb.crashcoin.client.ClientApplication;
import be.ac.ulb.crashcoin.client.WalletClient;
import be.ac.ulb.crashcoin.common.Address;
import be.ac.ulb.crashcoin.common.JSONable;
import be.ac.ulb.crashcoin.common.Message;
import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.Transaction;
import be.ac.ulb.crashcoin.common.net.AbstractReconnectConnection;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class RelayConnection extends AbstractReconnectConnection {

    private static RelayConnection instance = null;

    private RelayConnection() throws UnsupportedEncodingException, IOException {
        super("RelayConnection", new Socket(Parameters.RELAY_IP, Parameters.RELAY_PORT_WALLET_LISTENER));
        start();
    }

    @Override
    protected boolean canCreateNewInstance() {
        boolean isConnected;
        try {
            instance = new RelayConnection();
            isConnected = true;
        } catch (IOException ex) {
            isConnected = false;
        }
        return isConnected;
    }

    @Override
    protected void receiveData(final JSONable data) {
        final WalletClient wallet = ClientApplication.getInstance().getWallet();
        if(data instanceof Transaction) {
            final Transaction transaction = (Transaction) data;
            if(wallet != null) {
                if(transactionConcernCurrentWallet(transaction)) {
                    wallet.addAcceptedTransaction(transaction);
                } else {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Recieve transaction with no link "
                            + "with current wallet:\n{0}", transaction.toString());
                }
            } else {
                Logger.getLogger(getClass().getName()).log(Level.WARNING, "Wallet is not defined but recieved "
                        + "transaction:\n{0}", transaction.toString());
            }
            
        } else if(data instanceof Message) {
            final Message message = (Message) data;
            if(message.getRequest().equals(Message.TRANSACTIONS_NOT_VALID)) {
                // @see #transactionConcernCurrentWallet
                final Transaction transaction = new Transaction(message.getOption());
                if(wallet != null) {
                    wallet.removeNotValidTransaction(transaction);
                } else {
                    Logger.getLogger(getClass().getName()).log(Level.WARNING, "Wallet is not defined but recieved "
                            + "message:\n{0}", message.toString());
                }
            }
        } else {
            Logger.getLogger(getClass().getName()).log(Level.WARNING, "Receive unknowed object: {0}", data.toString());
        }
    }
    
    private boolean transactionConcernCurrentWallet(final Transaction transaction) {
        final WalletClient wallet = ClientApplication.getInstance().getWallet();
        if(wallet != null) {
            final Address address = wallet.getAddress();
            return transaction.getDestAddress().equals(address) || 
                    (transaction.getSrcAddress() != null && transaction.getSrcAddress().equals(address));
        }
        return false;
    }

    public static RelayConnection getInstance() throws IOException {
        if (instance == null) {
            instance = new RelayConnection();
        }
        return instance;
    }

}
