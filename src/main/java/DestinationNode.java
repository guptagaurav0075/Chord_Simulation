/**
 * Created by Gaurav on 11/10/17.
 */
class DestinationNode{
    private String sourceNode;
    private String destinationNode;
    private int hopCount = 0;
    String purpose;
    public DestinationNode(String sourceNode, String destinationNode, int hopCount, String purpose) {
        this.sourceNode = sourceNode;
        this.destinationNode = destinationNode;
        this.hopCount = hopCount;
        this.purpose = purpose;
    }
    public void incrementHopCount() {
        this.hopCount +=1;
    }

    public String getSourceNode() {
        return sourceNode;
    }

    public String getDestinationNode() {
        return destinationNode;
    }

    public int getHopCount() {
        return hopCount;
    }

    public String getPurpose() {
        return purpose;
    }
}
