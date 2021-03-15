package com.gdut.fundraising.entities.raft;

import com.gdut.fundraising.blockchain.Block;

public class LogEntry {
    //索引
    long index;

    long term;

    Block data;

    @Override
    public String toString() {
        return "LogEntry{" +
                "index='" + index + '\'' +
                ", term=" + term + '\'' +
                "data='" + data +
                '}';
    }

    public long getIndex() {
        return index;
    }

    public void setIndex(long index) {
        this.index = index;
    }

    public Block getData() {
        return data;
    }

    public void setData(Block data) {
        this.data = data;
    }

    public long getTerm() {
        return term;
    }

    public void setTerm(long term) {
        this.term = term;
    }
}
