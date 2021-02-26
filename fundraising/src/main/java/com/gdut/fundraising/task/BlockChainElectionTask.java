package com.gdut.fundraising.task;//package com.gdut.fundraising.task;
//
//import com.gdut.fundraising.constant.raft.NodeStatus;
//import com.gdut.fundraising.dto.raft.VoteRequest;
//import com.gdut.fundraising.dto.raft.VoteResult;
//import com.gdut.fundraising.entities.raft.BlockChainNode;
//import com.gdut.fundraising.entities.raft.LogEntry;
//import com.gdut.fundraising.entities.raft.NodeInfo;
//import com.gdut.fundraising.entities.raft.NodeInfoSet;
//import com.gdut.fundraising.manager.RaftLogManager;
//import com.gdut.fundraising.service.NetworkService;
//import com.gdut.fundraising.util.JsonResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.context.annotation.Lazy;
//import org.springframework.stereotype.Component;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.ConcurrentHashMap;
//import java.util.concurrent.CountDownLatch;
//import java.util.concurrent.Future;
//import java.util.concurrent.ThreadLocalRandom;
//import java.util.concurrent.atomic.AtomicInteger;
//
//import static java.util.concurrent.TimeUnit.MILLISECONDS;
//
///**
// * this is the core of the election.
// * 1. 在转变成候选人后就立即开始选举过程
// * 自增当前的任期号（currentTerm）
// * 给自己投票
// * 重置选举超时计时器
// * 发送请求投票给其他所有服务器
// * 2. 如果接收到大多数服务器的选票，那么就变成领导人
// * 3. 如果接收到来自新的领导人的附加日志，转变成跟随者
// * 4. 如果选举过程超时，再次发起一轮选举
// */
//@Component
//public class BlockChainElectionTask extends ElectionTask {
//    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainHeartBeatTask.class);
//
//    @Autowired(required = false)
//    BlockChainNode blockChainNode;
//
//    @Override
//    public void run() {
//        if (blockChainNode.getStatus() == NodeStatus.LEADER) {
//            return;
//        }
//
//        long current = System.currentTimeMillis();
//        RaftLogManager raftLogManager = blockChainNode.getRaftLogManager();
//        long currentTerm = blockChainNode.getCurrentTerm();
//        NodeInfoSet nodeInfoSet = blockChainNode.getNodeInfoSet();
//        NetworkService networkService=blockChainNode.getNetworkService();
//        // 基于 RAFT 的随机时间,解决冲突.
//        blockChainNode.setElectionTime(blockChainNode.getElectionTime() + ThreadLocalRandom.current().nextInt(50));
//        //如果选举时间之前都没用收到心跳包或者Candidate请求投票的，则自己成为候选人
//        if (current - blockChainNode.getPreHeartBeatTime() < blockChainNode.getElectionTime() ||
//                current - blockChainNode.getPreElectionTime() < blockChainNode.getElectionTime()) {
//            return;
//        }
//        blockChainNode.setStatus(NodeStatus.CANDIDATE);
//        LOGGER.error("node {} will become CANDIDATE and start election leader, current term : [{}], LastEntry : [{}]",
//                blockChainNode.getNodeInfoSet().getSelf(), blockChainNode.getCurrentTerm(), blockChainNode.getRaftLogManager().getLastLogEntry());
//        //更新新的选举时间
//        blockChainNode.setPreElectionTime(System.currentTimeMillis() + ThreadLocalRandom.current().nextInt(200) + 150);
//        //更新任期
//        blockChainNode.setCurrentTerm(blockChainNode.getCurrentTerm() + 1);
//        // 推荐自己.
//        blockChainNode.setVotedFor(nodeInfoSet.getSelf().getId());
//
//        /**
//         * 获取除了自己的其他人的列表
//         */
//        List<NodeInfo> nodes = nodeInfoSet.getNodeExceptSelf();
//
//        ArrayList<Future> futureArrayList = new ArrayList<>();
//
//        LOGGER.info("nodeList size : {}, node list content : {}", nodes.size(), nodes);
//
//        VoteRequest voteRequest = new VoteRequest(nodeInfoSet.getSelf().getId(), raftLogManager.getLastLogIndex(),
//                raftLogManager.getLastLogEntry().getTerm(), currentTerm);
//
//        // 发送请求
//        for (NodeInfo node : nodes) {
//
//            futureArrayList.add(RaftThreadPool.submit(() -> {
//                long lastTerm = 0L;
//                LogEntry last = raftLogManager.getLastLogEntry();
//                if (last != null) {
//                    lastTerm = last.getTerm();
//                }
//                try {
//                    JsonResult<VoteResult> response = networkService.post(node.getIp(), node.getPort(), voteRequest);
//                    return response;
//
//                } catch (Exception e) {
//                    //TODO 这些地方可以添加对于节点剔除的判断
//                    LOGGER.error("ElectionTask  Fail, request node : {}:{}", node.getIp(),
//                            node.getPort());
//                    return null;
//                }
//            }));
//        }
//
//        AtomicInteger success2 = new AtomicInteger(0);
//        CountDownLatch latch = new CountDownLatch(futureArrayList.size());
//
//        LOGGER.info("futureArrayList.size() : {}", futureArrayList.size());
//        // 等待结果.
//        for (Future future : futureArrayList) {
//            RaftThreadPool.submit(() -> {
//                try {
//
//                    JsonResult<VoteResult> response = (JsonResult<VoteResult>) future.get(3000, MILLISECONDS);
//                    if (response == null) {
//                        return -1;
//                    }
//                    boolean isVoteGranted = ((VoteResult) response.getData()).isVoteGranted();
//                    /**
//                     * 成功被投票，则增加
//                     * 否则更新任期
//                     */
//                    if (isVoteGranted) {
//                        success2.incrementAndGet();
//                    } else {
//                        // 更新自己的任期.
//                        long resTerm = ((VoteResult) response.getData()).getTerm();
//                        if (resTerm >= currentTerm) {
//                            blockChainNode.setCurrentTerm(resTerm);
//                        }
//                    }
//                    return 0;
//                } catch (Exception e) {
//                    LOGGER.error("future.get exception , e : ", e);
//                    return -1;
//                } finally {
//                    //减少投票期待
//                    latch.countDown();
//                }
//            });
//        }
//
//        try {
//            // 稍等片刻
//            latch.await(3500, MILLISECONDS);
//        } catch (InterruptedException e) {
//            LOGGER.warn("InterruptedException By Master election Task");
//        }
//
//        int success = success2.get();
//        LOGGER.info("node {} maybe become leader , success count = {} , status : {}", nodeInfoSet.getSelf(),
//                success, NodeStatus.Enum.value(blockChainNode.getStatus()));
//        // 如果投票期间,有其他服务器发送 appendEntry , 就可能变成 follower ,这时,应该停止.
//        if (blockChainNode.getStatus() == NodeStatus.FOLLOWER) {
//            return;
//        }
//        // 加上自身.
//        //TODO 这里就是通过投票成为主节点，可以处理一些prepareWork
//        if (success >= nodeInfoSet.getAll().size() >> 1) {
//            LOGGER.warn("node {} become leader ", nodeInfoSet.getSelf());
//            blockChainNode.setStatus(NodeStatus.LEADER);
//            nodeInfoSet.setLeader(nodeInfoSet.getSelf());
//            blockChainNode.setVotedFor("");
//            becomeLeaderToDoThing();
//        } else {
//            // else 重新选举
//            blockChainNode.setVotedFor("");
//        }
//
//    }
//
//    /**
//     * 初始化所有的 nextIndex 值为自己的最后一条日志的 index + 1. 如果下次 RPC 时, 跟随者和leader 不一致,就会失败.
//     * 那么 leader 尝试递减 nextIndex 并进行重试.最终将达成一致.
//     */
//    private void becomeLeaderToDoThing() {
//        blockChainNode.setNextIndexs(new ConcurrentHashMap<>());
//        blockChainNode.setMatchIndexs(new ConcurrentHashMap<>());
//        for (NodeInfo node : blockChainNode.getNodeInfoSet().getNodeExceptSelf()) {
//            blockChainNode.getNextIndexs().put(node, blockChainNode.getRaftLogManager().getLastLogIndex() + 1);
//            blockChainNode.getMatchIndexs().put(node, 0L);
//        }
//    }
//}
