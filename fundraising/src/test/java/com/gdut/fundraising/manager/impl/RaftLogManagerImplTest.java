package com.gdut.fundraising.manager.impl;

import com.gdut.fundraising.entities.raft.LogEntry;
import com.gdut.fundraising.manager.RaftLogManager;
import org.testng.Assert;
import org.testng.annotations.Test;

import static org.testng.Assert.*;

public class RaftLogManagerImplTest {

    @Test
    public void testRemoveOnStartIndex() {
        RaftLogManager raftLogManager = new RaftLogManagerImpl();
        LogEntry logEntry = new LogEntry();
        logEntry.setIndex(1);
        logEntry.setTerm(1);
        logEntry.setData("afsdfsdfs");
        boolean res = raftLogManager.write(logEntry);
        raftLogManager.removeOnStartIndex(1);
        Assert.assertTrue(res);
        Assert.assertTrue(raftLogManager.getLastLogIndex()==0);

        logEntry.setIndex(raftLogManager.getLastLogIndex() + 1);
        res = raftLogManager.write(logEntry);

        Assert.assertTrue(res);
        LogEntry logEntry2 = new LogEntry();
        logEntry2.setData("sfsdgftgw4t43");
        logEntry2.setTerm(1);
        logEntry2.setIndex(raftLogManager.getLastLogIndex() + 1);
        res = raftLogManager.write(logEntry2);
        Assert.assertTrue(res);

        Assert.assertTrue(raftLogManager.getLastLogIndex()==2);

        raftLogManager.removeOnStartIndex(1);
        Assert.assertTrue(raftLogManager.getLastLogIndex()==0);
    }
}