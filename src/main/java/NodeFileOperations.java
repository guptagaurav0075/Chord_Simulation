import scala.Int;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * Created by Gaurav on 11/11/17.
 */
class NodeFileOperations {
    private TreeMap<String, TreeSet<String>> file_hash_value_to_fileName;
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
        if(file_hash_value_to_fileName.containsKey(fo.getHashValue())){
             set_Of_Files = file_hash_value_to_fileName.get(fo.getHashValue());
             if(set_Of_Files.contains(fo.getFileName())){
                 System.out.println("Server already has file with name");
                 return;
             }
             transferFile(fo);
             set_Of_Files = file_hash_value_to_fileName.get(fo.getHashValue());
             set_Of_Files.add(fo.getFileName());
             return;
        }
        transferFile(fo);
        set_Of_Files = new TreeSet<>();
        set_Of_Files.add(fo.getFileName());
        file_hash_value_to_fileName.put(fo.getHashValue(), set_Of_Files);
    }
    public void searchFile(FileOperations fo){
        System.out.println();
        if(file_hash_value_to_fileName.containsKey(fo.getHashValue())){
            if(file_hash_value_to_fileName.get(fo.getHashValue()).contains(fo.getFileName())){
                System.out.println("File is stored at node:"+currentNode);
                System.out.println();
                return;
            }
        }
        System.out.println("File is not stored anywhere on the Server");
    }
    private void transferFile(FileOperations fo){
        File file_Orig = new File(fo.getSourcePath());
        File file_New = new File(directoryPath+"/"+fo.getFileName());
        fileTransfer(file_Orig, file_New);
    }
    private void fileTransfer(File sourceFile, File destinationFile){
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
            e.printStackTrace();
        }
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
        String hash = utl.generateHashString(fo.getSourceNode(), PrimaryServerClass.getInstance().getLOG_N());
        Map.Entry<String, TreeSet<String>> entry = file_hash_value_to_fileName.floorEntry(hash);
        while (entry!=null){
            TreeSet<String> setOfFiles = entry.getValue();
            while (setOfFiles.size()>0){
                String fileName = setOfFiles.first();
                File sourceFile = new File(directoryPath+"/"+fileName);
                File destination = new File(fo.getSourcePath()+"/"+fileName);
                fileTransferAndRemove(sourceFile, destination);
            }
        }
    }

    public void doneLoadBalance() throws IOException, NoSuchAlgorithmException {
        File currDir = new File(directoryPath);
        getAllFiles(currDir);
    }
    private void getAllFiles(File curDir) throws IOException, NoSuchAlgorithmException {

        File[] filesList = curDir.listFiles();
        for(File f : filesList){
            if(f.isFile()){
                String hash = utl.generateHashString(f.getName(), PrimaryServerClass.getInstance().getLOG_N());
                TreeSet<String> files;
                if(file_hash_value_to_fileName.containsKey(hash)){
                    files = file_hash_value_to_fileName.get(hash);
                }else{
                    files = new TreeSet<>();
                    file_hash_value_to_fileName.put(hash, files);
                }
                files.add(f.getName());
            }
        }

    }
}
