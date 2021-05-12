package com.gdut.fundraising.service.impl;

import com.gdut.fundraising.blockchain.*;
import com.gdut.fundraising.constant.GraphLineTypeEnum;
import com.gdut.fundraising.constant.NodeNameEnum;
import com.gdut.fundraising.constant.raft.NodeStatus;
import com.gdut.fundraising.dto.FundFlowGraphResult;
import com.gdut.fundraising.dto.NodeQueryResult;
import com.gdut.fundraising.entities.*;
import com.gdut.fundraising.entities.raft.BlockChainNode;
import com.gdut.fundraising.entities.raft.NodeInfo;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.mapper.UserMapper;
import com.gdut.fundraising.service.BCTService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BCTServiceImpl implements BCTService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BCTServiceImpl.class);

    @Autowired
    private BlockChainNode blockChainNode;

    @Resource
    UserMapper userMapper;

    public static HashMap<String, String> kvTable = new HashMap<>();

    /**
     * 募捐
     *
     * @param userId
     * @param projectId
     * @param money
     * @return
     */
    @Override
    public boolean contribution(String userId, String projectId, long money) {
        Peer peer = blockChainNode.getPeer();
        //创币交易
        Transaction transaction = peer.getTransactionService().createCoinBaseTransaction(peer, peer.getWallet().getAddress(),
                userId, projectId, money);

        if (transaction == null) {
            LOGGER.error("transaction create fail!!  userId:{},projectId:{},money:{}", userId, projectId, money);
            return false;
        }

        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        if (blockChainNode.status == NodeStatus.LEADER) {
            return doConsensus(peer);
        } else {
            //广播交易给到leader节点，由它去创建候选区块,并进行共识
            //TODO 返回false 对交易进行回滚
            return blockChainNode.broadcastTransaction(txs);
        }
    }

    /**
     * leader节点做一致性该做的事
     *
     * @param peer
     * @return
     */
    private boolean doConsensus(Peer peer) {
        if (blockChainNode.status != NodeStatus.LEADER) {
            throw new BaseException(400, "区块链共识出错，原因为非节点想代替主节点引导共识");
        }

        //创建区块
        Block block = createBlock(peer);

        //添加区块到区块链中,该方法是线程安全的
        boolean res = peer.getBlockChainService().addBlockToChain(peer, block);
        //保存utxo set
        res = res && peer.saveUTXOSet();
        //保存transaction set
        res = res && peer.saveTransactionSet();
        if (!res) {
            rollBack(peer, block);
            return false;
        }

        //进行共识
        res = blockChainNode.sendLogToOtherNodeForConsistency(block);

        //如果共识成功，则插入给该区块,否则回滚该区块,该区块必须是位于最后一个
        if (!res) {
            rollBack(peer, block);
        }

        return res;
    }

    private void rollBack(Peer peer, Block block) {
        //该方法是线程安全的
        peer.getBlockChainService().rollBackBlock(peer, block);
        //保存对应的内容
        peer.saveUTXOSet();
        peer.saveTransactionSet();
    }


    /**
     * 管理员花费金额
     *
     * @param spendEntity
     * @return
     */
    @Override
    public boolean useMoney(SpendEntity spendEntity) {
        Peer peer = blockChainNode.getPeer();
        //创币交易
        Transaction transaction = peer.getTransactionService().createTransaction(peer, spendEntity.getToAddress(),
                spendEntity.getMoney(), spendEntity.getProjectId(), spendEntity.getFormUserId());

        if (transaction == null) {
            LOGGER.error("transaction create fail!!  spendEntity:{}", spendEntity);
            return false;
        }

        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        spendEntity.setOrderTime(timeFormat.format(transaction.getLockTime()));

        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        if (blockChainNode.status == NodeStatus.LEADER) {
            return doConsensus(peer);
        } else {
            //广播交易给到leader节点，由它去创建候选区块,并进行共识
            //TODO 返回false 对交易进行回滚
            return blockChainNode.broadcastTransaction(txs);
        }
    }

    /**
     * 校验其他节点发送过来的数据是否正确
     *
     * @param txs
     * @return
     */
    @Override
    public boolean verifyTransactionsFromOtherNode(List<Transaction> txs) {
        Peer peer = blockChainNode.getPeer();
        for (Transaction t : txs) {
            boolean res = peer.getTransactionService().verifyTransaction(peer, t);
            if (!res) {
                return false;
            }
        }

        //添加到交易池
        for (Transaction t : txs) {
            peer.getTransactionService().addTransaction(peer, t);
        }

        return doConsensus(peer);
    }

    @Override
    public boolean addBlockToChain(Block block) {
        //添加区块到区块链中,该方法是线程安全的
        Peer peer = blockChainNode.getPeer();
        return peer.getBlockChainService().addBlockToChain(peer, block);
    }

