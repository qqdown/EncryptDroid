package edu.nju.encryptdroid;

import brut.androlib.AndrolibException;
import brut.androlib.ApkDecoder;
import brut.androlib.err.CantFindFrameworkResException;
import brut.androlib.err.InFileNotFoundException;
import brut.directory.DirectoryException;
import com.sun.corba.se.impl.javax.rmi.CORBA.Util;
import edu.nju.encryptdroid.utils.AdbTool;
import edu.nju.encryptdroid.utils.CmdExecutor;
import edu.nju.encryptdroid.utils.Configuration;
import edu.nju.encryptdroid.utils.Utils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;

/**
 * Created by ysht on 2016/4/9 0009.
 */
public class EncryptedApk {
    public final String androidJar = Configuration.getAndroidJarPath();
    public final String aapt = Configuration.getAaptPath();
    public final String javac = Configuration.getJavacPath();
    public final String android = Configuration.getAndroidPath();

    private File apkFile;
    private String packageName = null;
    private String appName = null;
    private String androidId = "26";

    public EncryptedApk(String apkPath, String androidId) throws Exception {
        this.androidId = androidId;
        apkFile = new File(apkPath);
        if(!apkFile.exists()){
            throw new FileNotFoundException("Apk file " + apkPath + " does not exist!");
        }
        packageName = AdbTool.getPackageFromApk(apkFile.getAbsolutePath());
        if(packageName==null || packageName.isEmpty()){
            throw new Exception(String.format("Can not get the Package information from %s！", apkPath));
        }
        appName = packageName.replace('.', '_');
    }

    public void encryptToNewApk(String outputFile){
        File extractedFile = extractApk();
        File projectFolder = createProject(extractedFile);
        File releaseFile = new File(projectFolder.getAbsolutePath()+"/bin/"+appName+"-release.apk");
        Utils.copyFile(releaseFile.getAbsolutePath(), outputFile);
    }

    public File extractApk() {
        try {
            //将原来的工程解压
            File extractOutputFile = new File("temp/extracted/" + apkFile.getName());
            ApkDecoder decoder = new ApkDecoder();
            decoder.setApkFile(apkFile);
            decoder.setDecodeSources((short)0);
            decoder.setForceDelete(true);
            decoder.setOutDir(extractOutputFile);
            decoder.decode();
            return extractOutputFile;
        }  catch (InFileNotFoundException var7) {
            System.err.println("Input file (" + apkFile + ") " + "was not found or was not readable.");
            System.exit(1);
        } catch (CantFindFrameworkResException var8) {
            System.err.println("Can\'t find framework resources for package of id: " + String.valueOf(var8.getPkgId()) + ". You must install proper " + "framework files, see project website for more info.");
            System.exit(1);
        } catch (IOException var9) {
            System.err.println("Could not modify file. Please ensure you have permission.");
            System.exit(1);
        } catch (DirectoryException var10) {
            System.err.println("Could not modify internal dex files. Please ensure you have permission.");
            System.exit(1);
        } catch (AndrolibException e) {
            e.printStackTrace();
        }
        return null;
    }

