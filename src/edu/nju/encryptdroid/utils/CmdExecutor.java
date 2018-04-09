package edu.nju.encryptdroid.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class CmdExecutor {
    /**
     * 需求：执行cmd命令，且输出信息到控制台
     * @param cmd
     */
    public static String execCmd(String cmd) {
        Logger.logInfo("ExecCmd:  " + cmd);
        try {
            Process p = Runtime.getRuntime().exec(cmd);
            //正确输出流
            InputStream input = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input));
            String line = "";
            String result = "";
            while ((line = reader.readLine()) != null) {
                result += line + "\n";
            }

            //错误输出流
            InputStream errorInput = p.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorInput));
            String eline = "";
            while ((eline = errorReader.readLine()) != null) {
                result += eline +"\n";
                Logger.logError(eline);
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
