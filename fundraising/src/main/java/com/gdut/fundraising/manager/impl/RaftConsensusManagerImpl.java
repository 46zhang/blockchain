package com.gdut.fundraising.manager.impl;


import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.dto.raft.AppendLogRequest;
import com.gdut.fundraising.dto.raft.AppendLogResult;
import com.gdut.fundraising.dto.raft.VoteRequest;
import com.gdut.fundraising.dto.raft.VoteResult;
import com.gdut.fundraising.entities.raft.DefaultNode;
import com.gdut.fundraising.entities.raft.LogEntry;
import com.gdut.fundraising.entities.raft.NodeInfoSet;
import com.gdut.fundraising.manager.RaftConsensusManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;

public class RaftConsensusManagerImpl implements RaftConsensusManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftConsensusManagerImpl.class);


    public final ReentrantLock voteLock = new ReentrantLock();
    public final ReentrantLock appendLock = new ReentrantLock();

    /**
     * 请求投票 RPC
     * <p>
     * 接收者实现：
     * 如果term < currentTerm返回 false （5.2 节）
     * 如果 votedFor 为空或者就是 candidateId，并且候选人的日志至少和自己一样新，那么就投票给他（5.2 节，5.4 节）
     */

    @Override
    public VoteResult dealVoteRequest(VoteRequest param, final DefaultNode node) {
        try {
            if (!voteLock.tryLock()) {
                return VoteResult.fail(node.getCurrentTerm());
            }

            // 对方任期没有自己新
            if (param.getTerm() < node.getCurrentTerm()) {
                return VoteResult.fail(node.getCurrentTerm());
            }

            LOGGER.info("node {} current vote for [{}], param candidateId : {}", node.getNodeInfoSet().getSelf(),
                    node.getVotedFor(), param.getCandidateId());
            LOGGER.info("node {} current term {}, peer term : {}", node.getNodeInfoSet().getSelf(),
                    node.getCurrentTerm(), param.getTerm());
            // (当前节点并没有投票 或者 已经投票过了且是对方节点) && 对方日志和自己一样新
            if ((StringUtils.isEmpty(node.getVotedFor()) || node.getVotedFor().equals(param.getCandidateId()))) {

                if (node.getRaftLogManager().getLastLogEntry() != null) {
                    // 对方没有自己新
                    if (node.getRaftLogManager().getLastLogEntry().getTerm() > param.getLastLogTerm()) {
                        return VoteResult.fail(node.getCurrentTerm());
                    }
                    // 对方没有自己新
                    if (node.getRaftLogManager().getLastLogIndex() > param.getLastLogIndex()) {
                        return VoteResult.fail(node.getCurrentTerm());
                    }
                }
                //更新node状态
                updateNode(param,node);
                // 返回成功
                return VoteResult.ok(node.getCurrentTerm());
            }

            return VoteResult.fail(node.getCurrentTerm());

        } finally {
            voteLock.unlock();
        }
    }

    /**
     * 添加日志
     * @param param
     * @param node
     * @return
     */
    @Override
    public AppendLogResult appendLog(AppendLogRequest param, DefaultNode node) {
        AppendLogResult result = AppendLogResult.fail(node.getCurrentTerm());
        try {
            if (!appendLock.tryLock()) {
                return result;
            }

            result.setTerm(node.getCurrentTerm());
            // 不够格
            if (param.getTerm() < node.getCurrentTerm()) {
                return result;
            }
           final NodeInfoSet nodeInfoSet=node.getNodeInfoSet();
            /**
             * 更新心跳时间
             */
            node.preHeartBeatTime = System.currentTimeMillis();
            nodeInfoSet.setLeader(nodeInfoSet.getNode(param.getLeaderId()));

            // 对方任期更大
            if (param.getTerm() >= node.getCurrentTerm()) {
                LOGGER.debug("node {} become FOLLOWER, currentTerm : {}, param Term : {}, param serverId",
                        nodeInfoSet.getSelf(), node.getCurrentTerm(), param.getTerm(), param.getLeaderId());
                // 自动降为follower
                node.status = NodeStatus.FOLLOWER;
            }
            // 使用对方的 term.
            node.setCurrentTerm(param.getTerm());

            //心跳
            if (param.getEntries() == null || param.getEntries().length == 0) {
                LOGGER.info("node {} append heartbeat success , he's term : {}, my term : {}",
                        param.getLeaderId(), param.getTerm(), node.getCurrentTerm());
                return AppendLogResult.ok(node.getCurrentTerm());
            }

            // 真实日志
            // 第一次
            if (node.getRaftLogManager().getLastLogIndex() != 0 && param.getPrevLogIndex() != 0) {
                LogEntry logEntry;
                if ((logEntry = node.getRaftLogManager().read(param.getPrevLogIndex())) != null) {
                    // 如果日志在 prevLogIndex 位置处的日志条目的任期号和 prevLogTerm 不匹配，则返回 false
                    // 需要减小 nextIndex 重试.
                    if (logEntry.getTerm() != param.getPreLogTerm()) {
                        return result;
                    }
                } else {
                    // index 不对, 需要递减 nextIndex 重试.
                    return result;
                }

            }

            // 如果已经存在的日志条目和新的产生冲突（索引值相同但是任期号不同），删除这一条和之后所有的
            LogEntry existLog = node.getRaftLogManager().read(((param.getPrevLogIndex() + 1)));
            if (existLog != null && existLog.getTerm() != param.getEntries()[0].getTerm()) {
                // 删除这一条和之后所有的, 然后写入日志和状态机.
                node.getRaftLogManager().removeOnStartIndex(param.getPrevLogIndex() + 1);
            } else if (existLog != null) {
                // 已经有日志了, 不能重复写入.
                result.setSuccess(true);
                return result;
            }

            // 写进日志并且应用到状态机
            for (LogEntry entry : param.getEntries()) {
                node.getRaftLogManager().write(entry);
                //暂时不用状态机
                //node.stateMachine.apply(entry);
                result.setSuccess(true);
            }

            //如果 leaderCommit > commitIndex，令 commitIndex 等于 leaderCommit 和 新日志条目索引值中较小的一个
            if (param.getLeaderCommit() > node.getCommitIndex()) {
                int commitIndex = (int) Math.min(param.getLeaderCommit(), node.getRaftLogManager().getLastLogIndex());
                node.setCommitIndex(commitIndex);
                node.setLastApplied(commitIndex);
            }

            result.setTerm(node.getCurrentTerm());

            node.status = NodeStatus.FOLLOWER;
            // TODO, 是否应当在成功回复之后, 才正式提交? 防止 leader "等待回复"过程中 挂掉.
            return result;
        } finally {
            appendLock.unlock();
        }
    }


    /**
     * 每轮选举后，更新node状态
     * <p>更新自身状态</p>
     * <p>更新新一轮的选举时间</p>
     * <p>更新leader</p>
     * <p>更新任期</p>
     * @param param
     * @param node
     */
    private void updateNode(VoteRequest param, DefaultNode node) {
        // 切换状态
        node.status = NodeStatus.FOLLOWER;
        //更新新的选举时间
        node.setPreElectionTime(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(200) + 150);
        // 更新??
        // 这么快就要更新leader了吗
        node.getNodeInfoSet().setLeader(node.getNodeInfoSet().getNode(param.getCandidateId()));
        node.setCurrentTerm(param.getTerm());
        node.setVotedFor(param.getCandidateId());
    }


}
