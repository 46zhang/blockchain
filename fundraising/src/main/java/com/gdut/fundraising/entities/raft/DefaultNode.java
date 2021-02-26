package com.gdut.fundraising.entities.raft;


import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.manager.RaftConsensusManager;
import com.gdut.fundraising.manager.RaftLogManager;
import com.gdut.fundraising.service.NetworkService;
import com.gdut.fundraising.service.impl.NetworkServiceImpl;
import com.gdut.fundraising.task.RaftThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * 节点服务
 */
public abstract class DefaultNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultNode.class);
    /**
     * 节点信息
     */
    protected NodeInfoSet nodeInfoSet;

    /**
     * 选举时间间隔基数 15S
     */
    public volatile long electionTime = 15 * 1000;
    /**
     * 上一次选举时间
     */
    public volatile long preElectionTime = 0;

    /**
     * 上次一心跳时间戳
     */
    public volatile long preHeartBeatTime = 0;
    /**
     * 心跳间隔基数 5S
     */
    public final long heartBeatTick = 5 * 1000;

    /**
     * 节点当前状态
     *
     * @see NodeStatus
     */
    public volatile int status = NodeStatus.FOLLOWER;


    /* ============ 所有服务器上持久存在的 ============= */

    /**
     * 服务器最后一次知道的任期号（初始化为 0，持续递增）
     */
    volatile long currentTerm = 0;
    /**
     * 在当前获得选票的候选人的 Id
     */
    volatile String votedFor;



    /* ============ 所有服务器上经常变的 ============= */

    /**
     * 已知的最大的已经被提交的日志条目的索引值
     */
    volatile long commitIndex;

    /**
     * 最后被应用到状态机的日志条目索引值（初始化为 0，持续递增)
     */
    volatile long lastApplied = 0;

    /* ========== 在领导人里经常改变的(选举后重新初始化) ================== */

    /**
     * 对于每一个服务器，需要发送给他的下一个日志条目的索引值（初始化为领导人最后索引值加一）
     */
    Map<NodeInfo, Long> nextIndexs;

    /**
     * 对于每一个服务器，已经复制给他的日志的最高索引值
     */
    Map<NodeInfo, Long> matchIndexs;



    /* ============ 所有服务器上持久存在的 ============= */


    /**
     * 底层网络服务
     */
    protected NetworkService networkService = new NetworkServiceImpl();

    /**
     * 心跳任务
     */
    protected Runnable heartBeatTask;

    /**
     * 选举任务
     */
    protected Runnable electionTask;

    /**
     * 一致性服务
     */
    protected RaftConsensusManager raftConsensusManager;

    /**
     * 日志服务
     */
    protected RaftLogManager raftLogManager;

    DefaultNode() {
        //TODO 添加其他网络节点
        init();
    }

   protected void init(){
       //初始化节点
       initNodeInfoSet();
       //设置定时任务
       setElectionTask();
       //设置选择任务
       setHeartBeatTask();
       //这是底层网络服务
       setNetworkService();
       //设置一致性算法服务
       setRaftConsensusManager();
       //设置日志服务
       setRaftLogManager();
   }

    protected abstract void initNodeInfoSet();


    /**
     * 设置心跳任务
     */
    abstract protected void setHeartBeatTask();


    /**
     * 设置选举任务
     */
    abstract protected void setElectionTask();

    /**
     * 设置一致性服务
     */
    abstract protected void setRaftConsensusManager();


    /**
     * 设置底层网络服务
     */
    abstract protected void setNetworkService();

    /**
     * 设置日志服务
     */
    abstract protected void setRaftLogManager();

    public void start() {
        synchronized (this) {

            //延时启动心跳
            RaftThreadPool.scheduleWithFixedDelay(heartBeatTask, 500);
            //第一次启动的选举任务延时为6S
            RaftThreadPool.scheduleAtFixedRate(electionTask, 6000, 500);
//            //复制
//            RaftThreadPool.execute(replicationFailQueueConsumer);

            LogEntry logEntry = raftLogManager.getLastLogEntry();
            if (logEntry != null) {
                currentTerm = logEntry.getTerm();
            }
//
            LOGGER.info("start success, selfId : {} ", nodeInfoSet.getSelf().getId());
        }
    }

    public void testHeartBeat() {
        heartBeatTask.run();
    }


    public NodeInfoSet getNodeInfoSet() {
        return nodeInfoSet;
    }

    public void setNodeInfoSet(NodeInfoSet nodeInfoSet) {
        this.nodeInfoSet = nodeInfoSet;
    }

    public long getElectionTime() {
        return electionTime;
    }

    public void setElectionTime(long electionTime) {
        this.electionTime = electionTime;
    }

    public long getPreElectionTime() {
        return preElectionTime;
    }

    public void setPreElectionTime(long preElectionTime) {
        this.preElectionTime = preElectionTime;
    }

    public long getPreHeartBeatTime() {
        return preHeartBeatTime;
    }

    public void setPreHeartBeatTime(long preHeartBeatTime) {
        this.preHeartBeatTime = preHeartBeatTime;
    }

    public long getHeartBeatTick() {
        return heartBeatTick;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public long getCurrentTerm() {
        return currentTerm;
    }

    public void setCurrentTerm(long currentTerm) {
        this.currentTerm = currentTerm;
    }

    public String getVotedFor() {
        return votedFor;
    }

    public void setVotedFor(String votedFor) {
        this.votedFor = votedFor;
    }

    public long getCommitIndex() {
        return commitIndex;
    }

    public void setCommitIndex(long commitIndex) {
        this.commitIndex = commitIndex;
    }

    public long getLastApplied() {
        return lastApplied;
    }

    public void setLastApplied(long lastApplied) {
        this.lastApplied = lastApplied;
    }

    public Map<NodeInfo, Long> getNextIndexs() {
        return nextIndexs;
    }

    public void setNextIndexs(Map<NodeInfo, Long> nextIndexs) {
        this.nextIndexs = nextIndexs;
    }

    public Map<NodeInfo, Long> getMatchIndexs() {
        return matchIndexs;
    }

    public void setMatchIndexs(Map<NodeInfo, Long> matchIndexs) {
        this.matchIndexs = matchIndexs;
    }


    public NetworkService getNetworkService() {
        return networkService;
    }


    public RaftConsensusManager getRaftConsensusManager() {
        return raftConsensusManager;
    }



    public RaftLogManager getRaftLogManager() {
        return raftLogManager;
    }

}
