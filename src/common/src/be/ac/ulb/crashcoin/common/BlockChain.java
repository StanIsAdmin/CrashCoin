package be.ac.ulb.crashcoin.common;

import java.util.HashSet;
import org.json.JSONObject;

/**
 * Stock block
 */
public class BlockChain extends HashSet<Block> implements JSONable {
    
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

    @Override
    public JSONObject toJSON() {
        // TODO
        return new JSONObject();
    }
    
}
