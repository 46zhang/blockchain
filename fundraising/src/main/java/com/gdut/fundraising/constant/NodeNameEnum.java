package com.gdut.fundraising.constant;

public enum NodeNameEnum {
    /**
     * 募捐平台
     */
    PLATFORM("8090", "公益募捐平台"),
    /**
     * X医院
     */
    HOSPITAL("8091", "X医院"),
    /**
     * Y慈善机构
     */
    INSTITUTION("8092", "Y慈善机构");

    private String port;
    private String name;

    NodeNameEnum(String address, String name) {
        this.name = name;
        this.port = address;
    }

    public static NodeNameEnum getNodeNameEnumByPort(String port){
        for(NodeNameEnum nodeNameEnum:values()){
            if(nodeNameEnum.port.equals(port)){
                return nodeNameEnum;
            }
        }
        return null;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
