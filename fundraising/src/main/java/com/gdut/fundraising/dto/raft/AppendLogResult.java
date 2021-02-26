package com.gdut.fundraising.dto.raft;

/**
 * 心跳包的回复
 */
public class AppendLogResult {
    /**
     * 任期
     */
    private long term;
    private boolean success;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public AppendLogResult() {

    }

    public AppendLogResult(long term, boolean success) {
        this.term = term;
        this.success = success;
    }

    public static AppendLogResult fail(long term) {
        return new AppendLogResult(term, false);
    }

    public static AppendLogResult ok(long term) {
        return new AppendLogResult(term, true);
    }
}
