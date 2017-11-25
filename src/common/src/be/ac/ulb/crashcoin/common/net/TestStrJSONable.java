package be.ac.ulb.crashcoin.common.net;

import be.ac.ulb.crashcoin.common.JSONable;
import org.json.JSONObject;

/**
 * 
 */
public class TestStrJSONable implements JSONable {
    
    private String _value;
    
    public TestStrJSONable() {
        this("Test");
    }
    
    public TestStrJSONable(final JSONObject jsonObject) {
        this(jsonObject.getString("value"));
    }
    
    public TestStrJSONable(final String value) {
        _value = value;
    }
    
    private String getJsonType() {
        return "TestStr";
    }
    
    @Override
    public JSONObject toJSON() {
        final JSONObject json = new JSONObject();
        json.put("type", getJsonType());
        json.put("value", _value);
        return json;
    }

}
