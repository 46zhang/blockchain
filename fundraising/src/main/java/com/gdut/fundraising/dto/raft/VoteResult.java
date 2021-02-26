package com.gdut.fundraising.dto.raft;

/**
 * 投票结果
 */
public class VoteResult {
    /**
     * 整个群体的任期号
     */
    private long term;
    /**
     * 是否赢得投票
     */
    private boolean voteGranted;

    public VoteResult() {

    }

    public VoteResult(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }

    public VoteResult(long term, boolean voteGranted) {
        this.term = term;
        this.voteGranted = voteGranted;
    }

    @Override
    public String toString() {
        return "VoteResult{" +
                "voteGranted='" + voteGranted + '\'' +
                ", term=" + term +
                '}';
    }

    /**
     * @return 失败
     */
    public static VoteResult fail(long term) {
        return new VoteResult(term, false);
    }

    /**
     * @return 成功
     */
    public static VoteResult ok(long term) {
        return new VoteResult(term, true);
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }

    public boolean isVoteGranted() {
        return voteGranted;
    }

    public void setVoteGranted(boolean voteGranted) {
        this.voteGranted = voteGranted;
    }
}
