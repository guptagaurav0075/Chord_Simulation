import akka.actor.ActorRef;

import java.util.Map;
import java.util.TreeMap;

/**
 * Created by Gaurav on 11/9/17.
 */
public class RoutingTable {
    TreeMap<Integer, ActorRef> fingerTable;
    int currentNode;
    private PrimaryServerClass PSC = PrimaryServerClass.getInstance();


    public RoutingTable(String actorName) {
        this.currentNode = Integer.valueOf(actorName);
        this.fingerTable = new TreeMap<Integer, ActorRef>();
        GenerateRoutingTable();
    }
    private void GenerateRoutingTable(){
        if(PSC.getNUMBER_OF_NODES()-1<0)
            return;
        int index=1;
        while (index<PSC.getNUMBER_OF_NODES() && index<=PSC.getLOG_N()){
            int max_N = PSC.getMAX_N();
            int nodeName =((int) Math.pow(2, index-1))% max_N;
            getNextEntry(nodeName, PSC, max_N);
            index+=1;
        }
        printFingerTable();

    }
    private void printFingerTable(){
        System.out.println();
        for (Map.Entry<Integer, ActorRef> actor : fingerTable.entrySet()){
            System.out.println("Finger Table\n current node: "+currentNode+"\tKey :"+actor.getKey());
        }
    }
    private void getNextEntry(int nodeName, PrimaryServerClass PSC, int max_N){
        Map.Entry<Integer, ActorRef> nextEntry = PSC.getNodeList().ceilingEntry(nodeName);
        if(nextEntry!=null &&  !fingerTable.containsKey(nextEntry.getKey()) && nextEntry.getKey()!=this.currentNode){
            fingerTable.put(nextEntry.getKey(), nextEntry.getValue());
        }else{
            getNextEntry((nodeName+1)%max_N, PSC, max_N);
        }
    }
    public void updateFingerTable(){
        /*if(!fingerTable.containsKey(n)){
            fingerTable.put(n,PSC.getNodeList().get(n));
            removeFromFingerTable();
        }*/
        fingerTable = new TreeMap<>();
        GenerateRoutingTable();
    }
    public void informActorsToUpdateRoutingTable(){
        int index=1;
        int node = currentNode;
        while (index<PSC.getNUMBER_OF_NODES() && index<=PSC.getLOG_N()){
            node = node - ((int) Math.pow(2, index-1));
            if(node<0){
                node = PSC.getNUMBER_OF_NODES()-node;
            }
            Map.Entry<Integer, ActorRef> nodeToInform = PSC.getNodeList().floorEntry(node);
            if(nodeToInform!=null && nodeToInform.getKey()!=currentNode){
                System.out.println("Informing actor with key :"+nodeToInform.getKey()+" to update!");
                nodeToInform.getValue().tell("updateFingerTable", ActorRef.noSender());
            }
            index+=1;
        }
    }
    public int findNeareastNodeKey(int nodeToSearch){
        if(fingerTable.size()>0) {
            if (fingerTable.floorKey(nodeToSearch) != null) {
                return fingerTable.floorKey(nodeToSearch);
            }
            return fingerTable.lastKey();
        }
        return -1;
    }
    public ActorRef getActorRefFromKey(int key){
        if(fingerTable.containsKey(key))
            return fingerTable.get(key);
        return null;
    }
    /*private void removeFromFingerTable(){
        if(fingerTable.size()<=PSC.getLOG_N()){
            return;
        }
        int index=0;
        while (index<PSC.getLOG_N()){
            int key = (currentNode+ (int) Math.pow(2, index))%PSC.getMAX_N();
            if(true){

            }
        }
    }*/
}
