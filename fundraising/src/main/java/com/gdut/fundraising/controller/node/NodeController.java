package com.gdut.fundraising.controller.node;

import com.gdut.fundraising.blockchain.Block;
import com.gdut.fundraising.blockchain.Transaction;
import com.gdut.fundraising.dto.raft.AppendLogRequest;
import com.gdut.fundraising.dto.raft.VoteRequest;
import com.gdut.fundraising.entities.raft.BlockChainNode;
import com.gdut.fundraising.service.BCTService;
import com.gdut.fundraising.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


import java.util.List;
import java.util.Map;

@ControllerAdvice
@RestController
@RequestMapping("/node")
public class NodeController {
    @Autowired
    BlockChainNode node;

    @Autowired
    BCTService BCTService;

    /**
     * 添加日志
     * @param request
     * @return
     */
    @PostMapping("/appendlog")
    public Map appendLog(@RequestBody AppendLogRequest request){
        return JsonResult.success(node.getRaftConsensusManager().appendLog(request,node)).result();
    }

    /**
     * 投票
     * @param request
     * @return
     */
    @PostMapping("/vote")
    public Map vote(@RequestBody VoteRequest request){
        return JsonResult.success(node.getRaftConsensusManager().dealVoteRequest(request,node)).result();
    }

    /**
     * 同步数据给其他节点
     * @param data
     * @return
     */
    @PostMapping("/send")
    public Map send(@RequestBody Block data){
        return JsonResult.success(node.sendLogToOtherNodeForConsistency(data)).result();
    }


    /**
     * leader节点接受其他节点
     * @param data
     * @return
     */
    @PostMapping("/broadcast")
    public Map consistence(@RequestBody List<Transaction> data){
        return JsonResult.success(BCTService.verifyTransactionsFromOtherNode(data)).result();
    }
}
