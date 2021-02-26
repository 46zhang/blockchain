package com.gdut.fundraising.manager;

import com.gdut.fundraising.dto.raft.AppendLogRequest;
import com.gdut.fundraising.dto.raft.AppendLogResult;
import com.gdut.fundraising.dto.raft.VoteRequest;
import com.gdut.fundraising.dto.raft.VoteResult;
import com.gdut.fundraising.entities.raft.DefaultNode;

/**
 * 一致性管理器
 * 为raft算法提供投票跟日志添加服务
 */
public interface RaftConsensusManager {
    VoteResult dealVoteRequest(VoteRequest param, DefaultNode node);
    AppendLogResult appendLog(AppendLogRequest param, DefaultNode node);
}
