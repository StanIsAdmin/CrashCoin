package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;

/**
 * Static class to convert JSONObject to real Java Object
 */
public class ManageJSON {
    
    public static JSONable getObjectFromJsonObject(final JSONObject jsonData) {
        JSONable result = null;
        switch(jsonData.getString("value")) {
            // TODO
        }
        
        return result;
    }
    
}
