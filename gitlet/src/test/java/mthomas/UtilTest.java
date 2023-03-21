
package mthomas;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

public class UtilTest {

    // original function returns void, but testing purposes requires return of arraylist to enable cross-check testing.
    public static ArrayList<GObject> find(String logMsg) throws IOException{
        ArrayList<String> cObject = Util.extractFileList("commit"); // get commit files in objdir in absolutePath format -- OK
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
            System.out.println("Found no commit with that log message.");
        }
        else{
            for (int x = 0; x < gObj.size() ; x++){
                System.out.println("Commit(s) with log message: " + logMsg + " is found.");
                System.out.println(gObj.get(x).getSha());
            }
        }
        return gObj;
    }

    @Test
    public void testCheckOutFileFromCommit() throws ClassNotFoundException, IOException {
        
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }
        
        File f = new File(Util.getWD() + "\\testblob.txt");

        FileWriter fw = new FileWriter(f.toString());
        fw.write("This is a testBlob1.");
        fw.close();

        Util.setAuthor("tester");
        Util.add("testblob.txt");
        Util.commit("testblobcommit");

        ArrayList<GObject> firstCommit = find("testblobcommit");
        String commitSha = firstCommit.get(0).getSha();

        fw = new FileWriter(f.toString());
        fw.write("This is edited.");
        fw.close();

        Util.add("testblob.txt");
        Util.commit("secondtest");

        Util.checkOutFileFromCommit(commitSha, "testblob.txt");

        String str = Files.readString(Paths.get(f.toString()));

        assertFalse(str.contains("edited"));
        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));

    }

    @Test
    public void testAdd() throws IOException, ClassNotFoundException {
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }

        File f = new File(Util.getWD() + "test.txt");

        try {
            f.createNewFile(); 
            FileWriter fw = new FileWriter(f.toString());
            fw.write("Testing file.");
            fw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        Util.add("test.txt");
        
        ArrayList<String> fileNames = Util.extractFileList("blob");
        assertTrue(fileNames.get(0).contains("blob"));
        
        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
    }

    @Test
    public void testCommit() throws ClassNotFoundException, IOException {
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }

        File f = new File(Util.getWD() + "\\test.txt");

        try {
            f.createNewFile(); 
            FileWriter fw = new FileWriter(f.toString());
            fw.write("Testing file.");
            fw.close();
            System.out.println("CreatedFile.");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        Util.setAuthor("tester");
    
        Util.add("test.txt");
        Util.commit("test commit");

        // check contents of the file.
        ArrayList<String> fileNames = Util.extractFileList("commit");
        GObject tObj = Util.deserialiseCommitPathToGetTree(fileNames.get(0));
        LinkedHashMap<String, String> blobList = tObj.getBlobList();

        for (Entry<String, String> entry : blobList.entrySet()){
            assertTrue(entry.getKey().contains("test")); 
            // check if commit recorded blob content through access to tree object
            // that is linked to commit 
        }

        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));  
    }

    @Test
    public void testBranch() throws IOException, ClassNotFoundException {
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }
        
        File f = new File(Util.getWD() + "\\test.txt");

        try {
            f.createNewFile(); 
            FileWriter fw = new FileWriter(f.toString());
            fw.write("Testing file.");
            fw.close();
            System.out.println("CreatedFile.");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        Util.setAuthor("tester");
        Util.add("test.txt");
        Util.commit("test commit");
     

        Util.branch("new-branch");
        Util.checkOutBranch("new-branch");


        try {
            f.createNewFile(); 
            FileWriter fw = new FileWriter(f.toString());
            fw.write("Testing file.");
            fw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }

        Util.add("test.txt");
        Util.commit("test2nd commit");

        String p = Util.checkIfInBranch();
        String a = "new-branch.txt";

        assertTrue(p.contains(a));

        Util.returnToMasterBranch();
        String pa = Util.checkIfInBranch();

        assertFalse(pa.contains(a));

        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
    }

    @Test
    public void testFind() throws ClassNotFoundException, IOException {

        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }

        File f = new File(Util.getWD() + "\\2test.txt");

        try {
            f.createNewFile(); 
            FileWriter fw = new FileWriter(f.toString());
            fw.write("2Testing file.");
            fw.close();
            System.out.println("CreatedFile.");
        }
        catch(IOException e){
            e.printStackTrace();
        }

        Util.setAuthor("tester");
        Util.add("2test.txt");
        Util.commit("2test commit");

        ArrayList<GObject> gObjList =  find("2test commit");

        assertTrue(!(gObjList.isEmpty()));  
        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
           
    }

    @Test
    public void testInit() throws IOException {
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }
        File f = new File (Util.getWD() + "\\.git");
     
        assertTrue(f.exists());
        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git")); 
    }

    @Test
    public void testRm() throws IOException, ClassNotFoundException {
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }

        File f = new File(Util.getWD() + "\\test.txt");

        try {
            f.createNewFile(); 
            FileWriter fw = new FileWriter(f.toString());
            fw.write("Testing file.");
            fw.close();
        }
        catch(IOException e){
            e.printStackTrace();
        }
        Util.add("test.txt");
        Util.setAuthor("tester");
        Util.commit("first test");

        Util.rm("test.txt");

        GObject untrackedFileObj = (GObject) Util.deserialise(Util.getWD() + "\\.git\\info\\untrackedfiles.txt");
        LinkedHashMap<String, String> list = new LinkedHashMap<String, String>();
        list = untrackedFileObj.getUntrackedFiles();

        assertFalse(new File(Util.getWD() + "\\.git\\index\\test.txt").exists());
        System.out.println("list is" + list);
        assertTrue(list.containsKey("test.txt"));
        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
    }

    @Test
    public void testRmb() throws IOException, ClassNotFoundException {
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }

        File test = new File(Util.getWD() + "\\blob1.txt");
        test.createNewFile(); 
    
        try{
            FileWriter fw = new FileWriter(test.toString());
            fw.write("This is a test file.");
            fw.close();
        }catch(IOException e){
                e.printStackTrace();
        }

        Util.setAuthor("tester");
        Util.add("blob1.txt");
        Util.commit("test commit");

        Util.branch("test-branch"); 
        Util.checkOutBranch("test-branch");
        Util.rmb("test-branch");

        assertFalse(new File (Util.getWD() + "\\.git\\refs\\heads\\branch\\btestbranch.txt").exists());
        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
        
    }


    // aim: whether checkoutCommit fcn will restore version of file in committed blobs back to the target file.
    @Test
    public void testCheckOutCommitID() throws IOException, ClassNotFoundException {  
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }
        
        File test = new File(Util.getWD() + "\\blob1.txt");
        test.createNewFile(); 
    
        try{
            FileWriter fw = new FileWriter(test.toString());
            fw.write("This is a test file.");
            fw.close();
        }catch(IOException e){
                e.printStackTrace();
        }

        Util.setAuthor("tester");
        Util.add("blob1.txt");
        Util.commit("test commit");

        try{
            FileWriter fw = new FileWriter(test.toString());
            fw.write("This is an edited test file.");
            fw.close();
        }catch(IOException e){
                e.printStackTrace();
        }

        Util.add("blob1.txt");
        Util.commit("second test commit");
        
        String str = Files.readString(Paths.get(test.toString()));
 
        assert(str.contains("edited"));
        
        ArrayList<GObject> theCommit = find("test commit");
        String theCommitID = theCommit.get(0).getSha().substring(2);
        ArrayList<String> checkedOutFiles = new ArrayList<String>(); 

        System.out.println("<theCommmitID> is "+ theCommitID);
        Util.checkOutCommitID(theCommitID, checkedOutFiles);

        String strTwo = Files.readString(Paths.get(test.toString()));
        System.out.println("strTwo is" + strTwo);

        assertEquals("This is a test file.", strTwo);

        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));  
    }

    @Test
    public void testFindCommitFile() throws IOException, ClassNotFoundException {

        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }
        else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }
        
        File test = new File(Util.getWD() + "\\blob1.txt");
        test.createNewFile(); 
    
        try{
            FileWriter fw = new FileWriter(test.toString());
            fw.write("This is a test file.");
            fw.close();
        }catch(IOException e){
                e.printStackTrace();
        }

        Util.setAuthor("tester");
        Util.add("blob1.txt");
        Util.commit("test commit");

        ArrayList<GObject> theCommit = find("test commit");
        String theCommitID = theCommit.get(0).getSha().substring(2);
       
        String absPath = Util.findCommitFile(new File(Util.getWD() + "\\.git\\object").listFiles(), theCommitID, "");

        assertTrue(absPath.contains(theCommitID));

        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));  
    }
   
   /* aim: creating temporary files to test whether function will merge branch and head files together.
      goal: to merge branchMap back to headMap. assuming prioritisation of branchMap
      bTMap = branch Tree Map  ||  hTMap = head Tree Map
        test procedure:
    
        create 3 files on master branch,
        branch out to test-branch
        modify 1 file created in master branch in test-branch, remove 1 file of master branch, + 1 new files
        
        merge together result: file1 - new SHA | file2 - old SHA | file4 - new SHA | file6 - old SHA
    */
    @Test
    public void testMerge() throws IOException, ClassNotFoundException {
        
        if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
            Util.init(Util.getWD());
        }else{
            FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));
            Util.init(Util.getWD()); // start with empty git directory.
        }

        File blob1 = new File("blob1.txt"); File blob2 = new File( "blob2.txt"); File blob3 = new File( "blob3.txt");
      
        blob1.createNewFile(); blob2.createNewFile(); blob3.createNewFile(); 
       
    
        FileWriter fw = new FileWriter(blob1.toString());
        fw.write("This is a testBlob1.");
        fw.close();
    
        fw = new FileWriter(blob2.toString());
        fw.write("This is a testBlob2.");
        fw.close();
    
        fw = new FileWriter(blob3.toString());
        fw.write("This is a testBlob3.");
        fw.close();
       
        Util.add(blob1.toString()); Util.add(blob2.toString()); Util.add(blob3.toString());

        Util.setAuthor("tester");
        Util.commit("1st test commit at head");
    

        File blob6 = new File( "blob6.txt");
        fw = new FileWriter(blob6.toString());
        fw.write("This is a testBlob6.");
        fw.close();

        Util.add(blob6.toString());
        System.out.println("");
        Util.commit("2nd test commit at head");

        Util.branch("test-branch"); // branch pointer already created here 
        Util.checkOutBranch("test-branch");   


        File blob4 = new File("blob4.txt");
        blob4.createNewFile(); 
        fw = new FileWriter(blob1.toString()); 
        fw.write("This is a edited testBlob1.");
        fw.close();
        fw = new FileWriter(blob4.toString());   // modify file created on master branch previously
        fw.write("This is a testBlob4.");
        fw.close();
        Util.rm("blob3.txt");  

        Util.add(blob4.toString()); Util.add(blob1.toString());
        
        Util.commit("1st test commit at branch");
        
       // code to check bloblists of each commit
        ArrayList<GObject> bTObj = find("1st test commit at branch");
        GObject btObj = Util.deserialiseCommitObjToGetTree(bTObj.get(0));
        LinkedHashMap<String, String> bTMap = btObj.getBlobList(); 
        System.out.println("bTMap ->" + bTMap  );

        
        ArrayList<GObject> firstCommit = find("1st test commit at head");
        GObject firstObj = Util.deserialiseCommitObjToGetTree(firstCommit.get(0));
        LinkedHashMap<String, String> firstTMap = firstObj.getBlobList(); 
        System.out.println("firstTMap ->" + firstTMap);

        ArrayList<GObject> secondCommit = find("2nd test commit at head");
        GObject secTObj = Util.deserialiseCommitObjToGetTree(secondCommit.get(0));
        LinkedHashMap<String, String> secTMap = secTObj.getBlobList(); 
        System.out.println("secTMap ->" + secTMap);

        Util.merge("merge commit");

        ArrayList<GObject> mergeObj = find("merge commit");
        GObject mergeTObj = Util.deserialiseCommitObjToGetTree(mergeObj.get(0));
        LinkedHashMap<String, String> mergeMap = mergeTObj.getBlobList(); 
        
        LinkedHashMap<String, String> expectedMap = new LinkedHashMap<String, String>();
        expectedMap.put("blob1.txt" , bTMap.get("blob1.txt"));
        expectedMap.put("blob2.txt", firstTMap.get("blob2.txt"));
        expectedMap.put("blob4.txt", bTMap.get("blob4.txt"));
        expectedMap.put("blob6.txt", secTMap.get("blob6.txt"));

        System.out.println("expectedMap" + expectedMap);
        System.out.println("mergeMap" + mergeMap);

        assertTrue(mergeMap.get("blob1.txt").equals(expectedMap.get("blob1.txt")));
        assertTrue(mergeMap.get("blob2.txt").equals(expectedMap.get("blob2.txt")));
        assertTrue(mergeMap.get("blob4.txt").equals(expectedMap.get("blob4.txt")));
        assertTrue(mergeMap.get("blob6.txt").equals(expectedMap.get("blob6.txt")));

        FileUtils.deleteDirectory(new File(Util.getWD() + "\\.git"));  
    }

}

