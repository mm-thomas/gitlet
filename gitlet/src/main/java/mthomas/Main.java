package mthomas;


import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.StringJoiner;

import org.apache.commons.lang.StringUtils;

public class Main{

    // main commands that run this limited version-control system.
    static String addCmd = "add";
    static String comCmd = "commit";
    static String initCmd = "init";
    static String rmCmd = "rm"; // removes file from index and marks as untracked... only reversable when user adds the file again.
    static String rmbCmd = "rmb";
    static String logCmd = "log";
    static String gLogCmd = "glog";
    static String findCmd = "find";
    static String statusCmd = "status";
    static String cOutCmd = "checkout"; // file
    static String cOutID = "coutid"; // check-out commitid
    static String cBOutCmd = "coutb"; // checkout branch
    static String bCmd = "branch";
    static String mCmd = "merge";
    static String rHeadCmd = "rth"; // return to head
    static String seenCmd = "seen";
    static String hideCmd = "hidden";
    static String exitCmd = "exit";
    

    public static void main(String[] args) throws IOException, ClassNotFoundException{
    
        try (Scanner sc = new Scanner(System.in)) {
            while(true){
            
                System.out.println("Enter a command: ");
                String command = sc.nextLine(); // reads string
                command.toLowerCase();
                String[] cmdInput = StringUtils.split(command, ' ');

            

                if (cmdInput[0].equals(addCmd)){
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }
                    for (int x = 1; x < cmdInput.length; x++){
                    if (Util.checkIfFileExists(Util.getWD() + "\\" + cmdInput[x])){
                        Util.add(cmdInput[x]); 
                        System.out.println("Added file");
                        }
                        else{
                            System.out.println("File does not exist in user working directory.");
                        }
                    }
                }

                else if (cmdInput[0].equals(comCmd)){  
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }else{

                    

                    if (Util.isDirEmpty(Paths.get(Util.getWD() + "\\.git\\index")) == true){ // to handle failure case - when there are no files in staging directory
                        if (!Util.checkLocalUser()){
                            Util.requestCredentials();
                        }
                        Util.commit(cmdInput[1]);
                    } else{
                        System.out.println("No files are present at staging directory. Unable to commit.");
                    }
                    }
                }
                
                else if (cmdInput[0].equals(initCmd)) {
                    if (Util.checkIfDirExists(Util.getWD() + "\\.git") == false){
                        Util.init(Util.getWD());
                        System.out.println("Git directory successfully initialised");
                    }else{
                        System.out.println("Git directory already initialied in directory.");
                    }
                }

                else if (cmdInput[0].equals(rmCmd)){   //can have many files rm file1.txt file2.txt 
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");     
                    }else{
                        for (int x = 1; x < cmdInput.length; x++){
                            if (new File (Util.getWD() + "\\" + cmdInput[x]).exists()){
                                Util.rm(cmdInput[x]);
                            }else{
                                System.out.println("File not present. Unable to remove.");
                            }
                        }  
                    }
                }

                else if (cmdInput[0].equals(rmbCmd)){
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }else{
                        if(new File (Util.getWD() + "\\.git\\refs\\heads\\branch\\b" + cmdInput[1] + ".txt").exists()){
                            Util.rmb(cmdInput[1]);
                        }
                        else{
                            System.out.println("Given branch does not exist in git directory.");
                        }
                    }
                }

                else if (cmdInput[0].equals(logCmd)){
                    if (!(new File (Util.getWD() + "\\.git\\HEAD\\head.txt").exists())){
                        System.out.println("No commits to show");
                    }
                    else{
                       Util.log((GObject) Util.deserialise (Util.getWD() + "\\.git\\HEAD\\head.txt"));
                    }
                }

                else if (cmdInput[0].equals(gLogCmd)){
                    Util.glog();      // failure case handled at glog()
                }

                else if (cmdInput[0].equals(findCmd)){
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }else{
                        StringJoiner strJoiner = new StringJoiner(" ");
                        for (int i = 1; i < cmdInput.length; i++) {
                            strJoiner.add(cmdInput[i]);
                        }
                        String newString = strJoiner.toString();
                                
                        Util.find(newString);  
                    }
                }
            
                else if (cmdInput[0].equals(statusCmd)){
                    Util.status(); // failure case handled at status() 
                }

                else if (cmdInput[0].equals(cOutCmd)){ // checkout commitID file.txt
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }else{
                    Util.checkOutFileFromCommit(cmdInput[1], cmdInput[2]); 
                    }
                }  
                
                
                else if (cmdInput[0].equals(cOutID)){ // checkout snapshot of ID // coutID commitID
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }else{
                        if (new File (Util.getWD() + "\\" + cmdInput[1]).exists()){ 
                            Util.checkOutCommitID(cmdInput[1], new ArrayList<String>());
                        }else{
                            System.out.println("Given commit ID does not exist in git directory.");
                        }
                    }
                }

                else if (cmdInput[0].equals(cBOutCmd)){ // coutb branchName
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                  
                    }else{
                        if(new File (Util.getWD() + "\\.git\\refs\\heads\\branch\\b" + cmdInput[1] + ".txt").exists()){
                            Util.checkOutBranch(cmdInput[1]);
                        }
                        else{
                            System.out.println("Given branch name does not exist in git directory.");
                        }
                    }   
                }

                else if ( cmdInput[0].equals(bCmd)){
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }else{
                        Util.branch(cmdInput[1]);
                    }
                  }

        
                else if (cmdInput[0].equals(mCmd)){
                    if(!Util.isOrNotCompleteCmd(cmdInput)){
                        System.out.println("Incomplete command.");
                    }else{
                        Util.merge(cmdInput[1]); // failure case handled at merge()
                    }
                }
                
                else if (cmdInput[0].equals(rHeadCmd)) {
                    Util.returnToMasterBranch();
                    
                }

                else if (cmdInput[0].equals(seenCmd)) {
                    if (Util.checkIfDirExists(Util.getWD() + "\\.git") == true){
                        Util.setFileSeen();
                    }else{
                        System.out.println("No git directory initialised in folder");
                    }
                }

                else if (cmdInput[0].equals(hideCmd)) {
                    Util.setFileHidden();
                }

                else if (cmdInput[0].equals(exitCmd)) {
                    return;
                }
            
                cmdInput = new String[10];
                }
            }
    } 
}



