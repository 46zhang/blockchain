package com.gdut.fundraising.service;

import com.gdut.fundraising.dto.raft.Request;
import com.gdut.fundraising.util.JsonResult;

public interface NetworkService {
    public <R extends Request> JsonResult post(String ip, String port, R data);

}
