package tools.ceac.ai.mcp.qbid.model.dto;

public class ReferenciaDTO {
    private String filename;       // "REF19_1939998.pdf"
    private String contentType;    // "application/pdf"
    private String encoding;       // "base64"
    private String data;           // contenido codificado en base64

    private ReferenciaDTO() {}

    public String getFilename()    { return filename; }
    public String getContentType() { return contentType; }
    public String getEncoding()    { return encoding; }
    public String getData()        { return data; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ReferenciaDTO o = new ReferenciaDTO();
        public Builder filename(String v)    { o.filename = v; return this; }
        public Builder contentType(String v) { o.contentType = v; return this; }
        public Builder encoding(String v)    { o.encoding = v; return this; }
        public Builder data(String v)        { o.data = v; return this; }
        public ReferenciaDTO build()         { return o; }
    }
}
