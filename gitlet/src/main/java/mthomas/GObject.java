package mthomas;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import java.util.LinkedHashMap;

public class GObject implements Serializable { // GitObject. to not be confused with 'Object' data type.

    //for Blob
    private String objType;
    private String sha;
    private byte[] data;


    //for Tree+
    LinkedHashMap<String, String> blobList = new LinkedHashMap<String, String>();
    // key = filename, value = blobSha 
    // eg blobNameList {blob1.txt=47bc9874c3715f9f4b6b63d10b87803e2adb564ae88f3912975542c8}
    // LinkedHM keeps order of elements according to when it was put in.

    //for Commit
    private String timeStamp;
    private String author;
    private String committer; 
    private String logMsg;
    private String parent;
    private String treeSha; // for Commit and Tree object

    
    private ArrayList<String> parentShas; // for merge commit objects as they have two parent commits 


    //for untrackedFiles.txt
    private LinkedHashMap<String, String> untrackedFiles;   //fileName + commitSha (at which commit was fileName untracked)

    
    //BLOB object
    GObject(String type, String shaCode, byte[] arr) throws IOException{
        objType = type;
        sha = shaCode;  
        data = arr;  
    }

    // FOR TREE
    GObject(String type, LinkedHashMap<String, String> blobListInput ) throws IOException{
        objType = type;
        blobList = blobListInput;
    }

   
    //FOR 1st COMMIT
    // staging file needs to clear.
    GObject(String type, String authName, String committerName, String logMessage, String tSha){
        timeStamp = Util.setTimeStamp();
        objType = type;
        author = authName;
        committer = committerName;
        logMsg = logMessage;
        treeSha = tSha;
    }

    // FOR SECOND COMMIT ONWARDS
    GObject(String type, String authName,  String parentSha, String committerName, String logMessage, String tSha){
        timeStamp = Util.setTimeStamp();
        objType = type;
        parent = parentSha;
        author = authName;
        committer = committerName;
        logMsg = logMessage;
        treeSha = tSha;


    }

    // FOR MERGE COMMIT ONWARDS
    GObject(String type, String authName,  ArrayList<String> parentsList, String committerName, String logMessage, String tSha){
        timeStamp = Util.setTimeStamp();
    
        objType = type;
        parentShas = parentsList;
        author = authName;
        committer = committerName;
        logMsg = logMessage;
        treeSha = tSha;
    }

    GObject (LinkedHashMap<String, String> list){
        untrackedFiles = list;
    }

    public String getTimeStamp() {
        return timeStamp;
    }


    public String getAuthor() {
        return author;
    }


    public String getCommitter() {
        return committer;
    }


    public String getLogMsg() {
        return logMsg;
    }


    public String getParentSha() {
        return parent;
    }
 
    public String getObjType(){
        return objType;
    }

    public String getSha() {
        return sha;
    }

    
    public void setSha(String sha) {
        this.sha = sha;
    }

    
    public byte[] getByteData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }


    public LinkedHashMap<String, String> getBlobList() {
        return blobList;
    }

    public String getTreeSha() {
        return treeSha;
    }

    public ArrayList<String> getParentShas() {
        return parentShas;
    }

    public LinkedHashMap<String, String> getUntrackedFiles() {
        return untrackedFiles;
    }

    public void setUntrackedFiles(LinkedHashMap<String, String>untrackedFiles) {
        this.untrackedFiles = untrackedFiles;
    }


}
