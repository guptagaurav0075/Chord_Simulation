import akka.actor.ActorRef;
import akka.japi.pf.FI;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Created on: 11/9/17.
 *
 * Description: RoutingTable.java hold the functions and parameter's that are useful in generation of
 *              finger table for a particular node, finding predecessor and successor nodes, managing the load balancing operation
 *              when a new node is added, printing the finger table for a particular node, informing all the predecessor node regarding
 *              updating their finger table when a new node is added to the server system.
 *
 */


public class RoutingTable {
    TreeMap<Integer, ActorRef> fingerTable; //fingerTable hold the information regarding all the nodes that a particular node maps to.
    int currentNode; // current node specifies the node name for the particular node.
    ActorRef selfNodeRef; // Variable provides a self reference to the current node.
    private PrimaryServerClass PSC = PrimaryServerClass.getInstance(); // PSC hold the instance for super server class.


    /*
    RoutingTable Constructor in used to generate the routing table for a node when a new node is added,
    also it initializes all the variables current node, selfReferencing node, and finger table.
     */
    public RoutingTable(String actorName, ActorRef selfNodeRef) {
        this.currentNode = Integer.valueOf(actorName);
        this.fingerTable = new TreeMap<Integer, ActorRef>();
        this.selfNodeRef = selfNodeRef;
        GenerateRoutingTable();
    }

    /*
    GenerateRoutingTable is used to generate the routing table for a given node, This function is called from the constructor of the
    RoutingTable class.
     */
    private void GenerateRoutingTable(){
        if(PSC.getNUMBER_OF_NODES()-1<=0)
            return;
        int index=1;
        while (index<PSC.getNUMBER_OF_NODES() && index<=PSC.getLOG_N()){
            int max_N = PSC.getMAX_N();
            int nodeName = (currentNode+((int) Math.pow(2, index-1)))% max_N;
            getNextEntry(nodeName, PSC, max_N);
            index+=1;
        }
    }

    /*
    getNextEntry is used to find the nextEntry in the server system for a particular node value,
    for example: if the current node is 2 and there is only one server that is already present, let say node 7.
                 Then, in order to map the the finger table for node 2, the next node should be 7.
                 Since mapping following the rule that the (current node+2 power 0) should map to 3 rather 7,
                 this function helps in resolving that issue.
     */
    private void getNextEntry(int nodeName, PrimaryServerClass PSC, int max_N){
        Entry<Integer, ActorRef> nextEntry = PSC.getNodeList().ceilingEntry(nodeName);
        if(nextEntry!=null &&!fingerTable.containsKey(nextEntry.getKey()) && nextEntry.getKey()!=this.currentNode){
            fingerTable.put(nextEntry.getKey(), nextEntry.getValue());
        }else if(nextEntry!= null && (fingerTable.containsKey(nextEntry.getKey()) || nextEntry.getKey()!=this.currentNode)) {
            getNextEntry(nextEntry.getKey()+1, PSC, max_N);
        }else if(nextEntry==null){
            if(nodeName!=0) {
                getNextEntry((0) % max_N, PSC, max_N);
            }else{
                getNextEntry(1%max_N, PSC, max_N);
            }
        }
    }

    /*
    printFingerTable function helps in printing the finger table for the current node.
     */
    public void printFingerTable(){
        System.out.println("\n\n****Finger Table for Node:"+currentNode+" is :********");

        for (Entry<Integer, ActorRef> actor : fingerTable.entrySet()){
            System.out.println("\t\tcurrent node: "+currentNode+"\tMapped Node :"+actor.getKey());
        }
        System.out.println("\n\n");
    }

    /*
    updateFingerTable is called when a new node joins the system.
     */
    public void updateFingerTable(){
        fingerTable = new TreeMap<>();
        GenerateRoutingTable();
    }

    /*
    informActorsToUpdateRoutingTable function is used when a new node is generated, the other nodes that shall map to the current
    node are informed regarding joining the current node.
    for example:
                if the server has node 2 and a new node 3 or 4 or 6 joins the system, then node 2 shall be informed to update
                its routing table, so that it maps all nodes in it's finger table properly.
     */
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

    /*
    findNearestNodeKey function helps in searching a node, if the given node is present in the finger table of the current node
    then that node with key is returned, if the node is not present in the current system, then the node that is nearest to the node that is being
    searched for is returned. If the finger table is empty then the node no such neighbour exist.
     */
    public int findNeareastNodeKey(int nodeToSearch){
        if(fingerTable.size()>0) {
            if (fingerTable.floorKey(nodeToSearch) != null) {
                return fingerTable.floorKey(nodeToSearch);
            }
            return fingerTable.lastKey();
        }
        return -1;
    }

