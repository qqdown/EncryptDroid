package edu.nju.encryptdroid.utils;

import com.android.ddmlib.*;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 利用Adb的工具类
 * Created by ysht on 2016/3/7 0007.
 */
public class AdbTool {
    //用于同步
    private final static Object sSync = new Object();
    protected static boolean adbInitialized = false;
    protected static AndroidDebugBridge adb = null;

    protected IDevice device;

    protected AdbTool(){}

    /**
     * 初始化adb,可以重复初始化，函数会自动判断是否已经初始化
     * @return 初始化是否成功
     */
    public static boolean initializeBridge() {
        synchronized (sSync) {
            if (!adbInitialized) {
                try {
                    AndroidDebugBridge.init(false);
                    //AndroidDebugBridge.init(true);
                    adb = AndroidDebugBridge.createBridge(
                            Configuration.getADBPath(), true);
                    waitForInitialDeviceList();
                    adbInitialized = true;
                    Logger.logInfo("Init Bridge successfully!");
                } catch (Exception e) {
                    Logger.logException(e);
                }
            }
            return adbInitialized;
        }
    }

    /**
     * 终止adb
     */
    public static void terminateBridge()
    {
        if(!adbInitialized)
            return;

        synchronized (sSync) {
            AndroidDebugBridge.terminate();
            adbInitialized = false;
        }
    }

    //等待设备响应
    private static boolean waitForInitialDeviceList()
    {
        int count = 0;
        while (!adb.hasInitialDeviceList())
        {
            try
            {
                Thread.sleep(100);
                count++;
            }
            catch (InterruptedException e)
            {
                Logger.logException(e.getMessage());
                return false;
            }

            if (count > 100)
            {
                Logger.logError("获取设备超时");
                return false;
            }
        }
        return true;
    }

    /**
     * 获取默认（第一个）设备，如果无设备，返回null
     * @return 设备
     */
    public static IDevice getDefaultDevice(){
        assert (adbInitialized);
        synchronized (sSync) {
            IDevice[] recognizedDevices = adb.getDevices();
            if(recognizedDevices == null || recognizedDevices.length==0)
                return null;
            return recognizedDevices[0];
        }
    }

    /**
     * 获取设备列表
     * @return 设备列表
     */
    public static List<IDevice> getDevices(){
        assert (adbInitialized);
        synchronized (sSync) {
            return Arrays.asList(adb.getDevices());
        }
    }

    /**
     * 获取设备名列表
     * @return 设备名列表
     */
    public static List<String> getDeviceNames()
    {
        assert (adbInitialized);
        List<String> deviceNames = new ArrayList<String>();
        synchronized (sSync) {
            IDevice[] recognizedDevices = adb.getDevices();
            for (IDevice currDev : recognizedDevices) {
                if (currDev.isOnline()) {
                    deviceNames.add(currDev.getName());
                }
            }
            return deviceNames;
        }
    }

    /**
     * 获取指定的Device
     * @param deviceName 设备名
     * @return 设备
     */
    public static IDevice getIDevice(String deviceName) {
        assert (adbInitialized);
        //assert (!isDeviceBusy(deviceName));
        synchronized (sSync) {
            IDevice targetDevice = null;
            IDevice[] recognizedDevices = adb.getDevices();
            for (IDevice currDev : recognizedDevices) {
                if (currDev.isOnline()
                        && currDev.toString().equalsIgnoreCase(deviceName)) {
                    targetDevice = currDev;
                    break;
                }
            }
            return targetDevice;
        }
    }

    /**
     * 获取指定index的设备
     * @param deviceIndex 第deviceIndex个设备
     * @return 设备
     */
    public static IDevice getIDevice(int deviceIndex)
    {
        assert (adbInitialized);
        synchronized (sSync) {
            IDevice[] recognizedDevices = adb.getDevices();
            if(deviceIndex >= recognizedDevices.length || deviceIndex < 0)
                return null;
            return recognizedDevices[deviceIndex];
        }
    }