//    /**
//     * 获取某个捐款的全部资金使用记录
//     *
//     * @param txId
//     * @return
//     */
//    @Override
//    public List<FundFlowEntity> getTransactionFundFlowByBlockChain(String txId) {
//        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
//        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();
//        String aimTxId = txId;
//        //需要获取溯源该txId中的交易数据,找出其本身的交易以及其后续的交易
//        //后续交易的意思就是 A交易产生了 utxoA ，B交易使用utxoA作为输入单元，那么B就为A的后续交易，同理可得后续可能会有交易C、D
//        //因此要找出所有的相关交易
//        for (Block block : blockChain) {
//            for (Transaction tx : block.getTxs()) {
//                //同个交易的话直接构建资金流信息
//                if (tx.getId().equals(aimTxId)) {
//                    long sum = 0;
//                    for (Vout vout : tx.getOutList()) {
//                        sum += vout.getMoney();
//                    }
//                    //构建资金流信息
//                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx,block);
//
//                    fundFlowEntities.add(fundFlowEntity);
//                } else {
//                    //判断是否有人用txId的vout作为输入单元，有的话相当于就是使用了该txId的钱来做输入
//                    List<Vin> vins = tx.getInList();
//                    for(Vin vin:vins){
//                        if(vin.getToSpent().getTxId().equals(aimTxId)){
//                            //更新新的交易id
//                            aimTxId=tx.getId();
//
//                        }
//                    }
//                }
//            }
//        }
//        return null;
//    }

    /**
     * 获取所有用户捐款
     *
     * @param userId
     * @return
     */
    @Override
    public List<FundFlowEntity> getUserAllContributionFlow(String userId) {
        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i = blockChain.size() - 1; i >= 0; --i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                //获取用户的所有捐款信息，捐款全部都是创币交易
                if (tx.getFromUserId().equals(userId) && tx.isCoinBase()) {
                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx, block, i);
                    fundFlowEntities.add(fundFlowEntity);
                }
            }
        }
        return fundFlowEntities;
    }

    @Override
    public List<FundFlowEntity> getUserProjectAllFundFlow(String userId, String projectId) {
        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i = blockChain.size() - 1; i >= 0; --i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                if (tx.getFromUserId().equals(userId) && tx.getFormProjectId().equals(projectId)) {
                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx, block, i);
                    fundFlowEntities.add(fundFlowEntity);
                }
            }
        }
        return fundFlowEntities;
    }

    @Override
    public List<FundFlowEntity> getProjectFundFlow(String projectId) {
        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i = blockChain.size() - 1; i >= 0; --i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                if (tx.getFormProjectId().equals(projectId)) {
                    FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx, block, i);
                    fundFlowEntities.add(fundFlowEntity);
                }
            }
        }
        return fundFlowEntities;
    }

    @Override
    public List<NodeQueryResult> getNodeQueryList() {
        List<NodeQueryResult> list = new ArrayList<>();
        Properties props = System.getProperties(); //系统属性
        String ipList = (String) props.get("ipList");
        String[] strings = ipList.split(",");
        String port = (String) props.get("port");
        //设置每个节点的名称以及对应的地址(对前端管理员而言相当于是id)
        for (String s : strings) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setId(s);
            String[] s1 = s.split(":");
            //自身的节点不用返回，因为没必要自己把钱给自己
            if (s1[1].equals(port)) {
                continue;
            }
            NodeQueryResult nodeQueryResult = new NodeQueryResult();
            nodeQueryResult.setId(blockChainNode.getPeer().getAddressFromFile(s1[1]));
            NodeNameEnum nodeNameEnum = NodeNameEnum.getNodeNameEnumByPort(s1[1]);
            nodeQueryResult.setName(nodeNameEnum.getName());
            list.add(nodeQueryResult);
        }

        return list;
    }

    @Override
    public List<FundFlowEntity> getAllBlockFundFlow() {
        List<FundFlowEntity> fundFlowEntities = new ArrayList<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i = blockChain.size() - 1; i >= 0; --i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                FundFlowEntity fundFlowEntity = buildFundFlowEntity(tx, block, i);
                fundFlowEntities.add(fundFlowEntity);
            }
        }
        return fundFlowEntities;
    }

    @Override
    public FundFlowGraphResult getProjectFundGraph(String projectId) {
        FundFlowGraphResult fundFlowGraphResult = new FundFlowGraphResult();

        Set<GraphNodeEntity> nodeSet = new HashSet<>();
        Map<GraphPointerEntity, GraphEdgeEntity> edgesMap = new HashMap<>();
        Map<GraphNodeEntity, Long> nodeBalance = new HashMap<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();

        for (int i =0; i < blockChain.size(); ++i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                if (tx.getFormProjectId().equals(projectId)) {
                    buildFundGraph(nodeSet, edgesMap, tx, nodeBalance);
                }
            }
        }

        //把上述的边集、点集合加起来
        List<GraphNodeEntity> nodes = new ArrayList<>(nodeSet);
        List<GraphEdgeEntity> edges = new ArrayList<>(edgesMap.values());

        fundFlowGraphResult.setNodes(nodes);
        fundFlowGraphResult.setEdges(edges);
        return fundFlowGraphResult;
    }

    @Override
    public FundFlowGraphResult getOneUserProjectFundGraph(String userId, String projectId) {
        FundFlowGraphResult fundFlowGraphResult = new FundFlowGraphResult();

        Set<GraphNodeEntity> nodeSet = new HashSet<>();
        Map<GraphPointerEntity, GraphEdgeEntity> edgesMap = new HashMap<>();
        List<Block> blockChain = blockChainNode.getPeer().getBlockChain();
        Map<GraphNodeEntity, Long> nodeBalanceMap = new HashMap<>();
        for (int i =0; i < blockChain.size(); ++i) {
            Block block = blockChain.get(i);
            //实际上时间复杂度不会很高，因为每个block只有1-2个transaction
            for (Transaction tx : block.getTxs()) {
                if (tx.getFormProjectId().equals(projectId) && tx.getFromUserId().equals(userId)) {
                    buildFundGraph(nodeSet, edgesMap, tx, nodeBalanceMap);
                }
            }
        }

        //把上述的边集、点集合加起来
        List<GraphNodeEntity> nodes = new ArrayList<>(nodeSet);
        List<GraphEdgeEntity> edges = new ArrayList<>(edgesMap.values());

        fundFlowGraphResult.setNodes(nodes);
        fundFlowGraphResult.setEdges(edges);
        return fundFlowGraphResult;
    }

    private void buildFundGraph(Set<GraphNodeEntity> nodeSet, Map<GraphPointerEntity,
            GraphEdgeEntity> edgesMap, Transaction tx, Map<GraphNodeEntity, Long> nodeBalanceMap) {

        GraphNodeEntity from = new GraphNodeEntity();
        GraphNodeEntity to = new GraphNodeEntity();

        //设置资金流向的地址
        to.setId(tx.getOutList().get(0).getToAddress());
        to.setLabel(transferAddressToName(tx.getOutList().get(0).getToAddress()));
        if (tx.isCoinBase()) {
            //如果是创币交易，那么资金来源就是用户本身
            from.setId(tx.getFromUserId());
            from.setLabel(findUserNameById(tx.getFromUserId()));
        } else {
            //如果不是创币交易的话，就直接用输入地址就行
            from.setId(tx.getInList().get(0).getAddress());
            from.setLabel(transferAddressToName(tx.getInList().get(0).getAddress()));
        }

        nodeSet.add(from);
        nodeSet.add(to);

        //查找金额
        long sum = tx.getOutputMoneyNoIncludeChange();
        long num = 0;
        GraphPointerEntity graphPointerEntity = new GraphPointerEntity(from, to);
        GraphEdgeEntity edge = edgesMap.get(graphPointerEntity);
        //需要判断是否存在同起点，同终点的边，是的话，把他们的金额加起来
        if (edge == null) {
            GraphEdgeEntity edge1 = new GraphEdgeEntity();
            edge1.setLabel(String.valueOf(sum));
            edge1.setSource(from.getId());
            edge1.setTarget(to.getId());
            edge1.setType(GraphLineTypeEnum.QUADRATIC.getType());
            edgesMap.put(graphPointerEntity, edge1);
        } else {
            num = Long.parseLong(edge.getLabel());
            edge.setLabel(String.valueOf(num + sum));
        }

//        //计算当前节点的余额
//        long balance = sum+(nodeBalanceMap.get(to) == null ? 0 : nodeBalanceMap.get(to));
//        //如果是创币交易，这笔捐款属于它的金钱
//        nodeBalanceMap.put(to, balance);
//        if (!tx.isCoinBase()) {
//            nodeBalanceMap.put(from, (nodeBalanceMap.get(from) == null ? 0 : nodeBalanceMap.get(from)) - sum);
//        }
//
//
//        //判断是否需要画to节点的余额线
//        GraphPointerEntity ownToOwnPointer = new GraphPointerEntity(to, to);
//        if (balance > 0) {
//            //看看剩下多少余额
//            GraphEdgeEntity ownToOwnEdge = edgesMap.get(ownToOwnPointer);
//            if (ownToOwnEdge == null) {
//                ownToOwnEdge = new GraphEdgeEntity();
//                ownToOwnEdge.setSource(to.getId());
//                ownToOwnEdge.setTarget(to.getId());
//                ownToOwnEdge.setLabel(String.valueOf(balance));
//                ownToOwnEdge.setType(GraphLineTypeEnum.LOOP.getType());
//                edgesMap.put(ownToOwnPointer, ownToOwnEdge);
//            } else {
//                //加上之前的余额
//                ownToOwnEdge.setLabel(String.valueOf(balance));
//            }
//        } else {
//            //如果余额小于等于0，把线去掉
//            edgesMap.remove(ownToOwnPointer);
//        }
//
//        Long fromBalance=nodeBalanceMap.get(from);
//        if(fromBalance!=null && fromBalance<=0){
//            //如果余额小于等于0，把线去掉
//            edgesMap.remove(new GraphPointerEntity(from,from));
//        }

    }

    /**
     * 将地址转化为对应的机构名称
     *
     * @param address
     * @return
     */
    private String transferAddressToName(String address) {
        //使用缓存
        if (kvTable.containsKey(address)) {
            return kvTable.get(address);
        }

        List<NodeQueryResult> list = new ArrayList<>();
        Properties props = System.getProperties(); //系统属性
        String ipList = (String) props.get("ipList");
        String[] strings = ipList.split(",");
        String port = "";
        //设置每个节点的名称以及对应的地址(对前端管理员而言相当于是id)
        for (String s : strings) {
            NodeInfo nodeInfo = new NodeInfo();
            nodeInfo.setId(s);
            String[] s1 = s.split(":");
            String nodeAddress = blockChainNode.getPeer().getAddressFromFile(s1[1]);
            if (address.equals(nodeAddress)) {
                NodeNameEnum nodeNameEnum = NodeNameEnum.getNodeNameEnumByPort(s1[1]);
                kvTable.put(address, nodeNameEnum.getName());
                return nodeNameEnum.getName();
            }
        }

        return "";
    }

    /**
     * 创建区块
     *
     * @param peer
     * @return
     */
    private Block createBlock(Peer peer) {
        //获取上一个区块
        Block preBlock = peer.getBlockChain().size() == 0 ? null :
                peer.getBlockChain().get(peer.getBlockChain().size() - 1);
        //如果当前节点是leader则直接创建新的区块，如果是其他节点则只需要广播交易过去，由leader节点去创建区块
        Collection<Transaction> collection = peer.getTransactionPool().values();
        List<Transaction> txs = new ArrayList<Transaction>(collection);
        Block block = peer.getBlockChainService().createCandidateBlock(txs, preBlock);
        return block;
    }

    /**
     * 构建资金流实体
     *
     * @param tx
     * @param block
     * @return
     */
    private FundFlowEntity buildFundFlowEntity(Transaction tx, Block block, int i) {
//        long sum = 0;
//        for (Vout vout : tx.getOutList()) {
//            sum += vout.getMoney();
//        }
        //获取输出金额，不包括找零
        long sum = tx.getOutputMoneyNoIncludeChange();

        //构建资金流信息
        FundFlowEntity fundFlowEntity = new FundFlowEntity();
        fundFlowEntity.setBlockHash(block.getHash());
        fundFlowEntity.setMoney(sum);

        fundFlowEntity.setProjectId(tx.getFormProjectId());
        fundFlowEntity.setProjectName(findProjectNameById(tx.getFormProjectId()));
        fundFlowEntity.setUserId(tx.getFromUserId());
        fundFlowEntity.setUserName(findUserNameById(tx.getFromUserId()));
        fundFlowEntity.setTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(tx.getLockTime()));
        String toAddress = tx.getOutList().get(0).getToAddress();
        fundFlowEntity.setTo(transferAddressToName(toAddress));
        fundFlowEntity.setTxId(tx.getId());
        fundFlowEntity.setBlockIndex(i);
        fundFlowEntity.setCoinBase(tx.isCoinBase());

        //如果是创币交易不需要设置输入地址
        if (!tx.isCoinBase()) {
            //同一个交易的所有vin地址都一样，所以选第一个就好
            String vinAddress = tx.getInList().get(0).getAddress();
            if (vinAddress != null) {
                fundFlowEntity.setFrom(transferAddressToName(vinAddress));
            }
        } else {
            fundFlowEntity.setFrom("用户: " + fundFlowEntity.getUserId());
        }

        return fundFlowEntity;
    }

    private String findUserNameById(String userId) {
        //使用缓存，减少数据库访问次数
        String res = "";
        if (kvTable.containsKey(userId)) {
            return kvTable.get(userId);
        } else {
            res = userMapper.selectUserById(userId).getUserName();
            kvTable.put(userId, res);
        }
        return res;
    }

    private String findProjectNameById(String projectId) {
        //使用缓存，减少数据库访问次数
        String res = "";
        if (kvTable.containsKey(projectId)) {
            return kvTable.get(projectId);
        } else {
            res = userMapper.readProjectDetail(projectId).getProjectName();
            kvTable.put(projectId, res);
        }
        return res;
    }

}
