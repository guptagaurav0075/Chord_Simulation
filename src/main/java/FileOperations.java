import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gaurav on 11/11/17.
 */
public class FileOperations {
    private String fileName;
    private String sourcePath;
    private String sourceNode;
    private String destinationPath;
    private String destionationNode;
    private String hashValue;
    private String optimizedHashValue;
    private String purpose;
    private int predecessorOfSourceNode;
    private int successorOfSourceNode;

    public FileOperations(String destinationNode, String sourceNode, String sourceNodePath, String purpose, String hashValue, int predecessor, int successor) throws IOException, NoSuchAlgorithmException {
        this.sourcePath = sourceNodePath;
        this.sourceNode = sourceNode;
        this.destionationNode = destinationNode;
        this.purpose = purpose;
        this.hashValue = String.valueOf(destinationNode);
        this.optimizedHashValue = this.hashValue;
        this.predecessorOfSourceNode = predecessor;
        this.successorOfSourceNode = successor;
    }

    public FileOperations(String fileName, String hashValue, String purpose, String sourceNode) {
        this.fileName = fileName;
        this.hashValue = hashValue;
        this.purpose = purpose;
        this.optimizedHashValue = this.hashValue;
        this.sourceNode = sourceNode;
    }

    public int getPredecessorOfSourceNode() {
        return predecessorOfSourceNode;
    }

    public int getSuccessorOfSourceNode() {
        return successorOfSourceNode;
    }
    public String getSourceNode() {
        return sourceNode;
    }

    public String getDestionationNode() {
        return destionationNode;
    }

    public FileOperations(String fileName, String sourcePath, String hashValue, String purpose, String sourceNode, String optimizedHashValue) {
        this.fileName = fileName;
        this.sourcePath = sourcePath;
        this.hashValue = hashValue;
        this.purpose = purpose;
        this.optimizedHashValue  = optimizedHashValue;
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

    public void printFileDesctiption(){
        System.out.println("Source Node :"+sourceNode);
        System.out.println("Predecessor Node :"+predecessorOfSourceNode);
        System.out.println("Successor Node :"+successorOfSourceNode);

    }
}
