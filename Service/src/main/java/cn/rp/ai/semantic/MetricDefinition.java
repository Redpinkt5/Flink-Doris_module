package cn.rp.ai.semantic;

import java.util.List;

public class MetricDefinition {
    private String name;
    private String displayName;
    private String description;
    private String table;
    private String aggregation;
    private String timeDimension;
    private String defaultTimeRange;
    private List<DimensionDefinition> dimensions;
    private List<String> supports;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getTable() { return table; }
    public void setTable(String table) { this.table = table; }
    public String getAggregation() { return aggregation; }
    public void setAggregation(String aggregation) { this.aggregation = aggregation; }
    public String getTimeDimension() { return timeDimension; }
    public void setTimeDimension(String timeDimension) { this.timeDimension = timeDimension; }
    public String getDefaultTimeRange() { return defaultTimeRange; }
    public void setDefaultTimeRange(String defaultTimeRange) { this.defaultTimeRange = defaultTimeRange; }
    public List<DimensionDefinition> getDimensions() { return dimensions; }
    public void setDimensions(List<DimensionDefinition> dimensions) { this.dimensions = dimensions; }
    public List<String> getSupports() { return supports; }
    public void setSupports(List<String> supports) { this.supports = supports; }
}
