package be.ac.ulb.crashcoin.common.net;

import java.util.Base64;

/**
 *
 * @author alexis
 */
public class JsonUtils {

    public static String encodeBytes(final byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] decodeBytes(final String string) {
        return Base64.getDecoder().decode(string);
    }
}
