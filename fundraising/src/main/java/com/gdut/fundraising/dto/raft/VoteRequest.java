package com.gdut.fundraising.dto.raft;

import com.gdut.fundraising.constant.raft.MessageType;

/**
 * 投票请求信息
 */
public class VoteRequest extends Request {
    /**
     * 候选人ID
     * ip:port
     */
    private String candidateId;
    /**
     * 候选人的最后日志项索引值
     */
    private long lastLogIndex;
    /**
     * 候选人的最后日志项任期号
     */
    private long lastLogTerm;

    public VoteRequest(){

    }

    /**
     * 初始化构造器
     * @param candidateId
     * @param lastLogIndex
     * @param lastLogTerm
     * @param term
     */
    public VoteRequest(String candidateId, long lastLogIndex, long lastLogTerm, long term){
        this.candidateId=candidateId;
        this.lastLogIndex=lastLogIndex;
        this.lastLogTerm=lastLogTerm;
        this.setTerm(term);
        this.setType(MessageType.VOTE.getValue());
    }

    public String getCandidateId() {
        return candidateId;
    }

    public void setCandidateId(String candidateId) {
        this.candidateId = candidateId;
    }

    public long getLastLogIndex() {
        return lastLogIndex;
    }

    public void setLastLogIndex(long lastLogIndex) {
        this.lastLogIndex = lastLogIndex;
    }

    public long getLastLogTerm() {
        return lastLogTerm;
    }

    public void setLastLogTerm(long lastLogTerm) {
        this.lastLogTerm = lastLogTerm;
    }
}
