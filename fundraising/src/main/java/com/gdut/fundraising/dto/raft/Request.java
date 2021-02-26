package com.gdut.fundraising.dto.raft;

public class Request {
    /**
     * 任期
     */
    private long term;
    /**
     * @see com.gdut.fundraising.constant.raft.MessageType
     */
    private int type;

    public Request(){

    }

    public Request(long term,int type){
        this.term=term;
        this.type=type;
    }


    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
