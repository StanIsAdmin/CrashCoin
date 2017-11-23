package be.ac.ulb.crashcoin.common;

import com.sun.istack.internal.logging.Logger;
import java.util.ArrayList;
import java.util.logging.Level;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Stock block
 */
public class BlockChain extends ArrayList<Block> implements JSONable {

    public BlockChain(JSONObject jsonObject) {
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
        JSONObject jObject = new JSONObject();
        try {
            JSONArray jArray = new JSONArray();
            for (Block block : this) {
                 JSONObject blockJSON = block.toJSON();
                 jArray.put(blockJSON);
            }
            jObject.put("blockchain", jArray);
        } catch (JSONException jse) {
            Logger.getLogger(Block.class).log(Level.SEVERE, null, jse);
        }
        return jObject;
    }
    
}
