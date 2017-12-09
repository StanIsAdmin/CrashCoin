package be.ac.ulb.crashcoin.miner;

/**
 *
 * Exception to be thrown when mining step is aborted due to new block entering
 * the blockchain
 */
public class AbortMiningException extends Exception {
    public AbortMiningException() {
        super();
    }
    
    public AbortMiningException(final String message) {
        super(message);
    }
}
