package com.gdut.fundraising.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class RaftThread extends Thread {

    private static final Logger LOGGER = LoggerFactory.getLogger(RaftThread.class);
    private static final UncaughtExceptionHandler UNCAUGHT_EXCEPTION_HANDLER = (t, e)
            -> LOGGER.error("Exception occurred from thread {}", t.getName(), e);

    public RaftThread(String threadName, Runnable r) {
        super(r, threadName);
        setUncaughtExceptionHandler(UNCAUGHT_EXCEPTION_HANDLER);
    }

}
