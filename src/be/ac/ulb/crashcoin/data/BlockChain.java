package be.ac.ulb.crashcoin.data;

import java.util.HashSet;

/**
 * Stock block
 */
public class BlockChain extends HashSet<Block> {
    
    @Override
    public boolean add(final Block block) {
        if(!this.contains(block)) {
            if(checkValideBlock(block)) {
                super.add(block);
            } else {
                // TODO not valide !
            }
            return true;
        }
        return false;
    }
    
    // Must may be move to Block
    protected boolean checkValideBlock(final Block block) {
        // TODO 
        return true;
    }
    
}
