/*
 * Abstract class for common classes that can be (de)serialized into/from JSONObject.
 */
package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;


public interface JSONable {
    
    /** Get a JSON representation of the class instance
     * @return  the JSONObject representation
     **/
    public abstract JSONObject toJSON();
}
