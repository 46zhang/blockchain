package com.gdut.fundraising.task;//package com.gdut.fundraising.task;
//
//import com.gdut.fundraising.constant.raft.NodeStatus;
//import com.gdut.fundraising.dto.raft.AppendLogRequest;
//import com.gdut.fundraising.dto.raft.AppendLogResult;
//import com.gdut.fundraising.entities.raft.BlockChainNode;
//
//import com.gdut.fundraising.entities.raft.NodeInfo;
//import com.gdut.fundraising.entities.raft.NodeInfoSet;
//import com.gdut.fundraising.manager.RaftLogManager;
//import com.gdut.fundraising.service.NetworkService;
//import com.gdut.fundraising.util.JsonResult;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.beans.factory.annotation.Autowired;
//
//import org.springframework.stereotype.Component;
//
//@Component
//public class BlockChainHeartBeatTask extends HeartBeatTask {
//    private static final Logger LOGGER = LoggerFactory.getLogger(BlockChainHeartBeatTask.class);
//
//    @Autowired(required = false)
//    BlockChainNode blockChainNode;
//
//    @Override
//    public void run() {
//        if (blockChainNode.getStatus() != NodeStatus.LEADER) {
//            return;
//        }
//
//        long current = System.currentTimeMillis();
//        NodeInfoSet nodeInfoSet = blockChainNode.getNodeInfoSet();
//        NetworkService networkService=blockChainNode.getNetworkService();
//        final RaftLogManager raftLogManager = blockChainNode.getRaftLogManager();
//        if (current - blockChainNode.getPreHeartBeatTime() < blockChainNode.getHeartBeatTick()) {
//            return;
//        }
//        LOGGER.info("===========HeartBeatTask-NextIndex =============");
//        for (NodeInfo node : blockChainNode.getNodeInfoSet().getNodeExceptSelf()) {
//            LOGGER.info("node {} nextIndex={}", node.getId(), blockChainNode.getNextIndexs().get(node));
//        }
//
//        blockChainNode.setPreHeartBeatTime(System.currentTimeMillis());
//
//        AppendLogRequest appendLogRequest = new AppendLogRequest(nodeInfoSet.getSelf().getId(),
//                raftLogManager.getLastLogIndex(), raftLogManager.getLsatLogTerm(), blockChainNode.getCurrentTerm());
//
//
//        // 心跳只关心 term
//        for (NodeInfo node : blockChainNode.getNodeInfoSet().getNodeExceptSelf()) {
//
//            RaftThreadPool.execute(() -> {
//                try {
//                    //通过心跳任务去获取别人的任期，从而加强一致性
//                    JsonResult response = networkService.post(node.getIp(),
//                            node.getPort(), appendLogRequest);
//                    AppendLogResult appendLogResult = (AppendLogResult) response.getData();
//                    long term = appendLogResult.getTerm();
//
//                    if (term > blockChainNode.getCurrentTerm()) {
//                        LOGGER.error("self will become follower, his term : {}, but my term : {}",
//                                term, blockChainNode.getCurrentTerm());
//                        blockChainNode.setCurrentTerm(term);
//                        blockChainNode.setVotedFor("");
//                        blockChainNode.setStatus(NodeStatus.FOLLOWER);
//                    }
//                } catch (Exception e) {
//                    //发生网络故障，说明该节点暂时不存活了
//                    //TODO 是否需要这一步进行判断节点情况
//                    synchronized (node) {
//                        node.setAlive(false);
//                    }
//                    LOGGER.error("HeartBeatTask  Fail, request node : {}:{}", node.getIp(),
//                            node.getPort());
//                }
//            }, false);
//        }
//    }
//}