    private File createProject(File extractedApkFile){
        File projectFolder = new File("temp/projects/" + apkFile.getName());
        if(projectFolder.exists())
        {
            Utils.deleteDirectory(projectFolder);
            System.out.println("delete projectFolder");
        }
        projectFolder.mkdirs();
        CmdExecutor.execCmd(String.format("\"%s\" create project --target %s --name %s --path %s --activity DiscoMainActivity --package %s",
                android, androidId, appName, projectFolder.getAbsolutePath(), packageName));

        File assetsFolder = new File(projectFolder.getPath() + "/assets");
        if(!assetsFolder.exists())
            assetsFolder.mkdirs();
        Utils.copyFolder(extractedApkFile.getAbsolutePath()+"/assets", assetsFolder.getAbsolutePath());
        File srcFolder = new File(projectFolder.getPath() + "/src");
        File resFolder = new File(projectFolder.getPath() + "/res");
        Utils.deleteDirectory(resFolder);
        resFolder.mkdirs();

        Utils.copyFolder(extractedApkFile.getAbsolutePath() + "/res",  resFolder.getAbsolutePath());
        File manifestFile = new File( projectFolder.getAbsolutePath() + "/AndroidManifest.xml");
        Utils.copyFile(extractedApkFile.getAbsolutePath()+"/AndroidManifest.xml", manifestFile.getAbsolutePath());
        Utils.copyFile(apkFile.getAbsolutePath(), assetsFolder.getAbsolutePath()+"/apks/enc.apk");

        String actionName = rewriteManifest(manifestFile);//在这个函数中，packagename将被获取得到
        File packageFolder = new File(srcFolder.getAbsolutePath()+"/"+packageName.replace('.','/'));
        packageFolder.mkdirs();

        Utils.copyFile("tools/ks_1234asdf", projectFolder.getAbsolutePath()+"/ks_1234asdf");
        createDiscoActivity(packageFolder, actionName);
        createSmith(packageFolder);

        File antPropertiesFile = new File(projectFolder.getAbsolutePath() + "/ant.properties");
        changeAntProperties(antPropertiesFile);

        String cmd = String.format("\"%s\" -f %s release", Configuration.getAntPath(), projectFolder.getAbsolutePath()+"/build.xml");
        try {
            Process p = Runtime.getRuntime().exec(cmd);

            //正确输出流
            BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

            InputStream input = p.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    input));
            String line = "";
            while ((line = reader.readLine()) != null) {
                if(line.contains("[input] Please enter keystore password")) {
                    bw.write("1234asdf\n");
                    bw.flush();

                }
                else if(line.contains("[input] Please enter password for alia")){
                    bw.write("1234asdf\n");
                    bw.flush();
                }
                System.out.println(line);
            }

