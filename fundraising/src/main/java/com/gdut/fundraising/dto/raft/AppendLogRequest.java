package com.gdut.fundraising.dto.raft;

import com.gdut.fundraising.constant.raft.MessageType;
import com.gdut.fundraising.entities.raft.LogEntry;

public class AppendLogRequest extends Request {
    /**
     * 领导人的 Id，以便于跟随者重定向请求
     */
    private String leaderId;

    /**
     * 新的日志条目紧随之前的索引值
     */
    private long prevLogIndex;

    /**
     * prevLogIndex 条目的任期号
     */
    private long preLogTerm;

    /**
     * 准备存储的日志条目（表示心跳时为空；一次性发送多个是为了提高效率）
     */
    private LogEntry[] entries;

    /**
     * 领导人已经提交的日志的索引值
     */
    private long leaderCommit;

    public AppendLogRequest() {

    }

    /**
     * 追加日志的请求
     *
     * @param leaderId
     * @param prevLogIndex
     * @param preLogTerm
     * @param entries
     * @param leaderCommit
     * @param term
     */
    public AppendLogRequest(String leaderId, long prevLogIndex, long preLogTerm, LogEntry[] entries, long leaderCommit, long term) {
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.preLogTerm = preLogTerm;
        this.entries = entries;
        this.leaderCommit = leaderCommit;
        this.setTerm(term);
        this.setType(MessageType.APPEND_LOG.getValue());
    }

    /**
     * 心跳包，日志为空
     *
     * @param leaderId
     * @param prevLogIndex
     * @param preLogTerm
     * @param term
     */
    public AppendLogRequest(String leaderId, long prevLogIndex, long preLogTerm, long term) {
        this.leaderId = leaderId;
        this.prevLogIndex = prevLogIndex;
        this.preLogTerm = preLogTerm;
        this.entries = null;
        this.leaderCommit = 0L;
        this.setTerm(term);
        this.setType(MessageType.APPEND_LOG.getValue());
    }

    @Override
    public String toString() {
        return "AppendLogRequest{" +
                "leaderId='" + leaderId + '\'' +
                ", prevLogIndex=" + prevLogIndex + '\'' +
                "preLogTerm='" + preLogTerm + '\'' +
                "entries='" + entries + '\'' +
                "leaderCommit='" + leaderCommit +

                '}';
    }

    public String getLeaderId() {
        return leaderId;
    }

    public void setLeaderId(String leaderId) {
        this.leaderId = leaderId;
    }

    public long getPrevLogIndex() {
        return prevLogIndex;
    }

    public void setPrevLogIndex(long prevLogIndex) {
        this.prevLogIndex = prevLogIndex;
    }

    public long getPreLogTerm() {
        return preLogTerm;
    }

    public void setPreLogTerm(long preLogTerm) {
        this.preLogTerm = preLogTerm;
    }

    public LogEntry[] getEntries() {
        return entries;
    }

    public void setEntries(LogEntry[] entries) {
        this.entries = entries;
    }

    public long getLeaderCommit() {
        return leaderCommit;
    }

    public void setLeaderCommit(long leaderCommit) {
        this.leaderCommit = leaderCommit;
    }
}
