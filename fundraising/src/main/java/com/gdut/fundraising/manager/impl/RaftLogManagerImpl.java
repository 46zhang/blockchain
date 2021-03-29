package com.gdut.fundraising.manager.impl;

import com.alibaba.fastjson.JSON;
import com.gdut.fundraising.constant.LogConstance;
import com.gdut.fundraising.entities.raft.LogEntry;
import com.gdut.fundraising.manager.RaftLogManager;
import com.gdut.fundraising.util.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

/**
 * 日志管理器
 */
public class RaftLogManagerImpl implements RaftLogManager {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftLogManagerImpl.class);
    /**
     * 路径名
     */
    private String pathName;

    /**
     * 日志列表
     */
    LinkedList<LogEntry> logEntries;

    public RaftLogManagerImpl() {
        Properties props = System.getProperties(); //系统属性
        String port = (String) props.get("port");
        //根据不同系统构建文件路径
        pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port, LogConstance.BLOCK_CHAIN_PATH);
        logEntries = new LinkedList<>();
        //创建文件夹，该函数有做幂等操作，如果文件夹已存在则直接返回true
        FileUtils.createDir(pathName);
        loadAllLog();
        LOGGER.info("log manager init success!!");
    }

    /**
     * 加载日志
     */
    private void loadAllLog() {
        String[] fileNames = FileUtils.getAllFileNameInDir(pathName);

        for (int i = 0; i < fileNames.length; ++i) {
            String data = FileUtils.read(FileUtils.buildPath(pathName, String.valueOf(i+1) + ".json"));
            if (data != null) {
                LogEntry entry = JSON.parseObject(data, LogEntry.class);
                logEntries.add(entry);
            }
        }
    }

    /**
     * 获取日志索引
     *
     * @return
     */
    @Override
    public long getLastLogIndex() {
        if (logEntries.size() == 0) {
            return 0;
        }
        LogEntry logEntry = logEntries.get(logEntries.size() - 1);
        return logEntry.getIndex();
    }

    /**
     * 获取最后一个日志
     *
     * @return
     */
    @Override
    public LogEntry getLastLogEntry() {
        if (null == logEntries || logEntries.size() == 0) {
            return null;
        }
        LogEntry logEntry = logEntries.get(logEntries.size() - 1);
        return logEntry;
    }

    /**
     * 获取最后日志的任期
     *
     * @return
     */
    @Override
    public long getLsatLogTerm() {
        if (logEntries.size() == 0) {
            return 0;
        }
        LogEntry logEntry = logEntries.get(logEntries.size() - 1);
        return logEntry.getTerm();
    }

    /**
     * 读取文件
     *
     * @param index
     * @return
     */
    @Override
    public LogEntry read(long index) {
        if (index <= logEntries.size()) {
            //LOGGER.info("log manager read index:{},size:{}",index,logEntries.size());
            return logEntries.get((int) index - 1);
        }
        return null;
    }

    /**
     * 从index开始删除日志
     *
     * @param l
     */
    @Override
    public void removeOnStartIndex(long l) {
        if (l <= logEntries.size()) {
            while (logEntries.size() >= l) {
                FileUtils.deleteFile(FileUtils.buildPath(pathName,
                        String.valueOf(logEntries.getLast().getIndex()) + ".json"));
                logEntries.removeLast();
            }

        }

    }

    /**
     * 写入数据
     *
     * @param entry
     * @return
     */
    @Override
    public boolean write(LogEntry entry) {
        String jsonStr = JSON.toJSONString(entry);
        boolean res = FileUtils.write(FileUtils.buildPath(pathName, String.valueOf(entry.getIndex()) + ".json"), jsonStr);
        if (res) {
            logEntries.add(entry);
        }
        return res;
    }
}
