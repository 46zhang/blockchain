package com.gdut.fundraising.constant;

public enum GraphLineTypeEnum {
    LINE("line"),LOOP("loop"),QUADRATIC("quadratic");
    String type;
    GraphLineTypeEnum(String type){
        this.type=type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
