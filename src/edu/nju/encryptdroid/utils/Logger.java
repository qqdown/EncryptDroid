package edu.nju.encryptdroid.utils;

import java.io.File;
import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 用于系统的日志记录
 * Created by ysht on 2016/3/7 0007.
 */
public class Logger {
    private static PrintStream logobject = System.out;
    private static boolean isInitialized = false;
    private static final String infoPrefix = "INFO:";
    private static final String errorPrefix = "ERROR:";
    private static final String exceptionPrefix = "EXCEPTION:";
    private static final String warningPrefix = "WARNING:";
    private static final SimpleDateFormat DateFORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    /**
     * 初始化logger，将log信息保存在logFileName中，如果logFile为null，则不写入文件
     * @param logFileName 日志文件名
     */
    public static final synchronized void initalize(String logFileName) {
        if (!isInitialized) {
            if (logFileName != null) {
                try {
                    logobject = new PrintStream(new File(logFileName));
                    isInitialized = true;
                } catch (Exception e) {
                    logobject = System.out;
                }
            } else{
                logobject = System.out;
            }
        }
    }

    /**
     * 结束日志，保证日志文件可以被正确的关闭和保存
     */
    public static final synchronized void endLogging() {
        if (logobject != System.out && logobject != null) {
            try {
                logobject.close();
            } catch (Exception e) {

            }
        }
        isInitialized = false;
    }

    /***
     * This is the method that is used to log the provided msg as info
     * @param msg target message that needs to be logged
     */
    public static final void logInfo(String msg) {
        writeMsg(infoPrefix + msg);
    }

    /***
     * Use this to log error message
     * @param msg target message that needs to be logged
     */
    public static final void logError(String msg) {
        writeMsg(errorPrefix + msg);
    }

    /***
     * Use this to log exception
     * @param msg target Message
     */
    public static final void logException(String msg) {
        writeMsg(exceptionPrefix + msg);
    }

    /**
     * This method is to log the provided exception
     * @param e the target exception that needs to be logged
     */
    public static final synchronized void logException(Exception e) {
        if (!isInitialized) {
            System.out.println(exceptionPrefix + " Stack Trace:");
            e.printStackTrace(System.out);
            System.out.println(e.getMessage());
            System.out.flush();
        } else {
            e.printStackTrace(System.out);
            e.printStackTrace(logobject);
            logobject.println(DateFORMAT.format(new Date()) + "\t" + e.getMessage());
            logobject.flush();
        }
    }

    private static final synchronized void writeMsg(String msg) {
        msg = DateFORMAT.format(new Date()) + "\t" + msg;
        if (!isInitialized) {
            System.out.printf("%s\n", msg);
            System.out.flush();
        } else {
            System.out.printf("%s\n", msg);
            System.out.flush();
            logobject.printf("%s\n", msg);
            logobject.flush();
        }
    }
}
