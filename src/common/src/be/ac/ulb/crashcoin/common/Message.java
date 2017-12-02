package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;

/**
 * Class representing a network request.
 */
public class Message implements JSONable {

    public static final String GET_TRANSACTIONS_FROM_WALLET = "get transactions from wallet";

    /**
     * String representing the request
     */
    private final String request;
    /**
     * possible arguments of the request
     */
    private final JSONObject option;

    public Message(final String request) {
        this(request, null);
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
}
