package tools.ceac.ai.modules.qbid.application.port.out;

import java.util.Map;

/**
 * Outbound transport contract for talking to qBid over HTTP.
 */
public interface QbidTransport {

    String login(String username, String password) throws Exception;
    String get(String url, String jsessionid) throws Exception;
    byte[] getBytes(String url, String jsessionid) throws Exception;
    String post(String url, Map<String, String> params, String jsessionid) throws Exception;
    String postRaw(String url, String formBody, String jsessionid) throws Exception;
}

