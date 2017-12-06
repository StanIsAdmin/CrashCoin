package be.ac.ulb.crashcoin.common.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Detobel
 */
public abstract class AbstractListener extends Thread {

    protected ServerSocket _sock;

    protected AbstractListener(final String name, final ServerSocket sock) {
        super(name);
        _sock = sock;
    }

    @Override
    public void run() {
        try {
            while (true) {
                final Socket newSock = _sock.accept();
                createNewConnection(newSock);
                Logger.getLogger(getClass().getName()).log(Level.INFO, "New connection (from {0})", 
                        newSock.getInetAddress().toString());
            }
        } catch (IOException e) {
            // Exception in relay
        }

        close();
    }

    protected void close() {
        try {
            Logger.getLogger(getClass().getName()).log(Level.INFO, "Close listener connection !");
            _sock.close();
        } catch (IOException e) {
        }
    }

    protected abstract void createNewConnection(Socket sock) throws IOException;

}