    /*
    findNearestActorFromKey performs similar kind of operation like findNearestNodeKey function.
    The findNearestActorFromKey would return the actor that is closest to the node that is being searched for.
    if node that is being searched for exist then that node is returned otherwise the node that is closest to the node being searched is returned
    if the finger table has no entries then it returns false.
     */
    public ActorRef getNearestActorFromKey(int key){
        if(fingerTable.size()>0){
            return fingerTable.get(findNeareastNodeKey(key));
        }
        return null;
    }

    /*
    isResponsibleForActorForKey checks that the node with key that is being searched is any power of 2 for the current node, if it is then
    the current node is responsible for the node, otherwise, the current node is not responsible for the current node.
     */
    public int isResponsibleForActorForKey(int key){
        int index = 0;
        int maxIteration = PSC.getLOG_N();
        while (index<maxIteration){
            int node = (currentNode + ((int) Math.pow(2, index)))%PSC.getMAX_N();
            if(node==key)
                return successorKey(key);
            index+=1;
        }
        return -1;
    }


    /*
    successorKey function will find the node A that is next node for a given node A, if the Node A, is present in the finger table
     then the node A is returned otherwise node X is returned.
     */
    private int successorKey(int key) {
        if(fingerTable.ceilingEntry(key)!=null){
            return fingerTable.ceilingKey(key);
        }
        return fingerTable.firstKey();
    }

    /*
    predecessorKey function will find the node A that is previous node for a given node A, if the Node A, is present in the finger table
    then the node A is returned otherwise node X is returned.
     */
    private int predecessorKey(int key) {
        if(fingerTable.floorEntry(key)!=null){
            return fingerTable.floorKey(key);
        }
        return fingerTable.lastKey();
    }

    /*
    successorNode function will return the successor node for a give Node A with identifier as key, if the key is present in the finger table
    then the node is returned; otherwise the successor node for a given key is returned.
     */
    public ActorRef successorNode(int key){
        if(fingerTable.ceilingEntry(key)!=null){
            return fingerTable.ceilingEntry(key).getValue();
        }
        return fingerTable.firstEntry().getValue();
    }

    /*
    getActorRefFromKey function will return the reference for the actor provided the key is present in the finger table.
     */
    public ActorRef getActorRefFromKey(int key){
        if(fingerTable.containsKey(key))
            return fingerTable.get(key);
        return null;
    }

    /*
    loadBalance is called when the node is created it helps in balancing the load provided among the nodes in the server.
     */
    public void loadBalance(NodeFileOperations nfo) throws IOException, NoSuchAlgorithmException, InterruptedException {
        //function checks if there are any file on the server that were supposed to be for the current server
        if(fingerTable.size()<=0){
            return;
        }
        int next = (currentNode + 1)%PSC.getMAX_N();
        Entry<Integer, ActorRef> succ_Entry = fingerTable.ceilingEntry(next);
        if(succ_Entry==null){
            succ_Entry= fingerTable.firstEntry();
//            loadBalanceInternal(nfo, succ_Entry, "PredecessorLoadBalance");
        }/*else{
            loadBalanceInternal(nfo, succ_Entry, "SuccessorLoadBalance");
        }*/
        System.out.println("Successor Node is :"+succ_Entry.getKey());
        loadBalanceInternal(nfo, succ_Entry, "SuccessorLoadBalance");

        /*next = (currentNode - 1)%PSC.getMAX_N();
        Entry<Integer, ActorRef> pred_Entry = fingerTable.floorEntry(next);
        boolean isPredecessor =true;
        if(pred_Entry==null){
            pred_Entry = fingerTable.lastEntry();
            isPredecessor = false;
        }
        if(pred_Entry.getKey()!=succ_Entry.getKey()) {
            if (!isPredecessor)
                loadBalanceInternal(nfo, pred_Entry, "SuccessorLoadBalance");
            else
                loadBalanceInternal(nfo, pred_Entry, "PredecessorLoadBalance");
        }*/
    }

    /*
    loadBalanceInternal is called from loadBalance function, it manges the call to be made to the proper node.
     */
    private void loadBalanceInternal(NodeFileOperations nfo, Entry<Integer, ActorRef> entry, String purpose) throws IOException, NoSuchAlgorithmException, InterruptedException {
        FileOperations msg = new FileOperations(String.valueOf(entry.getKey()), String.valueOf(currentNode), nfo.directoryPath, purpose, String.valueOf(entry.getKey()), predecessorKey(currentNode), successorKey(currentNode));
//        System.out.println("\n**** In Routing Table File->loadBalanceInternalFunction ***");
//        System.out.println("Current Node: "+currentNode+"\nPredecessor Node: "+predecessorKey(currentNode)+"\n Successor Node"+successorKey(currentNode));
        entry.getValue().tell(msg, ActorRef.noSender());
        TimeUnit.SECONDS.sleep(4);
    }
}
