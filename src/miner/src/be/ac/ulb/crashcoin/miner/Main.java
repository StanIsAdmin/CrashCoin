package be.ac.ulb.crashcoin.miner;

/**
 * Entry point of the miner program. 
 */
public class Main {
    
    public static void main(final String[] args) {
        // create a miner... And start mining... Whut else?
        Miner miner = new Miner();
        miner.startMining();
    }
    
}