    /**
     * 向设备安装apk
     * @param deviceSerial 目标设备
     * @param apkFilePath apk文件路径
     * @return 是否安装成功
     */
    public static boolean installApk(String deviceSerial, String apkFilePath){
        File apkFile = new File(apkFilePath);
        if(!apkFile.exists())
            return false;
        String output = CmdExecutor.execCmd(Configuration.getADBPath() + " -s " + deviceSerial +  " install -r \"" + apkFile.getAbsolutePath() + "\"");
        return output.contains("Success");
    }

    public static String getPackageFromApk(String apkFilePath){
        File apkFile = new File(apkFilePath);
        if(!apkFile.exists())
            return null;
        String output = CmdExecutor.execCmd("\"" + Configuration.getAaptPath() + "\" dump badging \"" + apkFilePath + "\"");
        String[] lines = output.split("\n");
        for (String line : lines){
            if(line.startsWith("package:")){
                int l = line.indexOf("name='");
                String str = line.substring(l+6);
                int r = str.indexOf("'");
                str = str.substring(0, r);
                return str;
            }
        }
        return "";
    }

    public static String getLaunchableAcvivity(String apkFilePath){
        File apkFile = new File(apkFilePath);
        if(!apkFile.exists())
            return null;
        String output = CmdExecutor.execCmd("\"" + Configuration.getAaptPath() + "\" dump badging \"" + apkFilePath + "\"");
        String[] lines = output.split("\n");
        String activity = "";
        String packageName = "";
        for (String line : lines){
            if(line.startsWith("launchable-activity:")){
                int l = line.indexOf("name='");
                String str = line.substring(l+6);
                int r = str.indexOf("'");
                activity = str.substring(0, r);
            }
            else if(line.startsWith("package:")){
                int l = line.indexOf("name='");
                String str = line.substring(l+6);
                int r = str.indexOf("'");
                packageName = str.substring(0, r);
            }
        }
        return packageName + "/" + activity;
    }

    public static boolean hasInstalledPackage(IDevice device, String packageName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        boolean[] result = new boolean[1];
        result[0] = false;

        if(device != null) {
            device.executeShellCommand("pm list package | grep '" + packageName + "'", new IShellOutputReceiver() {
                @Override
                public void addOutput(byte[] bytes, int i, int i1) {
                    if(bytes.length > 0)
                        result[0] = true;
                    else
                        result[0] = false;
                }
                @Override
                public void flush() {}
                @Override
                public boolean isCancelled() {return false;}
            });
        }
        return result[0];
    }


