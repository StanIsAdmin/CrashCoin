package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;

/**
 * Abstract class for common classes that can be (de)serialized into/from
 * JSONObject.<br>
 * JSONable object <b>must</b> have an attribut "type".
 */
public interface JSONable {

    /**
     * Get a JSON representation of the class instance
     *
     * @return the JSONObject representation
     *
     */
    public default JSONObject toJSON() {
        final JSONObject json = new JSONObject();
        json.put("type", this.getClass().getName());
        return json;
    }

}
