package com.gdut.fundraising.service.impl;


import com.alibaba.fastjson.JSON;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdut.fundraising.dto.raft.Request;
import com.gdut.fundraising.dto.raft.VoteRequest;
import com.gdut.fundraising.dto.raft.VoteResult;
import com.gdut.fundraising.exception.BaseException;
import com.gdut.fundraising.service.NetworkService;
import com.gdut.fundraising.util.JsonResult;
import com.gdut.fundraising.util.NetworkUtils;


public class NetworkServiceImpl implements NetworkService {

    @Override
    public <R extends Request> JsonResult post(String ip, String port, R data) {
        String result = NetworkUtils.postByHttp(NetworkUtils.buildUrl(ip, port, data), data);

        JsonResult jsonResult = JSON.parseObject(result, JsonResult.class);

        if (jsonResult.getCode() != 100) {
            throw new BaseException(jsonResult.getCode(), jsonResult.getMsg());
        }
        return jsonResult;
    }

    @Override
    public <R> JsonResult post(String url, R data) {
        String result = NetworkUtils.postByHttp(url, data);

        JsonResult jsonResult = JSON.parseObject(result, JsonResult.class);

        if (jsonResult.getCode() != 100) {
            throw new BaseException(jsonResult.getCode(), jsonResult.getMsg());
        }
        return jsonResult;
    }

    public static void main(String[] args) {
        NetworkService networkService = new NetworkServiceImpl();
        VoteRequest voteRequest = new VoteRequest();
        voteRequest.setCandidateId("localhost:8091");
        voteRequest.setLastLogIndex(0);
        voteRequest.setLastLogTerm(0);
        voteRequest.setTerm(100);
        try {
            JsonResult result = networkService.post("localhost", "8092", voteRequest);
            System.out.println(result.getData());
            ObjectMapper mapper = new ObjectMapper();

            VoteResult voteResult = mapper.convertValue(result.getData(), VoteResult.class);
            System.out.println(voteResult.getTerm());
            System.out.println(result);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
