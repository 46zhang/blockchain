package com.gdut.fundraising.controller.node;

import com.gdut.fundraising.dto.raft.AppendLogRequest;
import com.gdut.fundraising.dto.raft.VoteRequest;
import com.gdut.fundraising.entities.raft.BlockChainNode;
import com.gdut.fundraising.util.JsonResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@ControllerAdvice
@RestController
@RequestMapping("/node")
public class NodeController {
    @Autowired
    BlockChainNode node;

    @PostMapping("/appendlog")
    public Map appendLog(@RequestBody AppendLogRequest request){
        return JsonResult.success(node.getRaftConsensusManager().appendLog(request,node)).result();
    }

    @PostMapping("/vote")
    public Map vote(@RequestBody VoteRequest request){
        return JsonResult.success(node.getRaftConsensusManager().dealVoteRequest(request,node)).result();
    }

}
