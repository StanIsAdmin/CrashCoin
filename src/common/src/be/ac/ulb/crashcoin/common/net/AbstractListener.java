package be.ac.ulb.crashcoin.common.net;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Manages a ServerSocket (i.e.: it is a Thread waiting for connections to accept)
 *
 * Subclasses should overwrite the createNewConnection method which is called
 * each time a new connection is accepted.
 *
 * @author Detobel
 */
public abstract class AbstractListener extends Thread {

    protected ServerSocket _sock;

    /**
     * Create a new AbstractListener, that is a thread and a server socket.
     *
     * @param name the thread name (For identification purposes. More than one thread may have the same name.)
     * @param sock the server socket
     */
    protected AbstractListener(final String name, final ServerSocket sock) {
        super(name);
        _sock = sock;
    }

    /**
     * Wait for connections on the ServerSocket, accepts, create a new (client)
     * socket for each and a call the abstract method createNewConnection with
     * each new socket.
     */
    @Override
    public void run() {
        try {
            while (true) {
                Logger.getLogger(getClass().getName()).log(Level.INFO, "New connection");
                createNewConnection(_sock.accept());
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

    /**
     * Called each time a new connection is accepted.
     * @param sock The (client) Socket created for this connection.
     * @throws IOException
     */
    protected abstract void createNewConnection(Socket sock) throws IOException;

}
