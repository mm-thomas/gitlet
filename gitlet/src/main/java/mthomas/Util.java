package mthomas;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Scanner;
import java.util.Map.Entry;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.SerializationUtils;
import org.apache.commons.lang3.StringUtils;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA_224;

public class Util { 
    public static void init(String pathName) throws IOException {
        // create the directory
        new File(pathName + "\\.git").mkdir();
        new File(pathName + "\\.git\\index").mkdir(); // staging area
        new File(pathName + "\\.git\\object").mkdir();
        new File(pathName + "\\.git\\info").mkdir();
        new File(pathName + "\\.git\\HEAD").mkdir();
        new File(pathName + "\\.git\\refs\\heads").mkdirs();

        new File(pathName + "\\.git\\git-credentials").mkdir();

        // Get the path of the given file f
        Path path = Paths.get(pathName + "\\.git");

        // set hidden attribute
        Files.setAttribute(path, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
    }


    public static void setFileSeen() throws IOException{
         // Get the path of the given file f
        Path path = Paths.get(getWD() + "\\.git");

         // set hidden attribute
        Files.setAttribute(path, "dos:hidden", false, LinkOption.NOFOLLOW_LINKS);
    }

    public static void setFileHidden() throws IOException{
        // Get the path of the given file f
       Path path = Paths.get(getWD() + "\\.git");

        // set hidden attribute
       Files.setAttribute(path, "dos:hidden", true, LinkOption.NOFOLLOW_LINKS);
   }

   public static boolean isOrNotCompleteCmd(String [] cmdInput){ //helper function to check if user command is complete for respective cmd to run
        if (cmdInput.length > 1){
            return true;
        }
        else{
            return false;
        }
   }

    public static void pwd(){ // print current directory
        System.out.println("Working Directory =" + System.getProperty("user.dir")); 
    }

    public static String getWD(){ //get working directory // TO FIX: MAYBE , CURRENTLY GETTING PATH FROM USER INPUT
        return System.getProperty("user.dir");
    }


    public static byte[] objectToByteArray(GObject object) throws IOException {
        try (ByteArrayOutputStream bos = new ByteArrayOutputStream();
             ObjectOutputStream out = new ObjectOutputStream(bos)) {
          out.writeObject(object);
          out.flush();
          return bos.toByteArray();
        }
    }


    public static byte[] convertFileToBytes(Path p) throws IOException{
        byte[] arr = Files.readAllBytes(p);
        return arr;
    }


    public static void convertBytesToFile(byte[] arr, File f) throws IOException{
        String fileContent = new String(arr, StandardCharsets.UTF_8);
        try {
            FileWriter fw = new FileWriter(f.toString());
            fw.write(fileContent);
            fw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

    }
    
    // serialise object and save to file
    public static void serialise(GObject obj, String fileName) throws IOException{
        ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(fileName)); 
        //oos: objects -> binary format... fos: binary -> text -> stored in a file.
        oos.writeObject(obj);
        oos.close();
    }

    //deserialise object from file
    public static GObject deserialise(String f) throws IOException, ClassNotFoundException{
        ObjectInputStream ois = new ObjectInputStream(new FileInputStream(f));
        GObject obj = (GObject) ois.readObject();
        ois.close();
        return obj;
    }



//////////////////functions related to SHA/ Commit/ Tree manipulation/////////////////////////////////////////////////////////////////


    public static GObject deserialiseCommitPathToGetTree(String path) throws ClassNotFoundException, IOException{

        GObject cObj = (GObject) Util.deserialise(path);

        String treeSha = cObj.getTreeSha();
        String shaString = treeSha.substring(0, 2) + "\\" + StringUtils.replace( treeSha, treeSha.substring(0,2), "" );
        String tPath = Util.getWD() + "\\.git\\object\\" + shaString + "\\tree"+ shaString.substring(0,2)+".txt";

        GObject tObj = (GObject) Util.deserialise (new File(tPath).toString());     
        
        return tObj;

    }

    public static GObject deserialiseCommitObjToGetTree(GObject cObj) throws ClassNotFoundException, IOException{
        String treeSha = cObj.getTreeSha();
        String shaString = treeSha.substring(0, 2) + "\\" + StringUtils.replace( treeSha, treeSha.substring(0,2), "" );
        String tPath = Util.getWD() + "\\.git\\object\\" + shaString + "\\tree"+ shaString.substring(0,2)+".txt";

        GObject tObj = (GObject) Util.deserialise (new File(tPath).toString());     
        
        return tObj;

    }

    // create tree -> get hash code -> serialise tree -> add to object folder
    public static GObject createTree(String objType, LinkedHashMap<String, String> map) throws IOException{
        GObject t = new GObject ("Tree", map ); 
    
        String tSha = Util.createSHAforObj(t);
        t.setSha(tSha); 
        String tPath = Util.getWD() + "\\.git\\object\\" + tSha.substring(0, 2) + "\\" + StringUtils.replace( tSha, tSha.substring(0,2), "" );
        new File(tPath).mkdirs();
        File tFile = new File (tPath, "tree" + tSha.substring(0, 2) + ".txt");
        Util.serialise(t, tFile.toString());
    

        return t;

    }
   
    public static String createSHA(Path p) throws IOException{ // create SHA for blob
        String sha = new DigestUtils(SHA_224).digestAsHex(Files.readAllBytes(p)); 
        return sha;
    }

    public static String createSHAforObj(GObject gObj) throws IOException{
        String sha =  new DigestUtils(SHA_224).digestAsHex(SerializationUtils.serialize(gObj));
        return sha;
    }

     // parent Sha will be the current commit HEAD is pointing to
     // (to be commited right now) will call getParentCommitSha
     public static String getParentCommitSha() throws ClassNotFoundException, IOException{
        File f = new File(getWD() + "\\.git\\HEAD\\head.txt");
        if (checkIfFileExists(f.toString())){
           GObject gObj = (GObject) deserialise(f.toString());
           return gObj.getSha();
        }
        else{
            // if no file, then it is the first commit. --> parent is "".
            return "";
        }

    }

    public static void toFinishCommit(GObject c, String type) throws IOException, ClassNotFoundException{

            String cSha = Util.createSHAforObj(c);
            c.setSha(cSha);
          
            // formatting for commit's file name
            String cShaString = cSha.substring(0, 2) + "\\" + StringUtils.replace( cSha, cSha.substring(0,2), "" );
            String cPath = Util.getWD() + "\\.git\\object\\" + cShaString;
          
            new File (cPath).mkdirs();
            File cFile;
            if (type == "normalCommit"){
                cFile = new File (cPath, "commit"+cSha.substring(0,2)+".txt");
            }else{ // for merge commits
                cFile = new File (cPath, "mcommit"+cSha.substring(0,2)+".txt");
            }
            
            Util.serialise(c, cFile.toString());
            Util.toUpdateRefFiles(c);
            
            if(type == "merge"){
                System.out.print(c.getParentShas());
            
            }else{
                System.out.println("Parent SHA " + c.getSha() );
            }
            
            System.out.println("Tree " + c.getTreeSha() );
            System.out.println("Author: " + c.getAuthor());
            System.out.println("Date: " + c.getTimeStamp());
            System.out.println("Committer:" + c.getCommitter());;
            System.out.println(c.getLogMsg());   


            FileUtils.cleanDirectory(new File (getWD()+ "\\.git\\index"));

       
            File untrackedFiles = new File(getWD() + "\\.git\\info\\untrackedfiles.txt");
            
            //continuation of rm fcn 
            // this will complete entry(/s) with corresponding commit Sha at which the files were untracked at
            if (untrackedFiles.exists()){
                GObject untrackedObj = (GObject) deserialise(untrackedFiles.toString());
                LinkedHashMap<String, String> untrackedList = untrackedObj.getUntrackedFiles();
            
                for (Entry<String, String> entry : untrackedList.entrySet()){
                    if (entry.getValue() == ""){
                        untrackedList.put(entry.getKey(), cSha);
                    }
                }

                untrackedObj.setUntrackedFiles(untrackedList);
                serialise(untrackedObj, untrackedFiles.toString());
            }
       
    }



//////////////////functions related to checking directories/files///////////////////////////////////



    public static boolean checkIfDirExists(String fileName) {
        Path p = Paths.get( fileName); // create Path object & assigning file path to it
        File f = new File (p.toString());

        if (f.exists() && f.isDirectory()) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean checkIfFileExists(String fileName) {
        
        Path p = Paths.get( fileName); // immediately assumes file is in same directory, specified otherwise 
        File f = new File (p.toString());
        if (f.isFile() ) {
            return true;
        } else {
            return false;
        }
    }

    // try-with-resources in the example
    // so the stream is automatically closed when exiting the block.
    public static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

    // extract hashcodes of staged files from staging area to be 
    // linked to tree objects with the next commit
    public static ArrayList<String> extractFileList(String cmd) throws IOException{
        ArrayList<String> fileNames = new ArrayList<String>();
        if (cmd == "blob" && new File(getWD() + "\\.git\\object").listFiles() != null){
            File[] f = new File(getWD() + "\\.git\\object").listFiles();
            displayFiles(f, fileNames, cmd);
        }
        else if (cmd == "commit" && new File(getWD() + "\\.git\\object").listFiles() != null){
            File[] f = new File(getWD() + "\\.git\\object").listFiles();
            displayFiles(f, fileNames, cmd);
        }
        else if (cmd == "Index" && new File(getWD() + "\\.git\\index").listFiles() != null){
            File[] f = new File(getWD() + "\\.git\\index").listFiles();
            displayFiles(f, fileNames, cmd);
        }
        else if (cmd == "Branch" && new File(getWD() + "\\.git\\refs\\heads").listFiles() != null){
            File[] f = new File(getWD() + "\\.git\\refs\\heads").listFiles();
            displayFiles(f, fileNames, cmd);
        }
        else if (cmd == "WorkDir" && new File(getWD()).listFiles() != null ){
            File[] f = new File(getWD()).listFiles();
            displayFiles(f, fileNames, cmd);
        }
    
        return fileNames;
    }

    public static ArrayList<String> displayFiles(File[] files, ArrayList<String> nameList, String cmd) throws IOException
    {
        if (cmd == "blob"){
            for (File fn : files) {
                if (fn.isDirectory()) { // access object directory
                    // recursive call to list files present in sub directory
                    displayFiles(fn.listFiles(), nameList, cmd);
                }
                else {
                    // Getting the file name + append to nameList of files
                    if (fn.getName().contains("blob")){ 
                        nameList.add(fn.getAbsolutePath());
                    }
                }
            }
        }

        else if (cmd == "commit"){
            for (File fn : files) {
                if (fn.isDirectory()) { // access object directory
                    // recursive call to list files present in sub directory
                    displayFiles(fn.listFiles(), nameList, cmd);
                }
                else {
                    // Getting the file name + append to nameList of files
                    if (fn.getName().contains("commit")){ 
                        nameList.add(fn.getAbsolutePath());
                    }
                }
            }
        }
        
        else if (cmd == "Index"){ // access index directory
            for (File fn : files) {
                if (!fn.isDirectory() && fn.isFile()){
                    nameList.add(fn.getName());
                }
            }  
        }

        else if (cmd == "Branch"){ // access ref/head directory to get branches
            for (File fn : files) {
                if (!fn.isDirectory() && fn.isFile()){
                    nameList.add(fn.getName());
                }
            }  
        }

        else if (cmd == "WorkDir"){ // access working directory (outside .git folder)
            for (File fn : files) {
                if (!fn.isDirectory() && fn.isFile()){
                    nameList.add(fn.getName());
                }
            }  
        }

        return nameList; 
    }

    // store commit object in designated folders.
    // eg. if current pointer is a branch ref, then update HEAD pointer + branch ref
    // if current pointer is a master ref, then update HEAD pointer + master ref
    public static void toUpdateRefFiles(GObject f) throws ClassNotFoundException, IOException{
        File destA = new File("");
        File destB = new File(Util.getWD() +"\\.git\\HEAD\\head.txt" );  // HEAD is a pointer to the current ref pointer. (current branch user is in)

        String path = checkIfInBranch();
      
        if (!(destB.exists())){
            destB.createNewFile();
        }

        if (path == ""){
            destA = new File(Util.getWD() + "\\.git\\refs\\heads\\master.txt" ); // means that there is no branch, thus commit and progress in the master branch
        }
        else{
            destA = new File(path); // user is on a branch, thus, commit and progress in the separate branch.
        }
        try {
            serialise(f, destA.toString());
            serialise(f, destB.toString());
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    //comparing file content of REFs with HEAD because current pointer in REF == HEAD pointer.
    public static String checkIfInBranch() throws IOException, ClassNotFoundException{
        String path = "";
        
        if (!(new File (Util.getWD() +"\\.git\\HEAD\\head.txt").exists()) || 
        !(new File(Util.getWD() +"\\.git\\refs\\heads\\branch").exists())){
            return path;
        }
        else{
            GObject cObj = deserialise(Util.getWD() +"\\.git\\HEAD\\head.txt");
            File[] files = new File(Util.getWD() + "\\.git\\refs\\heads\\branch").listFiles();
            for (File fn : files){
                if (deserialise(fn.toString()).getSha().equals(cObj.getSha())){
                    path = fn.getAbsolutePath();
                }   
            } 
        }
        return path;
    }

    public static String returnCurrentBranchName() throws ClassNotFoundException, IOException{
        String path = checkIfInBranch();
        if (path ==""){
            return new File(Util.getWD() + "\\.git\\refs\\heads\\master.txt").getName(); 
        }
        else{
            return new File(path).getName();
        }
    }

    // find previously committed files that are not edited (staged again), but needs to be tracked.
    public static LinkedHashMap<String, String> findCommittedFiles( LinkedHashMap<String, String> nameWithSha, ArrayList<String> fStaging) throws IOException, ClassNotFoundException{
        //manipulate tree object
        GObject tObj = deserialiseCommitPathToGetTree(Util.getWD() + "\\.git\\HEAD\\head.txt");

        LinkedHashMap<String, String> prevkeyValueBlobs = tObj.getBlobList();
           
        for (Entry<String, String> entry : prevkeyValueBlobs.entrySet()) { 
            for (String s : fStaging){ 
                if (!(entry.getKey().equals(s))){
                    nameWithSha.put(entry.getKey(), entry.getValue());
                }
            }
        } 
        return nameWithSha;
    }


    public static String findCommitFile(File[] files, String commitID, String path) throws IOException, ClassNotFoundException{
        for (File fn : files) {
            if (fn.isDirectory()) {   
                path = findCommitFile(fn.listFiles(), commitID, path);
            }
            else {
                if (fn.getAbsolutePath().contains(commitID.substring(2))){
                String absPath = fn.getAbsolutePath();
                return absPath;
                }
            }
        }
        return path;
    }

////////////////////////////////////////////////////////////////////////////////////////////////

/////////////////functions related to setting etc information////////////////////////////////////


    // create new file and store user credentials.
    // if blob doesn't have any prev author, then this author is the author
    // move it to .git-credentials.
    public static void setAuthor(String author) throws IOException{
        File f = new File (getWD() + "\\.git\\git-credentials\\creds.txt");
        f.createNewFile();
   
        try {
            FileWriter fw = new FileWriter(f.toString());
            fw.write("Author: ");
            fw.write(System.getProperty("line.separator"));
            fw.write(author);
            fw.close();
          } catch (IOException e) {
            e.printStackTrace();
          }
    }  


    //read localuser line in credential text file
    public static String getLocalCommitter() throws IOException{
        String committer = Files.readAllLines(Paths.get(getWD() + "\\.git\\git-credentials\\creds.txt")).get(1);
        return committer;
    }

     //check if local user is registered in creds.txt
     public static boolean checkLocalUser() throws IOException{
        if (checkIfFileExists(getWD() + "\\.git\\git-credentials\\creds.txt")){
            return true;
        }
        else{
            return false; }
    }


    //check HEAD file, if it doesn't exist, then getAuthor, committer == author.
    // probably dont need this though. because committer is always the author, but author is not always the committer
    public static String getAuthor() throws IOException, ClassNotFoundException{

        File f = new File(getWD() + "\\.git\\HEAD\\head.txt");
        String author;

        if (checkIfFileExists(f.toString())){ // check HEAD file
            GObject gObj = (GObject) deserialise(f.toString());
            return gObj.getAuthor();
         
        }
        else{
            author = getLocalCommitter();
        }
        return author;
    }

    public static void requestCredentials() throws IOException{
        Scanner sc = new Scanner(System.in);
        System.out.println("Enter author name: ");
        String author = sc.nextLine(); //reads string;
        setAuthor(author);
        sc.close();
    }

    public static String setTimeStamp(){
        String timeStamp = new SimpleDateFormat("d MMM yyyy HH:mm:ss Z").format(new java.util.Date());
        return timeStamp;
    }


    

///////////////////////////////////////////////////////////////////////////////////////////

    public static void printCommitInfo(GObject gObj, String type) throws ClassNotFoundException, IOException{
        System.out.println("");System.out.println("===");      
        System.out.println("Commit SHA " + gObj.getSha() );
        
        if(type == "merge"){
            System.out.print("Merge Parent SHAs: ");
            for (int x = 0; x < gObj.getParentShas().size(); x++){
                System.out.print(gObj.getParentShas() + "  ");
            }
        }else{
            System.out.println("Parent SHA " + gObj.getSha() );
        }
        System.out.println("Date: " + gObj.getTimeStamp());
        System.out.println(gObj.getLogMsg());      
    }

    // deletes file from index dir & adds only fileName to untrackedList (fileName, "")
    // the "" part will be supplemented by commit ID in which user will commit next (written in commit() fcn)
    // untrackedList signifies which file was untracked at which commit ID.
    public static void rm(String fileName) throws IOException, ClassNotFoundException{
        File f = new File(Util.getWD() +"\\.git\\index\\" + fileName);
        File untrackedFiles = new File(getWD() + "\\.git\\info\\untrackedfiles.txt");
        
        LinkedHashMap<String, String> untrackedList = new LinkedHashMap<String, String>();

        if (checkIfTracked(fileName) ){
            if (!(untrackedFiles.exists())){
                untrackedList.put(fileName, "");
                GObject untrackedFileObj = new GObject(untrackedList);
                untrackedFiles.createNewFile();
                serialise(untrackedFileObj, untrackedFiles.toString()); 
            }else{
                GObject untrackedFileObj = (GObject) deserialise(untrackedFiles.toString());
                untrackedList = untrackedFileObj.getUntrackedFiles();
                untrackedList.put(fileName, "");
                serialise(untrackedFileObj, untrackedFiles.toString());   
            }
            f.delete();  //  Unstage the file if it is currently staged.
                    
            System.out.println("File untracked successfully"); 
        }
        else{
            System.out.println("No reason to remove file.");
        }
    }


    public static boolean checkIfTracked(String fileName) throws ClassNotFoundException, IOException{
        File untrackedFiles = new File(getWD() + "\\.git\\info\\untrackedfiles.txt");

        if (!(untrackedFiles.exists())){
            return true; 
        }
        GObject untrackedFileObj = (GObject) deserialise(untrackedFiles.toString());
        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
    
        list = untrackedFileObj.getUntrackedFiles();
        for (Entry<String, String> entry : list.entrySet()){
            if (entry.getKey().contains(fileName)){
                return true;
            }
        }
        
        return false;

    }
    
    public static void add(String fileName) throws IOException, ClassNotFoundException{
        File source = new File(Util.getWD()+"\\" +fileName);
        File fileAtStagingIndex = new File(Util.getWD() +"\\.git\\index\\" + fileName); 
        
        if (fileAtStagingIndex.exists() &&
            convertFileToBytes(Paths.get(fileAtStagingIndex.toString())) == convertFileToBytes(Paths.get(source.toString())) ){
            System.out.println("File is already in staging area");
            return;
        } 
        

        //deserialise untrcked files
        // if filename is found, then it means user wants to track the files again.
        File f = new File (getWD() + "\\.git\\info\\untrackedfiles.txt");
        if (f.exists()){
            GObject untrackedFileObj = (GObject) deserialise(getWD() + "\\.git\\info\\untrackedfiles.txt");
            LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
            list = untrackedFileObj.getUntrackedFiles();

            for (Entry<String, String> entry : list.entrySet()) {
                if(entry.getKey().contains(fileName)){
                    list.remove(fileName);
                    serialise(untrackedFileObj, getWD() + "\\.git\\info\\untrackedfiles.txt");
                }
        }
        }
        

        FileUtils.copyFile(source, fileAtStagingIndex);  
        addBlob(fileName);      
    }

      
    public static void addBlob(String fileName) throws IOException{

        Path p = Paths.get( fileName);
        String sha = Util.createSHA(p);

        //manipulate file name = first 2 SHA char / last 38 SHA char;
        String objPath = sha.substring(0, 2) + "\\" + StringUtils.replace( sha, sha.substring(0,2), "" );
        String path = Util.getWD() + "\\.git\\object\\" + objPath; 
        
        new File (path).mkdirs();
        File f = new File (path, "blob"+sha.substring(0, 2) + ".txt");

        GObject b = new GObject ("Blob", sha, Util.convertFileToBytes(p));
        Util.serialise(b, f.toString());


        File source = new File(Util.getWD() + "\\" + fileName);
        File stagedIndex = new File(Util.getWD() + "\\.git\\index" + "\\" + fileName);
        
        FileUtils.copyFile(source, stagedIndex);
    }



    // potential for rm cmd - to remove blob of that version file?

    public static void commit(String logMsg) throws IOException, ClassNotFoundException{
       
        //1. deserialise objects into files
        ArrayList<String> fObject = Util.extractFileList("blob"); // get files in object folder in absolutePath format 
        ArrayList<GObject> gObj = new ArrayList<GObject>();

        fObject.forEach(name -> {
            try {
                gObj.add((GObject) Util.deserialise(name)); // deserialise file names into java objects
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        } ); 

        //find correct file + byte array , put into key value pair 
        ArrayList<String> fStaging = Util.extractFileList("Index"); // get fileNames at staging area.
        LinkedHashMap<String, String> blobNameList = new LinkedHashMap<String, String>(); // key = filename, value = blobSha  

        for (String s : fStaging){ 
            for(GObject o : gObj){
                if (Arrays.equals(Util.convertFileToBytes(Paths.get(s)), o.getByteData())){
                    blobNameList.put(s, o.getSha());
                };
            }
        }
       
        GObject t = createTree("Tree", blobNameList);  // create tree -> get hash code -> serialise tree -> add to object folder

        GObject c;
        if (new File(Util.getWD() + "\\.git\\HEAD\\head.txt").exists()){ // second commit onwards
            c = new GObject("commit", Util.getAuthor(), Util.getParentCommitSha(), Util.getLocalCommitter(), logMsg, t.getSha());    
        }
        else{
            c = new GObject("commit", Util.getAuthor(), Util.getLocalCommitter(), logMsg, t.getSha()); 
        }

        toFinishCommit(c,"normalCommit");
        
    }

    public static void branch(String branchName) throws IOException, ClassNotFoundException{
        // create new pointer branch in the refs folder
        // make both point to the same commit object first
        File head = new File (getWD() + "\\.git\\refs\\heads\\master.txt");
        
        if (head.exists()){
            File newBranch = new File (getWD() + "\\.git\\refs\\heads\\branch");
            newBranch.mkdirs();

            File newBranchFile = new File (getWD() + "\\.git\\refs\\heads\\branch\\b" + branchName + ".txt");
            newBranchFile.createNewFile();
        
            GObject gObj = deserialise(head.toString());

            serialise(gObj, newBranchFile.toString());
            System.out.println("Created branch sucessfuly");
        }
        else{
            System.out.println("Need to commit first");
        }
    }
 
    // make HEAD file references current pointer.
    public static void checkOutBranch(String branchName) throws IOException, ClassNotFoundException{
        File newBranch = new File (getWD() + "\\.git\\refs\\heads\\branch\\b" + branchName + ".txt");
        if(newBranch.exists() == false){
            System.out.println("Branch does not exist.");
            return;
        }
             
        File head = new File (getWD() + "\\.git\\HEAD\\head.txt");
        GObject gObj = deserialise(newBranch.toString());
        serialise(gObj, head.toString());
    }   

   
    // make HEAD file point to master ref pointer.
    public static void returnToMasterBranch() throws IOException, ClassNotFoundException{

        File master = new File (getWD() + "\\.git\\refs\\heads\\master.txt");
        
        if (master.exists() == false){
            System.out.println("Need to commit file(s) first.");
            return;
        }
        
        File head = new File (getWD() + "\\.git\\HEAD\\head.txt");
        GObject gObj = deserialise(master.toString());
        serialise(gObj, head.toString());
        System.out.println("User on master branch."); 
    }
    
    // fcn will restore version of file(s) (snapshot of work dir) through recursively iterating over commitIDs;
    // prioritising most recent versions of files
    // checkedOutFiles tracks which files have been restored in user's work dir.
    
    public static void checkOutCommitID(String commitID, ArrayList<String> checkedOutFiles) throws ClassNotFoundException, IOException{ 
        String filePath = Util.findCommitFile(new File(Util.getWD() + "\\.git\\object").listFiles(), commitID, "");
        if (filePath == ""){
            System.out.println("File associated with Commit ID is not found");
            return; 
        }

        GObject tObj = deserialiseCommitPathToGetTree(filePath);
        LinkedHashMap<String, String> blobNameList = tObj.getBlobList(); // file name + pure blobSha
        
        //if is not recorded in untrackedFiles.txt, checkedOutFiles (a variable to track checked out files)
        if ((new File(Util .getWD() + "\\.git\\info\\untrackedfiles.txt")).exists()){
        LinkedHashMap<String, String> untrackedList = ((GObject) Util.deserialise(Util.getWD() + "\\.git\\info\\untrackedfiles.txt")).getUntrackedFiles(); // fileName + commitSha

        for (Entry<String, String> entry : blobNameList.entrySet()){
            if (!(untrackedList.containsKey(entry.getKey()))  &&  !(untrackedList.containsValue(commitID))
            && !(checkedOutFiles.contains(entry.getKey()))){
                checkedOutFiles.add(entry.getKey());
                checkOutFileFromCommit(filePath, entry.getKey());}
        }
        }else{
            for (Entry<String, String> entry : blobNameList.entrySet()){
                checkedOutFiles.add(entry.getKey());
                checkOutFileFromCommit(filePath, entry.getKey());
            }
        }
  
        String normalParent = ((GObject) deserialise(filePath)).getParentSha();
        ArrayList<String> mergeParent = ((GObject) deserialise(filePath)).getParentShas();

        if(normalParent == null && mergeParent == null){
            return;
        }
        else if (normalParent != null){
            checkOutCommitID(normalParent, checkedOutFiles);
        }
        else if (mergeParent != null){
           checkOutCommitID(mergeParent.get(0), checkedOutFiles);
        }
    }

    //Restores commit's version of given file to the workdir.
    //However, this file is not automatically staged.
    public static void checkOutFileFromCommit(String commitID, String fileName) throws ClassNotFoundException, IOException{
        String path = findCommitFile(new File(Util.getWD() + "\\.git\\object").listFiles(), commitID, "");
        if (path.isEmpty()){
            System.out.println("File of given commit ID not found.");
            return;
        }
       LinkedHashMap<String, String> tBlobList = (Util.deserialiseCommitPathToGetTree(path)).getBlobList();
        for (Entry<String, String> b : tBlobList.entrySet()){
            if (b.getKey().contains(fileName)){
                GObject blob = (GObject) deserialise(Util.getWD() + "\\.git\\object\\" + b.getValue().substring(0, 2) + "\\" +  b.getValue().substring(2)+"\\blob" + b.getValue().substring(0, 2) + ".txt");
                byte[] arr = blob.getByteData();
                Util.convertBytesToFile(arr, new File(Util.getWD() + "\\" + b.getKey()));       
                System.out.println("File checked out from commit successfully.");
            }
        }
    }


    //Displays what branches currently exist, and marks the current branch with a *. 
    //Also displays what files have been staged or untracked
    public static void status() throws ClassNotFoundException, IOException{
        ArrayList<String> indexFiles = extractFileList("Index"); // get staged files
        ArrayList<String> branches = extractFileList("Branch"); //get name of branches

        if (indexFiles.isEmpty() && branches.isEmpty()){
            System.out.println("Branches do not exist. No staged files. No new files in user working directory");
            return;
        }
        // finding current branch and adding add a star next to it... (* to indicate that it is current branch)
        for (int x = 0; x < branches.size(); x ++){
            if (branches.get(x).contains(returnCurrentBranchName())){
            
                branches.set(x, "*"+x);
            }
        }

        //============= get info about modified but not staged for commit

        ArrayList<String> workDir = Util.extractFileList("WorkDir"); // get filenames in working directory
        
        // deserialise HEAD pointer, get tree sha from commit object and then compare the file contents to the ones in the work dir.

        if (new File (getWD() + "\\.git\\HEAD\\head.txt").exists() == false){
            System.out.println("Branches do not exist. Files are not staged. No new files in directory");
            return;
        }
        
        GObject tObj = deserialiseCommitPathToGetTree(getWD() + "\\.git\\HEAD\\head.txt");
        LinkedHashMap<String, String> blobList = tObj.getBlobList(); 
        
        //============== find untracked files living in working dir
        ArrayList<String> untrackedFiles = extractFileList("WorkDir"); 

        for(Entry<String, String> entry : blobList.entrySet()){ // remove previously commited files from arrlist
            untrackedFiles.remove(entry.getValue());
        }

        for (String s: indexFiles){ // remove currently staged files from arrlist
            untrackedFiles.remove(s);
        }  
        // end result: new untracked files that exist in work directory.
        

        // ============== -> get only unstaged files
        for(Entry<String, String> entry : blobList.entrySet()){ // previously commited files
            for (String s: indexFiles){ // currently staged files
                if (entry.getKey().equals(s)){
                    blobList.remove(s);   
                    // end result = unstaged previously committed files.
                }
            }  
        }

        // *****
        LinkedHashMap<String, String> trackFiles = new LinkedHashMap<>();
        trackFiles = checkUnstagedFileStatus(workDir, blobList, trackFiles); // compare unstaged previously committed files to files in working directory

        //============================================
        // get info about staged, but removed from workdir.
        ArrayList<String> removedFiles = new ArrayList<String>();

            for (String t : indexFiles){
                if (!(checkIfFileExists(t))){ 
                    removedFiles.add(t);
                }
            }


        printStatus(branches, indexFiles, removedFiles, trackFiles, untrackedFiles);
    }


    public static LinkedHashMap<String, String>  checkUnstagedFileStatus(ArrayList<String> workDir, LinkedHashMap<String, String> blobList, LinkedHashMap<String, String> trackFiles) throws IOException{

        for (String s : workDir){ 
            for(Entry<String, String> entry : blobList.entrySet()){
                String bFilePath = Util.getWD() + "\\.git\\object\\" + entry.getValue().substring(0, 2) + "\\" + StringUtils.replace( entry.getValue(), entry.getValue().substring(0,2), "" ); 
                File f = new File (bFilePath);

                // set up for working directory path
                Path p = Paths.get(getWD() + s);

                if ((checkIfFileExists(entry.getKey()))){
                    if (entry.getKey().equals(s) && !(Arrays.equals(Util.convertFileToBytes(p), Util.convertFileToBytes(Paths.get(f.toString()))))){
                        trackFiles.put(entry.getKey(), "Modified" );}
                    else{
                        trackFiles.put(entry.getKey(), "Unchanged" );}
                }
                else { // file doesn't exist anymore
                    trackFiles.put(entry.getKey(), "Removed");
                }
            }
        }
        return trackFiles;
    }


    public static void printStatus(ArrayList<String> branches, ArrayList<String> indexFiles,  ArrayList<String> removedFiles,  LinkedHashMap<String,String> trackCommitedFiles, ArrayList<String> untrackedFiles){
        System.out.println("=== Branches === ");
        for (int x = 0; x < branches.size(); x++){
            System.out.println(branches.get(x));
        }
        System.out.println('\n' + '\n');


        System.out.println("=== Staged Files === ");
        for (int x = 0; x < indexFiles.size(); x++){
            System.out.println(indexFiles.get(x));
        }
        System.out.println('\n' + '\n');


        System.out.println("=== Removed Files === ");  // Staged for addition, but deleted in the working directory; or
        for(String s : removedFiles){
            System.out.println(s);
        }
        System.out.println('\n' + '\n');

        System.out.println("=== Modifications Not Staged For Commit ===");
        for(Entry<String, String> entry : trackCommitedFiles.entrySet()){
            if (entry.getValue().equals("Modified")){
                System.out.println(entry.getKey() + "(modified)");
            }
            else if (entry.getValue().equals("Unchanged")){
                System.out.println(entry.getKey() + "(unchanged)");
            }

            else if (entry.getValue().equals("Removed")){
                System.out.println(entry.getKey() + "(removed)");
            }
        }
        System.out.println('\n' + '\n');

        System.out.println("=== Untracked Files === ");  
        for(String s : untrackedFiles){
            System.out.println(s);
        }
        System.out.println('\n' + '\n');
    }



// goal: to merge branchMap back to headMap. assuming prioritisation of branchMap
// deserialise given branch & current head branch
// crosscheck files that are modified, removed, left unchanged, ... add to linkedHashMap trackedFiles = new LHM;
 
    public static void merge(String logMsg) throws ClassNotFoundException, IOException{
       String path = checkIfInBranch();

        if (path == ""){
            System.out.println("User is in main master branch. To merge files, switch to another branch"); // means that there is no branch, thus commit and progress in the master branch
            return;
        }
    
        // create commit objects 
        GObject branchCObj = (GObject) Util.deserialise(path);
        GObject rheadCObj = (GObject) Util.deserialise(getWD() + "\\.git\\refs\\heads\\master.txt");

        //manipulate tree objects
        GObject branchTObj = deserialiseCommitPathToGetTree(path);  
        GObject headTObj = deserialiseCommitPathToGetTree(getWD() + "\\.git\\refs\\heads\\master.txt");
 
        LinkedHashMap<String, String> branchTMap = branchTObj.getBlobList(); 
   
        branchTMap = getBlobs(branchCObj, branchTMap);
        branchTMap = getBlobs(rheadCObj, branchTMap);
        
        branchTMap = filterTheUntracked(branchCObj, branchTMap);
    
        // register SHAs of the two parent commits into the merge commit.
        ArrayList<String> parentsList = new ArrayList<String>();
        parentsList.add(branchTObj.getSha());
        parentsList.add(headTObj.getSha());


        //create new commit object with gBTMap, 2 parent shas
        GObject mergeCObj = new GObject("commit", Util.getAuthor(), parentsList, Util.getLocalCommitter(), logMsg, createTree("Tree", branchTMap).getSha());
        
        // change branch to ref/head.txt
        System.out.println("merge's toFinishCommit");
        toFinishCommit(mergeCObj, "mergeCommit");
        // at the end, current HEAD pointer needs to point at new commit created by merge of two branches.;
    }

    // get all tracked blobs by iterating through commits and trees , prioritising most recent blob version
    // if this commit blob's SHA not recorded
    // if this commit blob's fileName not under untracked files
    // add this tree's blob to recorded files
    public static LinkedHashMap<String, String> getBlobs (GObject cObj,  LinkedHashMap<String, String> treeMap) throws ClassNotFoundException, IOException{

        if (cObj.getParentSha() == null && cObj.getParentShas() == null ){
            return treeMap;
        }

        LinkedHashMap<String, String> untrackedList = ((GObject) deserialise(getWD() + "\\.git\\info\\untrackedfiles.txt")).getUntrackedFiles();

        if(cObj.getParentShas() != null){ // if is merge commit // select 1st parent - branch user was on when merge happened

            GObject prevCObj = (GObject) Util.deserialise(findCommitFile((new File( Util.getWD() + "\\.git\\object").listFiles()), cObj.getParentShas().get(0),""));
            LinkedHashMap<String, String> prevTObj = deserialiseCommitObjToGetTree(prevCObj).getBlobList();

            for (Entry<String, String> e : prevTObj.entrySet()){
                if(!(treeMap.containsValue(e.getValue()) && !(treeMap.containsKey(e.getKey())))){
                   
                    treeMap.put(e.getKey(), e.getValue());
                }
            }
           getBlobs(prevCObj, treeMap);
        }

        else{
            GObject prevCObj = (GObject) Util.deserialise(findCommitFile((new File( Util.getWD() + "\\.git\\object").listFiles()), cObj.getParentSha(),""));
            LinkedHashMap<String, String> prevTObj = deserialiseCommitObjToGetTree(prevCObj).getBlobList();

            for (Entry<String, String> e : prevTObj.entrySet()){
                if(!(treeMap.containsValue(e.getValue())) && !(treeMap.containsKey(e.getKey()))){
                   treeMap.put(e.getKey(), e.getValue());
                }
            }
           getBlobs(prevCObj, treeMap);
        }

        // if current commitSha matches one keyvalue pair in untrackedList, then remove the pair.
        treeMap.entrySet().removeIf(entry -> untrackedList.containsKey(entry.getKey()) && untrackedList.get(entry.getKey()).contains(cObj.getSha()));

        return treeMap;
    }

    public static LinkedHashMap<String, String> filterTheUntracked(GObject cObj,  LinkedHashMap<String, String> treeMap) throws ClassNotFoundException, IOException{
        if (cObj.getParentSha() == null && cObj.getParentShas() == null ){
            return treeMap;
        }

        LinkedHashMap<String, String> untrackedList = ((GObject) deserialise(getWD() + "\\.git\\info\\untrackedfiles.txt")).getUntrackedFiles();
        treeMap.entrySet().removeIf(entry -> untrackedList.containsKey(entry.getKey()) && untrackedList.get(entry.getKey()).contains(cObj.getSha()));
    
        if(cObj.getParentShas() != null){ // if is merge commit // select 1st parent --> the branch user was on when merge happened

            GObject prevCObj = (GObject) Util.deserialise(findCommitFile((new File( Util.getWD() + "\\.git\\object").listFiles()), cObj.getParentShas().get(0),""));
            filterTheUntracked(prevCObj, treeMap);
        }
        else{
            GObject prevCObj = (GObject) Util.deserialise(findCommitFile((new File( Util.getWD() + "\\.git\\object").listFiles()), cObj.getParentSha(),""));
           filterTheUntracked(prevCObj, treeMap);
        }

        return treeMap;
    }

    // print all information from current commit object head is pointing to
    // recursively call function until first commit is reached.
    public static void log (GObject gObj) throws ClassNotFoundException, IOException{
        
        if (gObj.getParentSha().isEmpty() && gObj.getParentShas().isEmpty()){ // first commit
            printCommitInfo(gObj, "");
        }

        else if(gObj.getParentSha().isEmpty() && !(gObj.getParentShas().isEmpty())){ // merge commits have two parents
            printCommitInfo(gObj, "merge");

            String a = findCommitFile( new File(Util.getWD() + "\\.git\\object").listFiles(), gObj.getParentShas().get(0), "");
            log((GObject) Util.deserialise (a));
            String b = findCommitFile( new File(Util.getWD() + "\\.git\\object").listFiles(), gObj.getParentShas().get(1), "");
            log((GObject) Util.deserialise (b));
        }

        else if(!(gObj.getParentSha().isEmpty()) && gObj.getParentShas().isEmpty()){ // normal commit
            printCommitInfo(gObj, "normal");
            String path = findCommitFile(new File(Util.getWD() + "\\.git\\object").listFiles(), gObj.getSha(), "");
            log((GObject) Util.deserialise (path));
        }

        return;
    }

    // print information about all commit object.
    public static void glog() throws IOException{

        ArrayList<String> commitList = Util.extractFileList("commit"); // get commit files
        if(commitList.isEmpty()){
            System.out.println("No commits to show.");
        } 
        else{
            ArrayList<GObject> commitObjects = new ArrayList<GObject>();
            
            commitList.forEach(commit -> {
                try {
                    commitObjects.add((GObject) Util.deserialise(commit)); // deserialise commit files into java objects
                } catch (ClassNotFoundException | IOException e) {
                    e.printStackTrace();
                }
            } ); 

            for (GObject commit : commitObjects){
                System.out.println("===");
                System.out.println("commit " + commit.getSha());
                System.out.println("Date: " + commit.getTimeStamp());
                System.out.println("Parent: " + commit.getParentSha());
                System.out.println( commit.getLogMsg() + "\n");
            
            }
        }
    }


    // aim: to find commits with the same log message.
    // get commit files, deserialise into commit objects, filter through using the input logMsg
    public static void find(String logMsg) throws IOException{
        ArrayList<String> cObject = Util.extractFileList("commit"); // get commit files in objdir in absolutePath format -- OK
        if (cObject == null){
            System.out.println("Unable to find commit file with given log message because no commit files exists.");
            return;
        }else{
        ArrayList<GObject> gObj = new ArrayList<GObject>();

        cObject.forEach(name -> {
            try {
                gObj.add((GObject) Util.deserialise(name)); // deserialise file names into java objects
            } catch (ClassNotFoundException | IOException e) {
                e.printStackTrace();
            }
        } );

        for (int x = 0; x < gObj.size() ; x++){
           if (!(gObj.get(x).getLogMsg().equals( logMsg))){   
                gObj.remove(gObj.get(x));
            } 
        }

        if (gObj.isEmpty()){
            System.out.println("Commit with given log message not found.");
        }
        else{
            for (int x = 0; x < gObj.size() ; x++){
                System.out.println("Commit(s) with log message: " + logMsg + " is found.");
                System.out.println(gObj.get(x).getSha());
            }
        }
    }
        return;
    }

    // description: Deletes the branch with the given name.
    public static void rmb(String branch) throws ClassNotFoundException, IOException{
        if (checkIfFileExists(Util.getWD() + "\\.git\\refs\\heads\\branch\\b" + branch +".txt")){
            if (returnCurrentBranchName() != Util.getWD() + "\\.git\\refs\\heads\\branch\\b" + branch +".txt"){
                File branchFile = new File(Util.getWD() + "\\.git\\refs\\heads\\branch\\b" + branch +".txt");
                if (branchFile.delete()){
                    System.out.println(branch + " was deleted successfully");
                    
                    //change pointer to master branch
                    File masterBranch = new File (getWD() + "\\.git\\refs\\heads\\master.txt");
                    File head = new File (getWD() + "\\.git\\HEAD\\head.txt");
                    try {
                        FileUtils.copyFile(masterBranch, head);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }else{
                System.out.println("User is on current branch. Cannot remove the current branch.");
            }
        }
        else{
            System.out.println("Given branch does not exist");
        }
    }
}

