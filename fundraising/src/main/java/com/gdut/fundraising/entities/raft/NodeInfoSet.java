package com.gdut.fundraising.entities.raft;

import java.util.ArrayList;
import java.util.List;

/**
 * 服务器节点
 */
public class NodeInfoSet {
    /**
     * 自身节点
     */
    private NodeInfo self;
    /**
     * 领导者节点
     */
    private NodeInfo leader;
    /**
     * 所有节点
     */
    private List<NodeInfo> all;

    /**
     * 根据id获取某一个节点
     * @param id
     * @return
     */
    public NodeInfo getNode(String id){
        for(NodeInfo nodeInfo:all){
            if(nodeInfo.getId().equals(id)){
                return nodeInfo;
            }
        }
        return null;
    }

    /**
     * 获取出自我节点外的其他所有节点
     * @return
     */
    public List<NodeInfo> getNodeExceptSelf(){
        List<NodeInfo> nodeInfos=new ArrayList<>();
        for(NodeInfo nodeInfo:all){
            if(nodeInfo.getId().equals(self.getId())){
                continue;
            }
            nodeInfos.add(nodeInfo);
        }
        return nodeInfos;
    }

    public NodeInfo getLeader() {
        return leader;
    }

    public void setLeader(NodeInfo leader) {
        this.leader = leader;
    }

    public NodeInfo getSelf() {
        return self;
    }

    public void setSelf(NodeInfo self) {
        this.self = self;
    }

    public List<NodeInfo> getAll() {
        return all;
    }

    public void setAll(List<NodeInfo> all) {
        this.all = all;
    }
}
