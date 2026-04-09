package tools.ceac.ai.mcp.qbid.model.dto;

public class ItemPlanDTO {
    private String id;
    private String parentId;
    private int nivel;
    private String texto;
    private boolean seleccionada;
    private boolean editable;

    private ItemPlanDTO() {}

    public String getId()             { return id; }
    public String getParentId()       { return parentId; }
    public int getNivel()             { return nivel; }
    public String getTexto()          { return texto; }
    public boolean isSeleccionada()   { return seleccionada; }
    public boolean isEditable()       { return editable; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private final ItemPlanDTO o = new ItemPlanDTO();
        public Builder id(String v)            { o.id = v; return this; }
        public Builder parentId(String v)      { o.parentId = v; return this; }
        public Builder nivel(int v)            { o.nivel = v; return this; }
        public Builder texto(String v)         { o.texto = v; return this; }
        public Builder seleccionada(boolean v) { o.seleccionada = v; return this; }
        public Builder editable(boolean v)     { o.editable = v; return this; }
        public ItemPlanDTO build()             { return o; }
    }
}
