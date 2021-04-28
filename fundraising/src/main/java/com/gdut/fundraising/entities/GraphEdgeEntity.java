package com.gdut.fundraising.entities;

/**
 * 拓扑图的边
 */
public class GraphEdgeEntity {
    String source;
    String target;
    String label;
    /**
     * 线的类型
     */
    String type;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
