/*
 * Abstract class for common classes that can be (de)serialized into/from JSONObject.
 */
package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;


public abstract class JSONable {
    /* Creates a class instance from a JSONObject representation */
    public JSONable(JSONObject json) {};
    
    /* Allows other constructors for subclasses */
    public JSONable() {};
    
    /** Get a JSON representation of the class instance
     * @return  the JSONObject representation
     **/
    public abstract JSONObject toJSON();
}
