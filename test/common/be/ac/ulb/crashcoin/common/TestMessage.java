package be.ac.ulb.crashcoin.common;

import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Test;

/**
 *
 */
public class TestMessage {

    @Test
    public void testSimpleMessageJSONConversion() {
        final Message simpleMessage = new Message("Test");
        final Message copyMessage = new Message(simpleMessage.toJSON());

        Assert.assertEquals(copyMessage.getRequest(), simpleMessage.getRequest());
    }

    @Test
    public void testOptionMessageJSONConversion() {
        final JSONObject jsonObject = new JSONObject();
        final Message optionMessage = new Message("Test", jsonObject);
        final Message copyMessage = new Message(optionMessage.toJSON());

        Assert.assertEquals(copyMessage.getOption(), optionMessage.getOption());
    }

}
