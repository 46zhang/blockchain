package com.gdut.fundraising.manager.impl;

import com.alibaba.fastjson.JSON;
import com.gdut.fundraising.entities.raft.LogEntry;
import com.gdut.fundraising.manager.RaftLogManager;
import com.gdut.fundraising.util.FileUtils;

import java.util.List;
import java.util.Properties;

/**
 * 日志管理器
 */
public class RaftLogManagerImpl implements RaftLogManager {

    /**
     * 路径名
     */
    private String pathName;

    /**
     * 日志列表
     */
    private List<LogEntry> logEntries;

    public RaftLogManagerImpl() {
        Properties props = System.getProperties(); //系统属性
        String port = (String) props.get("port");
        //根据不同系统构建文件路径
        pathName = FileUtils.buildPath(FileUtils.getRootFilePath(), port);
        //创建文件夹，该函数有做幂等操作，如果文件夹已存在则直接返回true
        FileUtils.createDir(pathName);
        loadAllLog();
    }

    /**
     * 加载日志
     */
    private void loadAllLog() {
        String[] fileNames = FileUtils.getAllFileNameInDir(pathName);
        for (String file : fileNames) {
            String data = FileUtils.read(FileUtils.buildPath(pathName, file));
            if (data != null) {
                LogEntry entry = JSON.parseObject(data, LogEntry.class);
                logEntries.add(entry);
            }
        }
    }

    /**
     * 获取日志索引
     * @return
     */
    @Override
    public long getLastLogIndex() {
        LogEntry logEntry = logEntries.get(logEntries.size() - 1);
        return logEntry.getIndex();
    }

    /**
     * 获取最后一个日志
     * @return
     */
    @Override
    public LogEntry getLastLogEntry() {
        LogEntry logEntry = logEntries.get(logEntries.size() - 1);
        return logEntry;
    }

    /**
     * 获取最后日志的任期
     * @return
     */
    @Override
    public long getLsatLogTerm() {
        LogEntry logEntry = logEntries.get(logEntries.size() - 1);
        return logEntry.getTerm();
    }

    /**
     * 读取文件
     * @param index
     * @return
     */
    @Override
    public LogEntry read(long index) {
        if (index < logEntries.size()) {
            return logEntries.get((int) index);
        }
        return null;
    }

    /**
     * 从index开始删除日志
     * @param l
     */
    @Override
    public void removeOnStartIndex(long l) {
        if(l<logEntries.size()){
            //TODO long 直接转int可能存在数据溢出风险
           for(int i = (int) l; i<logEntries.size(); ++i){
               logEntries.remove(i);
               FileUtils.deleteFile(FileUtils.buildPath(pathName, String.valueOf(i) + ".json"));
           }
        }

    }

    /**
     * 写入数据
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
