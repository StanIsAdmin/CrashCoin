package be.ac.ulb.crashcoin.common;

import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stock block
 */
public class BlockChain extends ArrayList<Block> implements JSONable {

    // Used by [Relay Node]
    public BlockChain(final JSONObject jsonObject) {
        
        // TODO
    }

    // Used by [Master node]
    public BlockChain() {
        // TODO
    }
    
    @Override
    public boolean add(final Block block) {
        if(!this.contains(block)) {
            if(checkValidBlock(block, 8)) { // TODO difficulty
                super.add(block);
            } else {
                // TODO not valide !
            }
            return true;
        }
        return false;
    }
    
    // Must may be move to Block
    // Used by [master node]
    protected boolean checkValidBlock(final Block block, final int difficulty) {
        boolean result = block.isHashValid();
        
        // TODO
        // 1. Le hash du block précédent dans le block est bien égal au hash du dernier block présent dans la blockchain
        // 2. les transactions ont comme input des transactions déjà validées 
        //    (i.e. existent dans un bloc précédent – ou le bloc courant(?))
        return result;
    }
    
    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        
        try {
            final JSONArray jArray = new JSONArray();
            for (final Block block : this) {
                 jArray.put(block.toJSON());
            }
            json.put("blockchain", jArray);
        } catch (JSONException jse) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, jse);
        }
        return json;
    }
    
}
