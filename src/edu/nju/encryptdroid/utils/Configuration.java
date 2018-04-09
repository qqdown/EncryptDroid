package edu.nju.encryptdroid.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

/**
 * Created by ysht on 2016/3/7 0007.
 */
public class Configuration {
    private static String propPath = "autodroid.properties";
    private static Properties prop;

    static {
        prop = new Properties();
        try{
            InputStream in = new FileInputStream(propPath);
            prop.load(in);
            in.close();
            Logger.logInfo("Read configuraion successfully！");
        }
        catch (Exception e){
            Logger.logException("Fail to read configuraion！\n"+ new File(propPath).getAbsolutePath());
            Logger.logException(e);
        }
    }


    protected static String getProperty(String key)
    {
        return prop.getProperty(key);
    }

    protected static String getAndroidSDKPath()
    {
        return prop.getProperty("android_sdk_path");
    }

    protected static String getAntRootPath(){
        return prop.getProperty("ant_root_path");
    }

    public static String getADBPath()
    {
        if(System.getProperty("os.name").contains("Linux"))
            return getAndroidSDKPath() + "/platform-tools/adb";
        return getAndroidSDKPath() + "/platform-tools/adb.exe";
    }

    public static String getAaptPath() {
        return getProperty("aapt_path");
    }

    public static String getAndroidPath()
    {
        if(System.getProperty("os.name").contains("Linux"))
            return getAndroidSDKPath() + "/tools/android";
        return getAndroidSDKPath() + "/tools/android.bat";
    }

    public static String getAntPath(){
        if(System.getProperty("os.name").contains("Linux"))
            return getAntRootPath() + "/bin/ant";
        return getAntRootPath() + "/bin/ant.bat";
    }

    public static  String getWorkspacePath(){
        File directory = new File("");
        return directory.getAbsolutePath();
    }

    public static String getApktoolPath(){
        return prop.getProperty("apktool_path");
    }

    public static String getAndroidJarPath(){
        return prop.getProperty("android_jar_path");
    }

    public static String getDxPath(){
        return prop.getProperty("dx_path");
    }

    public static String getJavacPath(){
        return prop.getProperty("javac_path");
    }
}
