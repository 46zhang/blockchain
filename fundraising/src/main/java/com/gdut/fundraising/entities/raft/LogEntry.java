package com.gdut.fundraising.entities.raft;

public class LogEntry {
    long term;

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }
}
