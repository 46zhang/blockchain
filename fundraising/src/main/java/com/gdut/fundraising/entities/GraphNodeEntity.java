package com.gdut.fundraising.entities;

/**
 * 拓扑图节点
 */
public class GraphNodeEntity {
    String id;

    String label;



    @Override
    public int hashCode() {
        final int prime = 31;
        int result = prime * (id== null ? 1 : id.hashCode());
        return result + label.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GraphNodeEntity){
            GraphNodeEntity graphNodeEntity= (GraphNodeEntity) obj;
            return id.equals(graphNodeEntity.getId()) && label.equals(graphNodeEntity.label);
        }
        return super.equals(obj);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
