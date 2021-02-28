package com.gdut.fundraising.manager;

import com.gdut.fundraising.entities.raft.LogEntry;

public interface RaftLogManager {
    long getLastLogIndex();

    LogEntry getLastLogEntry();

    long getLsatLogTerm();

    LogEntry read(long index);

    void removeOnStartIndex(long l);

    void write(LogEntry entry);
}
