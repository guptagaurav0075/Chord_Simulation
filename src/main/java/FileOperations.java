/**
 * Created by Gaurav on 11/11/17.
 */
public class FileOperations {
    String fileName;
    String sourcePath;
    String DestinationPath;
    String hashValue;
    String optimizedHashValue;
    String purpose;
    String sourceNode;

    public FileOperations(String fileName, String hashValue, String purpose, String sourceNode) {
        this.fileName = fileName;
        this.hashValue = hashValue;
        this.purpose = purpose;
        this.optimizedHashValue = this.hashValue;
        this.sourceNode = sourceNode;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public FileOperations(String fileName, String sourcePath, String hashValue, String purpose, String sourceNode) {
        this.fileName = fileName;
        this.sourcePath = sourcePath;
        this.hashValue = hashValue;
        this.purpose = purpose;
        this.optimizedHashValue  = this.hashValue;
        this.sourceNode = sourceNode;

    }

    public void setOptimizedHashValue(String optimizedHashValue) {
        this.optimizedHashValue = optimizedHashValue;
    }

    public String getPurpose() {

        return purpose;
    }

    public String getOptimizedHashValue() {
        return optimizedHashValue;
    }

    public String getFileName() {
        return fileName;
    }


    public String getSourcePath() {
        return sourcePath;
    }

    public String getDestinationPath() {
        return DestinationPath;
    }

    public String getHashValue() {
        return hashValue;
    }
}