            //错误输出流
            InputStream errorInput = p.getErrorStream();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(errorInput));
            String eline = "";
            while ((eline = errorReader.readLine()) != null) {
                System.out.println(eline);
            }

        } catch (IOException e) {
            e.printStackTrace();

        }
        return projectFolder;
        /*
        //生成gen/R文件
        File genFile = new File(projectFolder.getAbsolutePath()+"/gen");
        genFile.mkdirs();
        CmdExecutor.execCmd("\"" + aapt + "\" package -f -m -J " + genFile.getAbsolutePath() + " -S " + resFolder.getAbsolutePath() + " -M " + manifestFile.getAbsolutePath() + " -I \"" + androidJar + "\"");

        //生成bin文件
        File binFile = new File(projectFolder.getAbsolutePath() +"/bin");
        binFile.mkdirs();
        CmdExecutor.execCmd("\"" + javac +  "\" -target 1.7 -bootclasspath \"" + androidJar+"\"" + " -d " + binFile.getAbsolutePath() + " " + packageFolder+"/*.java " + genFile.getAbsolutePath() + "/" +packageName.replace('.','/')+"/R.java");

        //生成dex
        com.android.dx.command.Main.main(new String[]{"--dex","--output", binFile.getAbsolutePath()+"/classes.dex", binFile.getAbsolutePath()});*/
    }

    private String rewriteManifest(File manifestFile){
        try {
            String mainActivity = null;
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document document = db.parse(manifestFile);

            String actionName = findAndChangeMainActivity(document);

            Source xmlSource = new DOMSource(document);
            TransformerFactory factory = TransformerFactory.newInstance();
            Transformer transformer = factory.newTransformer();
            Result result = new StreamResult(manifestFile);
            transformer.transform(xmlSource, result);
            return actionName;
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    private String findAndChangeMainActivity(Document document){
        NodeList roots = document.getChildNodes();
        for(int i=0; i<roots.getLength(); i++){
            Node node = roots.item(i);
            if(node.getNodeName().equals("manifest")){
                //packageName = node.getAttributes().getNamedItem("package").getNodeValue();
                //get main activity
                NodeList manifestChildren = node.getChildNodes();
                for(int j=0; j<manifestChildren.getLength(); j++){
                    Node mnode = manifestChildren.item(j);
                    //find application
                    if(mnode.getNodeName().equals("application")){
                        if(mnode.getAttributes().getNamedItem("android:name") != null)//删除applicaion中的android:name属性
                            mnode.getAttributes().removeNamedItem("android:name");
                        NodeList applicationChildren = mnode.getChildNodes();
                        //find main activity
                        for(int k=0; k<applicationChildren.getLength(); k++){
                            Node activityNode = applicationChildren.item(k);
                            if(activityNode.getNodeName().equals("activity")) {
                                if(activityNode.hasChildNodes()){
                                    NodeList activityChildren = activityNode.getChildNodes();
                                    for(int l=0; l<activityChildren.getLength(); l++){
                                        Node intentFilter = activityChildren.item(l);
                                        if(intentFilter.getNodeName().equals("intent-filter")) {
                                            for(int h=0; h<intentFilter.getChildNodes().getLength(); h++){
                                                Node actionNode = intentFilter.getChildNodes().item(h);
                                                if(actionNode.getNodeName().equals("action") && actionNode.hasAttributes()){
                                                    String androidName = actionNode.getAttributes().getNamedItem("android:name").getNodeValue();
                                                    if(androidName.equals("android.intent.action.MAIN")){
                                                        String mainActivity = activityNode.getAttributes().getNamedItem("android:name").getNodeValue();
                                                        activityNode.getAttributes().getNamedItem("android:name").setNodeValue(".DiscoMainActivity");
                                                        String actionName;
                                                        if(mainActivity.startsWith("."))
                                                            actionName=packageName+".intent.action"+mainActivity;
                                                        else
                                                            actionName = packageName+".intent.action."+mainActivity;
                                                        mnode.insertBefore(createNewActivityNode(document, mainActivity, actionName), activityNode);
                                                        System.out.println("get mainactivity " + mainActivity);
                                                        return actionName;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        break;
                    }
                }
                break;
            }
        }
        return null;
    }

    private Node createNewActivityNode(Document document, String activityName, String actionName){
        Element activityNode = document.createElement("activity");
        activityNode.setAttribute("android:launchMode","singleTask");
        activityNode.setAttribute("android:name",activityName);
        activityNode.setAttribute("android:screenOrientation","user");
        activityNode.setAttribute("android:taskAffinity",packageName);

        Element intentFilterNode = document.createElement("intent-filter");
        Element actionNode= document.createElement("action");
        actionNode.setAttribute("android:name", actionName);

        intentFilterNode.appendChild(actionNode);
        Element categoryNode = document.createElement("category");
        categoryNode.setAttribute("android:name","android.intent.category.DEFAULT");
        intentFilterNode.appendChild(categoryNode);

        activityNode.appendChild(intentFilterNode);
        return activityNode;
    }

    private void createDiscoActivity(File packageFile, String actionName){

        try {
            File discoMainActivityFile = new File("tools/src/DiscoMainActivity.java");
            FileReader fr = new FileReader(discoMainActivityFile);
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter(packageFile.getAbsolutePath() + "/" + discoMainActivityFile.getName());
            BufferedWriter bw = new BufferedWriter(fw);
            String str;
            while((str = br.readLine()) != null){
                bw.write(str.replace("targetpackagename", packageName).replace("targetactionname", actionName));
                bw.newLine();
            }
            bw.close();
            fw.close();
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void createSmith(File packageFile){

        try {
            File smith = new File("tools/src/Smith.java");
            FileReader fr = new FileReader(smith);
            BufferedReader br = new BufferedReader(fr);
            FileWriter fw = new FileWriter(packageFile.getAbsolutePath() + "/" + smith.getName());
            BufferedWriter bw = new BufferedWriter(fw);
            String str;
            while((str = br.readLine()) != null){
                bw.write(str.replace("targetpackagename", packageName));
                bw.newLine();
            }
            bw.close();
            fw.close();
            br.close();
            fr.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void changeAntProperties(File antPropertiesFile){
        try{
            FileWriter fw = new FileWriter(antPropertiesFile);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write("key.store=ks_1234asdf");
            bw.newLine();
            bw.write("key.alias=1234asdf");
            bw.newLine();
            bw.close();
            fw.close();
        }
        catch (IOException e){
            e.printStackTrace();
        }
    }
}
