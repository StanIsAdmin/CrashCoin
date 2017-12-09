package be.ac.ulb.crashcoin.master.net;

import be.ac.ulb.crashcoin.common.Parameters;
import be.ac.ulb.crashcoin.common.net.AbstractListener;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Listen all connexion from Relay<br>
 * It's a singleton and a thread
 */
public class RelayListener extends AbstractListener {

    private static RelayListener instance = null;

    private RelayListener() throws IOException {
        super("RelayListener", new ServerSocket(Parameters.MASTER_PORT_LISTENER));

        start();
    }

    @Override
    protected void createNewConnection(Socket sock) throws IOException {
        new RelayConnection(sock);
    }

    public static synchronized RelayListener getListener() throws IOException {
        if (instance == null) { // TODO it's seems to me that it is not thread safe. Even if we do not need it here, may be we shoud write it in comment? #Alexis
            instance = new RelayListener();
        }
        return instance;
    }

}
