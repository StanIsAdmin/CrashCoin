package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;

/**
 * Block that compose BlockChain
 */
public class Block implements JSONable {
    
    public Block() {
        super();
        //TODO
    }
    
    /** Create Address instance from a JSON representation **/
    public Block(JSONObject json) {
        this(); //TODO pass json values as parameters to Block() ctr
    }
    
    /** Get a JSON representation of the Block instance **/
    public JSONObject toJSON() {
        //TODO
        return new JSONObject();
    }

    /** Used for test purposes **/
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Block other = (Block) obj;
        return true; //TODO compare attributes
    }
    
    
}
