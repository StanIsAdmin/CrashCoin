package be.ac.ulb.crashcoin.common.net;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

/**
 *
 */
public abstract class AbstractReconnectConnection extends AbstractConnection {

    public AbstractReconnectConnection(final String name, final Socket acceptedSock)
            throws UnsupportedEncodingException, IOException {
        super(name, acceptedSock);
    }

    protected void reconnect() {
        boolean isConnected = false;
        while (!isConnected) {
            if (canCreateNewInstance()) {
                isConnected = true;
            }
        }
    }

    @Override
    public void run() {
        super.run();
        System.out.println("Lost connexion");
        reconnect();
    }

    /**
     * Use when connection have been lost. Try to create a new instance of this
     * class to reconnect.
     *
     * @return True if new class have been instanced. False otherwise
     */
    protected abstract boolean canCreateNewInstance();

}
