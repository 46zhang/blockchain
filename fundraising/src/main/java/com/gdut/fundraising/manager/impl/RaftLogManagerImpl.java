package com.gdut.fundraising.manager.impl;

import com.gdut.fundraising.entities.raft.LogEntry;
import com.gdut.fundraising.manager.RaftLogManager;

public class RaftLogManagerImpl implements RaftLogManager {
    @Override
    public long getLastLogIndex() {
        return 0;
    }

    @Override
    public LogEntry getLastLogEntry() {
        return null;
    }

    @Override
    public long getLsatLogTerm() {
        return 0;
    }
}
