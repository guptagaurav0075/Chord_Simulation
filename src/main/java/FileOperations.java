import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gaurav on 11/11/17.
 */
public class FileOperations {
    String fileName;
    String sourcePath;
    String destinationPath;
    String hashValue;
    String optimizedHashValue;
    String purpose;
    String sourceNode;
    int destionationNode;

    public int getDestionationNode() {
        return destionationNode;
    }

    public FileOperations(int destinationNode, String sourceNode, String sourceNodePath, String purpose) throws IOException, NoSuchAlgorithmException {
        this.sourcePath = sourceNodePath;
        this.sourceNode = sourceNode;
        this.destionationNode = destinationNode;
        this.purpose = purpose;
        this.hashValue = new Utility().generateHashString(String.valueOf(destinationNode), PrimaryServerClass.getInstance().getLOG_N());
        this.optimizedHashValue = this.hashValue;
    }
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
        return destinationPath;
    }

    public String getHashValue() {
        return hashValue;
    }
}
