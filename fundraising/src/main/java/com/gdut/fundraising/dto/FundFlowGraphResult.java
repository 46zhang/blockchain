package com.gdut.fundraising.dto;

import com.gdut.fundraising.entities.GraphEdgeEntity;
import com.gdut.fundraising.entities.GraphNodeEntity;

import java.util.List;

/**
 * 资金流拓扑图信息
 */
public class FundFlowGraphResult {
    /**
     * 拓扑图节点集
     */
    List<GraphNodeEntity> nodes;

    /**
     * 拓扑图边集合
     */
    List<GraphEdgeEntity> edges;

    public List<GraphNodeEntity> getNodes() {
        return nodes;
    }

    public void setNodes(List<GraphNodeEntity> nodes) {
        this.nodes = nodes;
    }

    public List<GraphEdgeEntity> getEdges() {
        return edges;
    }

    public void setEdges(List<GraphEdgeEntity> edges) {
        this.edges = edges;
    }
}
