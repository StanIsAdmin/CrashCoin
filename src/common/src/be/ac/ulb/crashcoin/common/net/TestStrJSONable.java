package be.ac.ulb.crashcoin.common.net;

import be.ac.ulb.crashcoin.common.JSONable;
import org.json.JSONObject;

/**
 * 
 */
public class TestStrJSONable implements JSONable {

    @Override
    public JSONObject toJSON() {
        final JSONObject json = new JSONObject();
        json.put("value", "Test");
        return json;
    }

}
