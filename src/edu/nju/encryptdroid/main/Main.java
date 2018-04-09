package edu.nju.encryptdroid.main;

import edu.nju.encryptdroid.EncryptedApk;
import edu.nju.encryptdroid.utils.Configuration;
import edu.nju.encryptdroid.utils.Utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;

/**
 * Created by ysht on 2016/4/9 0009.
 */
public class Main {
    public static void main(String[] args) throws Exception {
        if(args.length != 3){
            System.out.println("Parameters error！");
            printHelp();
            return;
        }
        String androidId = args[2];
        File inputFile = new File(args[0]);
        if(inputFile.isFile()){
            EncryptedApk apk = new EncryptedApk(args[0], androidId);
            apk.encryptToNewApk(args[1]);
        }
        else{
            File outputFile = new File(args[1]);
            if(!outputFile.exists()){
                outputFile.mkdirs();
            }
            if(!outputFile.isDirectory())
            {
                System.out.println("EncryptedApkPath need to be a directory");
                printHelp();
                return;
            }

            File[] apkFiles = inputFile.listFiles(new FileFilter() {
                @Override
                public boolean accept(File pathname) {
                    if(pathname.getName().endsWith(".apk"))
                        return true;
                    return false;
                }
            });
            for(File apk : apkFiles){
                System.out.print("Begin encrypt：" + apk.getAbsolutePath());
                try{
                    EncryptedApk eapk = new EncryptedApk(apk.getAbsolutePath(), androidId);
                    eapk.encryptToNewApk(outputFile.getAbsolutePath() + "/" + apk.getName());
                }
                catch (Exception e){
                    e.printStackTrace();
                }

                System.out.println("End cncryption");
            }
        }

    }

    public static void printHelp(){
        System.out.println("Useage：java -jar EncryptDroid.jar [OriginalApkPath] [EncryptedApkPath] [AndroidId]\n" +
                "If [OriginalApkPath] is a directory， all apks in this directory will be encrypted. [EncryptedApkPath] must be a directory as well.\n" +
                "If [OriginalApkPath] is an apk file path，only this apk will be encrypted and saved to [EncryptedApkPath] file.\n" +
                "[AndroidId] means the id of Android platform version. Use command 'android list' to view your ids in your system.");
    }


}
