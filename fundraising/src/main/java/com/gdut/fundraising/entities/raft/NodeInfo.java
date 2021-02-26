package com.gdut.fundraising.entities.raft;

/*
 * 每个服务器节点信息
 */
public class NodeInfo {
    /**
     * 服务器唯一标识，同一台电脑用端口号来标识
     * 如果不同ip地址则需要ip+port
     */
    private String id;
    /**
     * 该节点的状态
     * leader follower candidate
     */
    private int status;
    /**
     * 是否还存活
     */
    private boolean alive;

    /**
     * ip地址
     */
    private String ip;

    /**
     * 端口号
     */
    private String port;

    public NodeInfo() {

    }

    public NodeInfo(String ip, String port) {
        this.id = ip + ":" + port;
        this.ip = ip;
        this.port = port;
    }

    @Override
    public String toString() {
        return "NodeInfo{" +
                "id='" + id + '\'' +
                ", ip=" + ip +
                ", port=" + port +
                ", alive=" + alive +
                ", status=" + status +
                '}';
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setAlive(boolean alive) {
        this.alive = alive;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }
}
