import scala.Int;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Gaurav on 11/11/17.
 */
class NodeFileOperations {
    private TreeMap<Integer, TreeSet<String>> file_hash_value_to_fileName;
    String currentNode;
    String directoryPath;
    Utility utl = new Utility();

    public NodeFileOperations(String currentNode) {
        this.currentNode = currentNode;
        this.file_hash_value_to_fileName = new TreeMap<>();
        directoryPath = PrimaryServerClass.getInstance().getSourceDirectoryPath()+"/"+currentNode;
        createDirectory();
    }
    public void addFile(FileOperations fo){
        TreeSet<String> set_Of_Files;
        boolean status=false;
        int hashValue = Integer.valueOf(fo.getHashValue());
        if(file_hash_value_to_fileName.containsKey(hashValue)){
             set_Of_Files = file_hash_value_to_fileName.get(hashValue);
             if(set_Of_Files.contains(fo.getFileName())){
                 System.out.println("Server already has file with name");
                 return;
             }
             status = transferFile(fo);
             if(!status)
                 return; //file not stored due to error
             set_Of_Files = file_hash_value_to_fileName.get(hashValue);
             set_Of_Files.add(fo.getFileName());
             return;
        }
        status = transferFile(fo);
        if(!status)
            return; // file not stored due to some error
        set_Of_Files = new TreeSet<>();
        set_Of_Files.add(fo.getFileName());
        file_hash_value_to_fileName.put(hashValue, set_Of_Files);
    }
    public void searchFile(FileOperations fo){
        System.out.println();
        int hashValue = Integer.valueOf(fo.getHashValue());
        if(file_hash_value_to_fileName.containsKey(hashValue)){
            if(file_hash_value_to_fileName.get(hashValue).contains(fo.getFileName())){
                System.out.println("File is stored at node:"+currentNode);
                System.out.println();
                return;
            }
        }
        System.out.println("File is not stored anywhere on the Server");
    }
    private boolean transferFile(FileOperations fo){

        File file_Orig = new File(fo.getSourcePath());
        File file_New = new File(directoryPath+"/"+fo.getFileName());
        return fileTransfer(file_Orig, file_New);
    }
    private boolean fileTransfer(File sourceFile, File destinationFile){
        InputStream inStream = null;
        OutputStream outStream = null;
        try{

            inStream = new FileInputStream(sourceFile);
            outStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0){
                outStream.write(buffer, 0, length);
            }

            inStream.close();
            outStream.close();

        }catch(IOException e){
//            e.printStackTrace();
            System.out.println("File Transferred failed. Error: "+e.getMessage());
            return false;
        }
        return true;
    }
    private void fileTransferAndRemove(File sourceFile, File destinationFile){
        InputStream inStream = null;
        OutputStream outStream = null;
        try{

            inStream = new FileInputStream(sourceFile);
            outStream = new FileOutputStream(destinationFile);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0){
                outStream.write(buffer, 0, length);
            }
            sourceFile.delete();
            inStream.close();
            outStream.close();

        }catch(IOException e){
            e.printStackTrace();
        }
    }
    private void createDirectory(){
        File checkSourceDirectory = new File(directoryPath);
        if(!checkSourceDirectory.exists()){
            checkSourceDirectory.mkdir();
        }
    }
    public void transferFiles(FileOperations fo) throws IOException, NoSuchAlgorithmException {
//        String hash = utl.generateHashString(fo.getSourceNode(), PrimaryServerClass.getInstance().getLOG_N());
        System.out.println("In tansfer Files Operations");
        fo.printFileDesctiption();
        int hashValue = Integer.valueOf(currentNode);
        if(fo.getPurpose().equals("PredecessorLoadBalance"))
            tranferFilesFromPredecessor(fo, hashValue+1, fo.getSuccessorOfSourceNode());
        else if(fo.getPurpose().equals("SuccessorLoadBalance"))
            tranferFilesFromSuccessor(fo, hashValue-1, fo.getPredecessorOfSourceNode());

    }
    private void tranferFilesFromPredecessor(FileOperations fo, int hashOfCurrentNode, int successorOfSourceNode){
        Map.Entry<Integer, TreeSet<String>> entry = file_hash_value_to_fileName.ceilingEntry(hashOfCurrentNode);
//        if(){
//            //example-1: currentNode 400, sourceNode  = 200 successorNode 250
//        }
        while (entry!=null){
            TreeSet<String> setOfFiles = entry.getValue();
            while (setOfFiles.size()>0){
                String fileName = setOfFiles.first();
//                System.out.println("File to be transferred is: "+fileName);
                File sourceFile = new File(directoryPath+"/"+fileName);
//                System.out.println("SourcePath is:"+sourceFile.getAbsolutePath());
                File destination = new File(fo.getSourcePath()+"/"+fileName);
//                System.out.println("destination is:"+destination.getAbsolutePath());
                fileTransferAndRemove(sourceFile, destination);
                setOfFiles.remove(setOfFiles.first());
            }
            file_hash_value_to_fileName.remove(entry.getKey());
            entry = file_hash_value_to_fileName.ceilingEntry(hashOfCurrentNode);
        }
    }
    private void tranferFilesFromSuccessor(FileOperations fo, int hashOfCurrentNode, int predecessorOfSourceNode){
        System.out.println("\n**** In NoadFileOperation File->transferFileFromSuccessor function ***");
        Map.Entry<Integer, TreeSet<String>> entry;
        predecessorOfSourceNode = predecessorOfSourceNode+1;
        if(Integer.valueOf(fo.getSourceNode())<predecessorOfSourceNode && Integer.valueOf(fo.getSourceNode())<Integer.valueOf(currentNode)){
            // example1: predcessorNode = 1022 & newSourceNode = 100 & currentNode = 200
            // example2: predcessorNode = 1022 & newSourceNode = 100 & currentNode = 1022
            System.out.println("Source Node:"+fo.getSourceNode()+ "\n Current Node :"+currentNode+"\n Predecessor Node"+predecessorOfSourceNode);
            entry =  file_hash_value_to_fileName.ceilingEntry(predecessorOfSourceNode);
            // transfer all the files which has hashValue stored in node 200 with values greater than 1022;
            while (entry!=null){
                transferFileInternal(entry.getValue(), fo);
                file_hash_value_to_fileName.remove(entry.getKey());
                entry =  file_hash_value_to_fileName.ceilingEntry(predecessorOfSourceNode);
            }
            // transfer all files which has hashvalues less than newSourceNode which in this case would be 100, therefore
            entry = file_hash_value_to_fileName.floorEntry(Integer.valueOf(fo.getSourceNode()));
            while (entry!=null){
                transferFileInternal(entry.getValue(), fo);
                file_hash_value_to_fileName.remove(entry.getKey());
                entry = file_hash_value_to_fileName.floorEntry(Integer.valueOf(fo.getSourceNode()));
            }

        } else if (Integer.valueOf(fo.getSourceNode()) > predecessorOfSourceNode && Integer.valueOf(fo.getSourceNode()) > Integer.valueOf(currentNode)) {
            // example: predcessorNode = 1010 & newSourceNode = 1072 & currentNode = 884
            System.out.println("Source Node:"+fo.getSourceNode()+ "\n Current Node :"+currentNode+"\n Predecessor Node"+predecessorOfSourceNode);
            entry = file_hash_value_to_fileName.ceilingEntry(predecessorOfSourceNode);
            while (entry!=null && entry.getKey()<=(Integer.valueOf(fo.getSourceNode())) && entry.getKey()>Integer.valueOf(currentNode)){
                transferFileInternal(entry.getValue(), fo);
                file_hash_value_to_fileName.remove(entry.getKey());
                entry =  file_hash_value_to_fileName.ceilingEntry(predecessorOfSourceNode);
            }

        } else if(Integer.valueOf(fo.getSourceNode()) > predecessorOfSourceNode && Integer.valueOf(fo.getSourceNode()) < Integer.valueOf(currentNode)) {
            // example: predcessorNode = 1000 & newSourceNode = 1012 & currentNode = 1020
            System.out.println("Source Node:"+fo.getSourceNode()+ "\n Current Node :"+currentNode+"\n Predecessor Node"+predecessorOfSourceNode);
            entry = file_hash_value_to_fileName.firstEntry();
            while (entry != null && entry.getKey() <= Integer.valueOf(fo.getSourceNode())) {
                transferFileInternal(entry.getValue(), fo);
                file_hash_value_to_fileName.remove(entry.getKey());
                entry = file_hash_value_to_fileName.firstEntry();
            }
        }
    }
    private void transferFileInternal(TreeSet<String> setOfFiles, FileOperations fo){
        while (setOfFiles.size()>0){
            String fileName = setOfFiles.first();
//                System.out.println("File to be transferred is: "+fileName);
            File sourceFile = new File(directoryPath+"/"+fileName);
//                System.out.println("SourcePath is:"+sourceFile.getAbsolutePath());
            File destination = new File(fo.getSourcePath()+"/"+fileName);
//                System.out.println("destination is:"+destination.getAbsolutePath());
            fileTransferAndRemove(sourceFile, destination);
            setOfFiles.remove(setOfFiles.first());
        }
    }
    public void doneLoadBalance() throws IOException, NoSuchAlgorithmException {
//        System.out.println("Done Load Balancing");
        File currDir = new File(directoryPath);
        getAllFiles(currDir);
        printTreeMap();
    }
    public void printTreeMap(){
        System.out.println("\n\n");
        if(file_hash_value_to_fileName.size()==0){
            System.out.println("\n\n****** Currently there are no files on this server ******\n\n");
            return;
        }
        for(Map.Entry<Integer, TreeSet<String>> entry : file_hash_value_to_fileName.entrySet()){
            System.out.println("Files with hash value :"+entry.getKey());
            Iterator itr = entry.getValue().iterator();
            while (itr.hasNext()){
                System.out.println("File Name :"+itr.next());
            }
            System.out.println();
        }
        System.out.println("\n\n");
    }
    private void getAllFiles(File curDir) throws IOException, NoSuchAlgorithmException {
//        System.out.println("In get all files function");
        if(curDir.isDirectory()){
            File[] filesList = curDir.listFiles();
            for(File fileFromList : filesList){
                if(fileFromList.isFile()){
//                String hash = utl.generateHashString(fileFromList.getName(), PrimaryServerClass.getInstance().getLOG_N());
                    int hashValue = utl.generateHashString(fileFromList.getName(), PrimaryServerClass.getInstance().getLOG_N());
//                int hashValue = Integer.valueOf(hash);
//                    System.out.println("Hash Value of File "+fileFromList.getName()+" is :"+hashValue);
                    TreeSet<String> files;
                    if(file_hash_value_to_fileName.containsKey(hashValue)){
                        files = file_hash_value_to_fileName.get(hashValue);
                        files.add(fileFromList.getName());
                    }else{
                        files = new TreeSet<String>();
                        files.add(fileFromList.getName());
                        file_hash_value_to_fileName.put(hashValue, files);
                    }
                }
            }
        }
    }
}
