package tools.ceac.ai.mcp.campus.infrastructure.campus;

import org.springframework.context.ApplicationEvent;

public class HttpResponseEvent extends ApplicationEvent {
    private final String method;
    private final String url;
    private final String requestBody;
    private final String body;

    public HttpResponseEvent(Object source, String method, String url, String requestBody, String body) {
        super(source);
        this.method = method;
        this.url = url;
        this.requestBody = requestBody;
        this.body = body;
    }

    public String method()      { return method; }
    public String url()         { return url; }
    public String requestBody() { return requestBody; }
    public String body()        { return body; }
}