package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;

/**
 * Class representing a network request.
 */
public class Message implements JSONable {

    public static final String GET_TRANSACTIONS_FROM_WALLET = "GET_TRANSACTIONS_FROM_WALLET";
    public static final String GET_LAST_BLOCK = "GET_LAST_BLOCK";
    public static final String TRANSACTIONS_NOT_VALID = "TRANSACTIONS_NOT_VALID";

    /**
     * String representing the request
     */
    private final String request;
    /**
     * possible arguments of the request
     */
    private final JSONObject option;

    public Message(final String request) {
        this.request = request;
        this.option = null;
    }
    
    public Message(final String request, final JSONable option) {
        this(request, option.toJSON());
    }

    public Message(final String request, final JSONObject option) {
        this.request = request;
        this.option = option;
    }

    public Message(final JSONObject jobject) {
        this(jobject.getString("request"), jobject.has("option") ? jobject.getJSONObject("option") : null);
    }

    @Override
    public JSONObject toJSON() {
        final JSONObject json = JSONable.super.toJSON();
        json.put("request", request);
        json.put("option", option);
        return json;
    }

    public String getRequest() {
        return this.request;
    }

    public JSONObject getOption() {
        return this.option;
    }
    
    @Override
    public String toString() {
        String output = "Message: ";
        output += this.request+"\n";
        output += this.option.toString();
        return output;
    }
}
