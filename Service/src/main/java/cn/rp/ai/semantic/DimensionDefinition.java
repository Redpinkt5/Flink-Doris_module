package cn.rp.ai.semantic;

/**
 * 指标下钻维度定义，对应 schema.yaml 中的维度字段。
 */
public class DimensionDefinition {
    private String name;
    private String displayName;
    private String type;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
}