    /**
     * 获得当前Activity名
     * @param device 目标设备
     * @return activity名
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static String getFocusedActivity(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        final String[] result = new String[1];
        result[0] = null;
        if(device != null) {
            device.executeShellCommand("dumpsys activity | grep mFocusedActivity", new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    int i1, i2;
                    i1 = output.indexOf('{');
                    i2 = output.indexOf('}');
                    if (i1 < 0 || i2 < 0)
                        return;
                    output = output.substring(i1 + 1, i2);
                    result[0] = output.split(" ")[2];
                }
            });
        }
        else
            Logger.logError("设备为空！");
        return result[0];
    }

    /**
     * 启动程序
     * @param device 目标设备
     * @param activityName 完整的activity名，格式为packageName/.activityName
     * @return 是否成功启动
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static boolean startActivity(IDevice device, String activityName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException{
        boolean[] result = new boolean[1];
        result[0] = false;
        if(device != null) {
            device.executeShellCommand("am start -n " + activityName, new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    if(output.contains("Error"))
                    {
                        result[0] = false;
                        Logger.logError("startActivity " +  activityName + ": " + device.getName() + " " + output);
                    }
                    else
                        result[0] = true;
                }
            });
        }
        else
            Logger.logError("设备为空！");
        return result[0];
    }

    /**
     * 停止应用
     * @param device 目标设备
     * @param packageName 应用包名
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static void stopApplication(IDevice device, String packageName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException{
        if(device != null) {
            device.executeShellCommand("am force-stop " + packageName, new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    Logger.logInfo("stop application: " + output);
                }
            });
        }
        else
            Logger.logError("设备为空！");
    }

    /**
     * 获取当前正在运行的Activity，返回包含Activity名的List，顺序为运行栈顶-》栈底
     * @param device 目标设备
     * @return 包含Activity名的List
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static List<String>  getRunningActivities(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        final List<String> result = new ArrayList<String>();
        if(device != null) {
            device.executeShellCommand("dumpsys activity | grep 'Run #'", new IShellOutputReceiver() {
                @Override
                public boolean isCancelled() {return false;}
                @Override
                public void flush() {}
                @Override
                public void addOutput(byte[] arg0, int arg1, int arg2) {
                    String output = new String(arg0);
                    String[] lines = output.split("\n");

                    for(String line : lines) {
                        if (line.isEmpty())
                            continue;
                        int l, r;
                        l = line.indexOf("{");
                        r = line.indexOf("}");
                        if (l < 0 || r < 0)
                            continue;
                        result.add(line.substring(l + 1, r).split(" ")[2]);
                    }
                }
            });
        }
        else
            Logger.logError("设备为空！");
        return result;
    }

    /**
     * 获取当前Task的id
     * @param device 目标设备
     * @return Task id
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static int getFocusedTaskId(IDevice device) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException{
        final int[] id = new int[1];
        id[0] = -1;
        if(device == null)
        {
            Logger.logError("设备为空！");
            return id[0];
        }
        String focusedPackage = getFocusedActivity(device).split("/")[0];

        device.executeShellCommand("ps | grep \"" + focusedPackage + "\"", new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {
                String output = new String(arg0);
                int l = output.indexOf(' ');
                for(; l<output.length(); l++){
                    if(output.charAt(l) != ' ')
                        break;
                }
                String idStr = output.substring(l).split(" ")[0];
                id[0] = Integer.parseInt(idStr);
            }
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
        return id[0];
    }

    public static int getTaskId(IDevice device, String taskName) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {

        if(device == null)
        {
            Logger.logError("设备为空！");
            return -1;
        }
        int[] id = new int[1];
        id[0] = -1;
        device.executeShellCommand("ps | grep '"+ taskName + "'", new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {
                String output = new String(arg0);
                for(String line : output.split("\n")){
                    String[] params = line.split(" +");
                    if(params.length == 9 && params[8].contains(taskName)){
                        id[0] = Integer.parseInt(params[1]);
                        return;
                    }
                }
            }
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
        return id[0];
    }

    public static void killTask(IDevice device, int taskId) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }
        device.executeShellCommand("kill " + taskId, new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
        return;
    }


    public static void doPress(IDevice device, int x, int y) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }

        device.executeShellCommand(String.format("input tap %d %d", x,y), new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
    }

    /**
     * 长按操作
     * @param device 设备
     * @param x 坐标x
     * @param y 坐标y
     * @param duration 长按时间（毫秒），默认建议设置为1000
     * @throws TimeoutException
     * @throws AdbCommandRejectedException
     * @throws ShellCommandUnresponsiveException
     * @throws IOException
     */
    public static void doLongPress(IDevice device, int x, int y, int duration) throws TimeoutException, AdbCommandRejectedException, ShellCommandUnresponsiveException, IOException {
        if(device == null)
        {
            Logger.logError("设备为空！");
            return;
        }

        device.executeShellCommand(String.format("input swipe %d %d %d %d %d", x,y,x,y,duration), new  IShellOutputReceiver() {
            @Override
            public void addOutput(byte[] arg0, int arg1, int arg2) {}
            @Override
            public void flush() {}
            @Override
            public boolean isCancelled() {return false;}
        });
    }

    public static boolean areActivityNameSame(String activity1, String activity2){
        String[] ac1 = activity1.split("/");
        String[] ac2 = activity2.split("/");
        if(ac1.length != ac2.length)
            return false;
        if(ac1.length == 1)
            return activity1.equals(activity2);
        else if(ac1.length == 2 && ac1[0].equals(ac2[0])){//package名相同
            return ac1[1].endsWith(ac2[1]) || ac2[1].endsWith(ac1[1]);
        }
        return false;
    }


    //获取str开头的空格个数
    private static int getPrefixSpaceLength(String str){
        int i;
        for(i=0; i<str.length(); i++){
            if(str.charAt(i) != ' ')
                break;
        }
        return i;
    }
}
