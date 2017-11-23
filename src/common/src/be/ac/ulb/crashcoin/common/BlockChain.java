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

    public BlockChain(final JSONObject jsonObject) {
        // TODO
    }

    public BlockChain() {
        // TODO
    }
    
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
        final JSONObject jObject = new JSONObject();
        try {
            final JSONArray jArray = new JSONArray();
            for (final Block block : this) {
                 jArray.put(block.toJSON());
            }
            jObject.put("blockchain", jArray);
        } catch (JSONException jse) {
            Logger.getLogger(getClass().getName()).log(Level.SEVERE, null, jse);
        }
        return jObject;
    }
    
}
