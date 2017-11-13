import akka.actor.ActorRef;
import akka.japi.pf.FI;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

/**
 * Created by Gaurav on 11/9/17.
 */
public class RoutingTable {
    TreeMap<Integer, ActorRef> fingerTable;
    int currentNode;
    ActorRef selfNodeRef;
    private PrimaryServerClass PSC = PrimaryServerClass.getInstance();


    public RoutingTable(String actorName, ActorRef selfNodeRef) {
        this.currentNode = Integer.valueOf(actorName);
        this.fingerTable = new TreeMap<Integer, ActorRef>();
        this.selfNodeRef = selfNodeRef;
        GenerateRoutingTable();
    }
    private void GenerateRoutingTable(){
        if(PSC.getNUMBER_OF_NODES()-1<0)
            return;
        int index=1;
        while (index<PSC.getNUMBER_OF_NODES() && index<=PSC.getLOG_N()){
            int max_N = PSC.getMAX_N();
            int nodeName = (currentNode+((int) Math.pow(2, index-1)))% max_N;
            getNextEntry(nodeName, PSC, max_N);
            index+=1;
        }
//        printFingerTable();
    }
    private void getNextEntry(int nodeName, PrimaryServerClass PSC, int max_N){
        Entry<Integer, ActorRef> nextEntry = PSC.getNodeList().ceilingEntry(nodeName);
        if(nextEntry!=null &&  !fingerTable.containsKey(nextEntry.getKey()) && nextEntry.getKey()!=this.currentNode){
            fingerTable.put(nextEntry.getKey(), nextEntry.getValue());
        }else{
            if(nodeName!=0) {
                getNextEntry((0) % max_N, PSC, max_N);
            }
        }
    }
    public void printFingerTable(){
        System.out.println("****Finger Table for Node:"+currentNode+" is :********");

        for (Entry<Integer, ActorRef> actor : fingerTable.entrySet()){
            System.out.println("\t\tcurrent node: "+currentNode+"\tMapped Node :"+actor.getKey());
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
        while (index<PSC.getNUMBER_OF_NODES() && index<=PSC.getLOG_N()){
            int node = currentNode - ((int) Math.pow(2, index-1));
            if(node<0){
                node = PSC.getMAX_N()-node;
            }
            Entry<Integer, ActorRef> nodeToInform = PSC.getNodeList().floorEntry(node);
            if(nodeToInform!=null && nodeToInform.getKey()!=currentNode){
//                System.out.println("Informing actor with key :"+nodeToInform.getKey()+" to update!");
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
    public ActorRef getNearestActorFromKey(int key){
        if(fingerTable.size()>0){
            return fingerTable.get(findNeareastNodeKey(key));
        }
        return null;
    }
    public int isResponsibleForActorForKey(int key){
        int index = 0;
        int maxIteration = PSC.getLOG_N();
        while (index<maxIteration){
            int node = (currentNode + ((int) Math.pow(2, index)))%PSC.getMAX_N();
            if(index==key)
                return successorKey(key);
            index+=1;
        }
        return -1;
    }

    private int successorKey(int key) {
        if(fingerTable.ceilingEntry(key)!=null){
            return fingerTable.ceilingKey(key);
        }
        return fingerTable.firstKey();
    }

    public ActorRef successorNode(int key){
        if(fingerTable.ceilingEntry(key)!=null){
            return fingerTable.ceilingEntry(key).getValue();
        }
        return fingerTable.firstEntry().getValue();
    }
    public ActorRef getActorRefFromKey(int key){
        if(fingerTable.containsKey(key))
            return fingerTable.get(key);
        return null;
    }
    public void loadBalance(NodeFileOperations nfo) throws IOException, NoSuchAlgorithmException {
        //function checks if there are any file on the server that were supposed to be for the current server
        if(fingerTable.size()>0){
            int next = (currentNode + 1)%PSC.getMAX_N();
            Entry<Integer, ActorRef> succ_Entry = fingerTable.ceilingEntry(next);
            if(succ_Entry==null){
                succ_Entry= fingerTable.firstEntry();
            }
            loadBalanceInternal(nfo, succ_Entry, "SuccessorLoadBalance");

            next = (currentNode - 1)%PSC.getMAX_N();
            Entry<Integer, ActorRef> pred_Entry = fingerTable.floorEntry(next);
            if(pred_Entry==null){
                pred_Entry = fingerTable.lastEntry();
            }
            if(pred_Entry.getKey()!=succ_Entry.getKey())
                loadBalanceInternal(nfo, pred_Entry, "PredecessorLoadBalance");
        }
    }
    private void loadBalanceInternal(NodeFileOperations nfo, Entry<Integer, ActorRef> entry, String purpose) throws IOException, NoSuchAlgorithmException {
        FileOperations msg = new FileOperations(entry.getKey(), String.valueOf(currentNode), nfo.directoryPath, purpose);
        entry.getValue().tell(msg, ActorRef.noSender());
    }
}
