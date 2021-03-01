package com.gdut.fundraising.entities.raft;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdut.fundraising.constant.raft.MessageType;
import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.dto.raft.AppendLogRequest;
import com.gdut.fundraising.dto.raft.AppendLogResult;
import com.gdut.fundraising.dto.raft.VoteRequest;
import com.gdut.fundraising.dto.raft.VoteResult;
import com.gdut.fundraising.manager.impl.RaftConsensusManagerImpl;
import com.gdut.fundraising.manager.impl.RaftLogManagerImpl;
import com.gdut.fundraising.service.impl.NetworkServiceImpl;
import com.gdut.fundraising.task.RaftThreadPool;
import com.gdut.fundraising.util.JsonResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.concurrent.TimeUnit.MILLISECONDS;

/**
 * 区块链服务器节点
 */
@Component
@Lazy
public class BlockChainNode extends DefaultNode {
    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainNode.class);

//    @Value("${node.list}")
//    private String ipList;
//
//    @Value("${server.port}")
//    private String port;

    /**
     * 初始化函数，在调用构造器时会调用
     */
    @Override
    protected void init() {
        super.init();
    }

    @Override
    @PostConstruct
    public void start() {
        super.start();
    }

    /**
     * 初始化机器节点集
     */
    @Override
    protected void initNodeInfoSet() {
        Properties props = System.getProperties(); //系统属性
        String ipList = (String) props.get("ipList");
        String port = (String) props.get("port");
        String[] strings = ipList.split(",");
        NodeInfoSet nodeInfoSet = new NodeInfoSet();
        nodeInfoSet.setAll(new ArrayList<>());
        for (String s : strings) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setId(s);
            String[] s1 = s.split(":");
            nodeInfo.setIp(s1[0]);
            nodeInfo.setPort(s1[1]);
            nodeInfoSet.getAll().add(nodeInfo);
            if (port.equals(s1[1])) {
                nodeInfoSet.setSelf(nodeInfo);
            }
        }
        this.setNodeInfoSet(nodeInfoSet);
        //调试用
        nextIndexs = new ConcurrentHashMap<>();
        matchIndexs = new ConcurrentHashMap<>();
        for (NodeInfo node : nodeInfoSet.getNodeExceptSelf()) {
            nextIndexs.put(node, 1L);
            matchIndexs.put(node, 0L);
        }
    }

    /**
     * 设置自定义的心跳任务
     */
    @Override
    protected void setHeartBeatTask() {
        this.heartBeatTask = new HeartBeatTask();
    }

    @Override
    protected void setElectionTask() {
        this.electionTask = new ElectionTask();
    }

    @Override
    protected void setRaftConsensusManager() {
        this.raftConsensusManager = new RaftConsensusManagerImpl();
    }

    @Override
    protected void setNetworkService() {
        this.networkService = new NetworkServiceImpl();
    }

    @Override
    protected void setRaftLogManager() {
        this.raftLogManager = new RaftLogManagerImpl();
    }

    /**
     * 发送数据给其他节点去保持一致性
     * @param data
     * @return
     */
    public boolean sendLogToOtherNodeForConsistency(Test data){
        //构建索引
        LogEntry logEntry=buildLogEntry(currentTerm,data.getData(),raftLogManager.getLastLogIndex()+1);
        //下面采用原子量进行复制的统计
        final AtomicInteger success = new AtomicInteger(0);
        //异步
        List<Future<Boolean>> futureList = new CopyOnWriteArrayList<>();

        // 预提交到本地日志
        //here write ahead log(my local log)
        //如果后面发生日志复制达不到一半的条件，则执行撤销
        boolean res= raftLogManager.write(logEntry);
        if(!res){
            LOGGER.info("write logModule fail, logEntry info : {}, log index : {}", logEntry, logEntry.getIndex());
            return false;
        }
        LOGGER.info("write logModule success, logEntry info : {}, log index : {}", logEntry, logEntry.getIndex());

        int count=0;
        //给每个节点同步数据
        for(NodeInfo nodeInfo:nodeInfoSet.getNodeExceptSelf()){
            //如果节点不存活，直接跳过，省去等待时间
//            if(!nodeInfo.isAlive()){
//                continue;
//            }
            ++count;
            futureList.add(replication(nodeInfo,logEntry));
        }

        CountDownLatch latch = new CountDownLatch(futureList.size());
        List<Boolean> resultList = new CopyOnWriteArrayList<>();

        //执行future
        for (Future<Boolean> future : futureList) {
            RaftThreadPool.execute(() -> {
                try {
                    resultList.add(future.get(3000, MILLISECONDS));
                } catch (Exception e) {
                    e.printStackTrace();
                    resultList.add(false);
                } finally {
                    latch.countDown();
                }
            });
        }

        try {
            latch.await(4000, MILLISECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //成功复制的数量
        for (Boolean aBoolean : resultList) {
            if (aBoolean) {
                success.incrementAndGet();
            }
        }

        // 如果存在一个满足N > commitIndex的 N，并且大多数的matchIndex[i] ≥ N成立，
        // 并且log[N].term == currentTerm成立，那么令 commitIndex 等于这个 N （5.3 和 5.4 节）
        List<Long> matchIndexList = new ArrayList<>(matchIndexs.values());
        // 小于 2, 没有意义
        int median = 0;
        if (matchIndexList.size() >= 2) {
            Collections.sort(matchIndexList);
            median = matchIndexList.size() >> 1;
        }
        Long N = matchIndexList.get(median);
        if (N > commitIndex) {
            LogEntry entry = raftLogManager.read(N);
            if (entry != null && entry.getTerm() == currentTerm) {
                commitIndex = N;
            }
        }

        //  响应客户端(成功一半)
        if (success.get() >= (nodeInfoSet.getAll().size() >> 1)) {
            // 更新
            commitIndex = logEntry.getIndex();
//            //  应用到状态机
//            getStateMachine().apply(logEntry);
            lastApplied = commitIndex;

            LOGGER.info("success apply local state machine,  logEntry info : {}", logEntry);
            // 返回成功.
            return true;
        } else {
            // 回滚已经提交的日志.
            raftLogManager.removeOnStartIndex(logEntry.getIndex());
            LOGGER.warn("fail apply local state  machine,  logEntry info : {}", logEntry);
            // TODO 不应用到状态机,但已经记录到日志中.由定时任务从重试队列取出,然后重复尝试,当达到条件时,应用到状态机.
            // 这里应该返回错误, 因为没有成功复制过半机器.
            return false;
        }

    }

    /**
     * 复制日志到其他机器
     * @param nodeInfo
     * @param logEntry
     * @return
     */
    private Future<Boolean> replication(NodeInfo nodeInfo, LogEntry logEntry) {
        return RaftThreadPool.submit(() -> {
            long start = System.currentTimeMillis(), end = start;
            // 10 秒重试时间
            while (end - start < 10 * 1000L) {
                AppendLogRequest appendLogRequest=new AppendLogRequest();
                appendLogRequest.setLeaderId(nodeInfoSet.getSelf().getId());
                appendLogRequest.setTerm(currentTerm);
                appendLogRequest.setLeaderCommit(commitIndex);

                // 以我这边为准, 这个行为通常是成为 leader 后,首次进行 RPC 才有意义.
                Long nextIndex = nextIndexs.get(nodeInfo);
                LinkedList<LogEntry> logEntries = new LinkedList<>();
                if (logEntry.getIndex() >= nextIndex) {
                    for (long i = nextIndex; i <= logEntry.getIndex(); i++) {
                        LogEntry l = raftLogManager.read(i);
                        if (l != null) {
                            logEntries.add(l);
                        }
                    }
                } else {
                    logEntries.add(logEntry);
                }
                // 最小的那个日志.
                LogEntry preLog = logEntries.getFirst();
                appendLogRequest.setEntries(logEntries.toArray(new LogEntry[0]));
                appendLogRequest.setPreLogTerm(preLog.getTerm());
                appendLogRequest.setPrevLogIndex(preLog.getIndex());
                appendLogRequest.setType(MessageType.APPEND_LOG.getValue());
                try {
                    JsonResult response = networkService.post(nodeInfo.getIp(),
                            nodeInfo.getPort(), appendLogRequest);
                    if (response == null) {
                        return false;
                    }
                    ObjectMapper mapper = new ObjectMapper();
                    AppendLogResult result = mapper.convertValue(response.getData(), AppendLogResult.class);

                    if (result != null && result.isSuccess()) {
                        LOGGER.info("append follower entry success , follower=[{}], entry=[{}]", nodeInfo, appendLogRequest.getEntries());
                        // update 这两个追踪值
                        nextIndexs.put(nodeInfo, logEntry.getIndex() + 1);
                        matchIndexs.put(nodeInfo, logEntry.getIndex());
                        return true;
                    } else if (result != null) {
                        // 对方比我大
                        if (result.getTerm() > currentTerm) {
                            LOGGER.warn("follower [{}] term [{}]  more than self, and my term = [{}]," +
                                            " so, I will become follower",
                                    nodeInfo, result.getTerm(), currentTerm);
                            currentTerm = result.getTerm();
                            // 对方的任期比我大，则我成为跟随者。
                            status = NodeStatus.FOLLOWER;
                            return false;
                        } // 没我大, 却失败了,说明 index 不对.或者 term 不对.
                        else {
                            // 递减
                            if (nextIndex == 0) {
                                nextIndex = 1L;
                            }
                            nextIndexs.put(nodeInfo, nextIndex - 1);
                            LOGGER.warn("follower {} nextIndex not match, will reduce nextIndex and retry request" +
                                            "append, nextIndex : [{}]", nodeInfo.getId(),
                                    nextIndex);
                            // 重来, 直到成功.
                        }
                    }

                    end = System.currentTimeMillis();

                } catch (Exception e) {
                    LOGGER.warn(e.getMessage(), e);
                    // TODO 要不要放队列重试
//                        ReplicationFailModel model =  ReplicationFailModel.newBuilder()
//                            .callable(this)
//                            .logEntry(entry)
//                            .peer(peer)
//                            .offerTime(System.currentTimeMillis())
//                            .build();
//                        replicationFailQueue.offer(model);
                    return false;
                }
            }
            // 超时了,没办法了
            return false;
        });
    }


    private LogEntry buildLogEntry(long currentTerm, String data, long index) {
        LogEntry logEntry=new LogEntry();
        logEntry.setData(data);
        logEntry.setTerm(currentTerm);
        logEntry.setIndex(index);
        return logEntry;
    }

    /**
     * 心跳任务，用来检测follower节点是否存活，交换信息
     */
    class HeartBeatTask implements Runnable {

        @Override
        public void run() {

            if (status != NodeStatus.LEADER) {
                return;
            }

            long current = System.currentTimeMillis();
            if (current - preHeartBeatTime < heartBeatTick) {
                return;
            }
            LOGGER.info("===========HeartBeatTask-NextIndex =============");
            for (NodeInfo node : nodeInfoSet.getNodeExceptSelf()) {
                LOGGER.info("node {} nextIndex={}", node.getId(), nextIndexs.get(node));
            }

            preHeartBeatTime = System.currentTimeMillis();

            AppendLogRequest appendLogRequest = new AppendLogRequest(nodeInfoSet.getSelf().getId(),
                    raftLogManager.getLastLogIndex(), raftLogManager.getLsatLogTerm(), currentTerm);

            // 心跳只关心 term
            for (NodeInfo node : nodeInfoSet.getNodeExceptSelf()) {

                RaftThreadPool.execute(() -> {
                    try {
                        //通过心跳任务去获取别人的任期，从而加强一致性
                        JsonResult response = networkService.post(node.getIp(),
                                node.getPort(), appendLogRequest);
                        ObjectMapper mapper = new ObjectMapper();
                        AppendLogResult heartBeatResult = mapper.convertValue(response.getData(), AppendLogResult.class);

                        long term = heartBeatResult.getTerm();

                        if (term > currentTerm) {
                            LOGGER.error("self will become follower, his term : {}, but my term : {}",
                                    term, currentTerm);
                            currentTerm = currentTerm;
                            votedFor = "";
                            status = NodeStatus.FOLLOWER;
                        }
                        node.setAlive(true);
                    } catch (Exception e) {
                        //发生网络故障，说明该节点暂时不存活了
                        //TODO 是否需要这一步进行判断节点情况

                        node.setAlive(false);

                        LOGGER.error("HeartBeatTask  Fail, request node : {}:{}", node.getIp(),
                                node.getPort());
                    }
                }, false);
            }
        }
    }

    /**
     * this is the core of the election.
     * 1. 在转变成候选人后就立即开始选举过程
     * 自增当前的任期号（currentTerm）
     * 给自己投票
     * 重置选举超时计时器
     * 发送请求投票的 RPC 给其他所有服务器
     * 2. 如果接收到大多数服务器的选票，那么就变成领导人
     * 3. 如果接收到来自新的领导人的附加日志 RPC，转变成跟随者
     * 4. 如果选举过程超时，再次发起一轮选举
     */
    class ElectionTask implements Runnable {

        @Override
        public void run() {

            if (status == NodeStatus.LEADER) {
                return;
            }

            long current = System.currentTimeMillis();
            // 基于 RAFT 的随机时间,解决冲突.
            electionTime = electionTime + ThreadLocalRandom.current().nextInt(50);
            //如果选举时间之前都没用收到心跳包或者Candidate请求投票的，则自己成为候选人
            if (current - preHeartBeatTime < electionTime ||
                    current - preElectionTime < electionTime) {
                return;
            }
            status = NodeStatus.CANDIDATE;
            LOGGER.error("node {} will become CANDIDATE and start election leader, current term : [{}], LastEntry : [{}]",
                    nodeInfoSet.getSelf(), currentTerm, raftLogManager.getLastLogEntry());
            //更新新的选举时间
            preElectionTime = (System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(200) + 150);
            //更新任期
            currentTerm = currentTerm + 1;
            // 推荐自己.
            votedFor = nodeInfoSet.getSelf().getId();
            /**
             * 获取除了自己的其他人的列表
             */
            List<NodeInfo> nodes = nodeInfoSet.getNodeExceptSelf();

            ArrayList<Future> futureArrayList = new ArrayList<>();

            LOGGER.info("nodeList size : {}, node list content : {}", nodes.size(), nodes);


            VoteRequest voteRequest = new VoteRequest(nodeInfoSet.getSelf().getId(), raftLogManager.getLastLogIndex(),
                    raftLogManager.getLsatLogTerm(), currentTerm);

            // 发送请求
            for (NodeInfo node : nodes) {

                futureArrayList.add(RaftThreadPool.submit(() -> {
                    long lastTerm = 0L;
                    LogEntry last = raftLogManager.getLastLogEntry();
                    if (last != null) {
                        lastTerm = last.getTerm();
                    }
                    try {
                        JsonResult<VoteResult> response = networkService.post(node.getIp(), node.getPort(), voteRequest);
                        ObjectMapper mapper = new ObjectMapper();

                        VoteResult result = mapper.convertValue(response.getData(), VoteResult.class);
                        return result;

                    } catch (Exception e) {
                        //TODO 这些地方可以添加对于节点剔除的判断
                        LOGGER.error("ElectionTask  Fail, request node : {}:{}", node.getIp(),
                                node.getPort());
                        return null;
                    }
                }));
            }

            AtomicInteger success2 = new AtomicInteger(0);
            CountDownLatch latch = new CountDownLatch(futureArrayList.size());

            LOGGER.info("futureArrayList.size() : {}", futureArrayList.size());
            // 等待结果.
            for (Future future : futureArrayList) {
                RaftThreadPool.submit(() -> {
                    try {

                        VoteResult response = (VoteResult) future.get(3000, MILLISECONDS);
                        if (response == null) {
                            return -1;
                        }
                        boolean isVoteGranted = response.isVoteGranted();
                        /**
                         * 成功被投票，则增加
                         * 否则更新任期
                         */
                        if (isVoteGranted) {
                            success2.incrementAndGet();
                        } else {
                            // 更新自己的任期.
                            long resTerm = response.getTerm();
                            if (resTerm >= currentTerm) {
                                currentTerm = resTerm;
                            }
                        }
                        return 0;
                    } catch (Exception e) {
                        LOGGER.error("future.get exception , e : ", e);
                        return -1;
                    } finally {
                        //减少投票期待
                        latch.countDown();
                    }
                });
            }

            try {
                // 稍等片刻
                latch.await(3500, MILLISECONDS);
            } catch (InterruptedException e) {
                LOGGER.warn("InterruptedException By Master election Task");
            }

            int success = success2.get();
            LOGGER.info("node {} maybe become leader , success count = {} , status : {}", nodeInfoSet.getSelf(), success, NodeStatus.Enum.value(status));
            // 如果投票期间,有其他服务器发送 appendEntry , 就可能变成 follower ,这时,应该停止.
            if (status == NodeStatus.FOLLOWER) {
                return;
            }
            // 加上自身.
            //TODO 这里就是通过投票成为主节点，可以处理一些prepareWork
            if (success >= nodeInfoSet.getAll().size() >> 1) {
                LOGGER.warn("node {} become leader ", nodeInfoSet.getSelf());
                status = NodeStatus.LEADER;
                nodeInfoSet.setLeader(nodeInfoSet.getSelf());
                votedFor = "";
                becomeLeaderToDoThing();
            } else {
                // else 重新选举
                votedFor = "";
            }

        }
    }

    /**
     * 初始化所有的 nextIndex 值为自己的最后一条日志的 index + 1. 如果下次 RPC 时, 跟随者和leader 不一致,就会失败.
     * 那么 leader 尝试递减 nextIndex 并进行重试.最终将达成一致.
     */
    private void becomeLeaderToDoThing() {
        nextIndexs = new ConcurrentHashMap<>();
        matchIndexs = new ConcurrentHashMap<>();
        for (NodeInfo node : nodeInfoSet.getNodeExceptSelf()) {
            nextIndexs.put(node, raftLogManager.getLastLogIndex() + 1);
            matchIndexs.put(node, 0L);
        }
    }
}
