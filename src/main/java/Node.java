import akka.actor.ActorRef;
import akka.actor.UntypedAbstractActor;
import scala.Int;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gaurav on 11/8/17.
 */
public class Node extends UntypedAbstractActor{
    private RoutingTable fingerTable;
    String name;
    Scanner in = new Scanner(System.in);
    NodeFileOperations nfo;
    public Node() throws IOException, NoSuchAlgorithmException {
        name = self().path().name();
        System.out.println("Creating New Node with key :"+name);
        this.nfo = new NodeFileOperations(name);
        fingerTable = new RoutingTable(name, getSelf());
        fingerTable.informActorsToUpdateRoutingTable();
//        fingerTable.loadBalance(nfo);
    }
    @Override
    public void onReceive(Object msg) throws Throwable {
        if(msg instanceof String){
            if(msg.equals("updateFingerTable")) {
                fingerTable.updateFingerTable();
            }
            else if(msg.equals("useAsAdministrator")){
                NodeAsServer runAsAdmin = new NodeAsServer(Integer.valueOf(name), getSelf());
                runAsAdmin.runAsAdministrator();
            }
            else if(msg.equals("runInGeneral")){
                System.out.println();
                System.out.println("Currently the server node is :"+name);
                System.out.println();
                runInGeneral();
            }
            else if(msg.equals("printRoutingTable")){
                fingerTable.printFingerTable();
                getSelf().tell("useAsAdministrator", ActorRef.noSender());
            }
        }
        else if(msg instanceof DestinationNode){
            if(((DestinationNode) msg).getDestinationNode().equals(name)){
                if(((DestinationNode) msg).getPurpose().equals("RunAsServer")){
                    getSelf().tell("runInGeneral", ActorRef.noSender());
                }else if(((DestinationNode) msg).getPurpose().equals("DoneLoadBalance")){
                    nfo.doneLoadBalance();
//                    return;
                    getSelf().tell("runInGeneral", ActorRef.noSender());
                }else if(((DestinationNode) msg).getPurpose().equals("CheckHopCount")){
                    System.out.println("Number Of Hops to Reach from source node: "+((DestinationNode) msg).getSourceNode()+" to destination node: "+((DestinationNode) msg).getDestinationNode()+" is "+((DestinationNode) msg).getHopCount());
                    runAsServerChoice(name, ((DestinationNode) msg).getSourceNode());
                }
            }else if( ((DestinationNode) msg).getHopCount()>PrimaryServerClass.getInstance().getLOG_N() || ((DestinationNode) msg).getHopCount()>PrimaryServerClass.getInstance().getNUMBER_OF_NODES()-1){
                System.out.println("No such Destination Node exist");
                System.out.println("Returning to the source node");
                runAsServerChoice(name, ((DestinationNode) msg).getSourceNode());
            }
            else{
                ((DestinationNode) msg).incrementHopCount();
                fingerTable.getNearestActorFromKey(Integer.parseInt(((DestinationNode) msg).getDestinationNode())).tell(msg, ActorRef.noSender());
                TimeUnit.SECONDS.sleep(1);
            }
        }
        else if(msg instanceof FileOperations){
            if(((FileOperations) msg).getOptimizedHashValue().equals(name)){
                if(((FileOperations) msg).getPurpose().equals("Add")){
                    System.out.println("Storing the file on node :"+name);
                    nfo.addFile((FileOperations) msg);
                }else if(((FileOperations) msg).getPurpose().equals("Search")){
                    nfo.searchFile((FileOperations) msg);
                }
                else if(((FileOperations) msg).getPurpose().equals("LoadBalance")){
//                    System.out.println("Load Balancing called for Current Key :"+name);
//                    System.out.println("Source Node"+((FileOperations) msg).getSourceNode());
//                    System.out.println("Souce Path"+((FileOperations) msg).getSourcePath());
                    loadBalance((FileOperations) msg);
                    return;
                }
                runAsServerChoice(name, ((FileOperations) msg).getSourceNode());
            }
            else if(fingerTable.fingerTable.size()==0){
                ((FileOperations) msg).setOptimizedHashValue(name);
                getSelf().tell(msg, ActorRef.noSender());
            }
            else if(fingerTable.isResponsibleForActorForKey(Integer.valueOf(((FileOperations) msg).getOptimizedHashValue()))!=-1){
                ((FileOperations) msg).setOptimizedHashValue(String.valueOf(fingerTable.isResponsibleForActorForKey(Integer.valueOf(((FileOperations) msg).getOptimizedHashValue()))));
                fingerTable.getNearestActorFromKey(Integer.parseInt(((FileOperations) msg).getOptimizedHashValue())).tell(msg, ActorRef.noSender());
            }
            else{
                fingerTable.getNearestActorFromKey(Integer.parseInt(((FileOperations) msg).getOptimizedHashValue())).tell(msg, ActorRef.noSender());
                TimeUnit.SECONDS.sleep(1);
            }
        }
    }
    private void loadBalance(FileOperations msg) throws IOException, NoSuchAlgorithmException {
        nfo.transferFiles(msg);
//        System.out.println("In Load Balance Function");
        DestinationNode temp = new DestinationNode(name,msg.getSourceNode(),0,"DoneLoadBalance");
        getSelf().tell(temp, ActorRef.noSender());

    }
    private void runInGeneral() throws IOException, NoSuchAlgorithmException {
        int choice = 0;
        do{
            System.out.println("**** Following are the Operations that could be performed ****");
            System.out.println("\t\t1->\tSelect a node to \"Run As Server.\"");
            System.out.println("\t\t2->\tUse current node as administrator");
            System.out.println("\t\t3->\tAdd new Node(s) to the Server");
            System.out.println("\t\t4->\tRemove Node(s) from the Server");
            System.out.println("\t\t5->\tStop Executing The Server.");
            choice = in.nextInt();
            if(choice==1){
                runAsServerChoice();
            }else if (choice==2){
                getSelf().tell("useAsAdministrator", ActorRef.noSender());
            }else if(choice==3){
                addMoreNodes();
                getSelf().tell("runInGeneral", ActorRef.noSender());
            }
            else if(choice==4){
                removeNodes();
            }
            else if(choice==5){
                PrimaryServerClass.getInstance().stopExecution();
                break;
            }
            else{
                System.out.println("Kindly Enter a Valid Choice");
                getSelf().tell("runInGeneral", ActorRef.noSender());
            }
        }while (choice<1 && choice>5);
    }

    private void removeNodes() {
        System.out.println("\nSelect one of the choices mentioned below\n");
        System.out.println("\t\t1->Remove a specific server");
        System.out.println("\t\t2->Remove a random server");
        int choice = in.nextInt();
        if(choice == 1 ) {
            System.out.println("Enter the name of the server you would like to remove");
            int serverName = in.nextInt();
            boolean status = PrimaryServerClass.getInstance().checkServerExist(serverName);
            if(status && !name.equals(String.valueOf(serverName))){ // given that the server exist and is not the current server
                PrimaryServerClass.getInstance().removeServerNode(serverName);
            }else{
                System.out.println("No such server exist");
            }
        }else if(choice==2){
            removeRandomNode();
        }else{
            System.out.println("Enter a valid choice");
            removeNodes();
        }
    }

    private void removeRandomNode() {
        int randomNode = 0+ (int) (Math.random() * PrimaryServerClass.getInstance().getNUMBER_OF_NODES());
        if(randomNode == Integer.valueOf(name)){
            removeRandomNode();
        }else{
            PrimaryServerClass.getInstance().removeServerNode(randomNode);
        }
    }


    private void addMoreNodes() throws IOException, NoSuchAlgorithmException {
        System.out.println("Select one of the choices mentioned below\n");
        System.out.println("\t\t1->\tAdd single node");
        System.out.println("\t\t2->\tAdd multiple node");
        int choice = in.nextInt();
        if(choice==1) {
            System.out.println();
            addNodes(1);
        }else if(choice==2){
            System.out.println("Enter the number of nodes to run\n");
            int numOfNodes = in.nextInt();
            addNodes(numOfNodes);
        }
        else{
            addMoreNodes();
        }
        return;
    }
    private void addNodes(int numOfNodes) throws IOException, NoSuchAlgorithmException {
        for (int i = 0; i < numOfNodes; i++) {
            PrimaryServerClass.getInstance().checkAndGenerateNode();
        }
    }

    private void runAsServerChoice(){
        System.out.println("Enter the node which shall be run as server");
        String destinationNode = in.next();
        DestinationNode temp = new DestinationNode(name, destinationNode.trim(),0, "RunAsServer" );
        getSelf().tell(temp, ActorRef.noSender());
    }
    private void runAsServerChoice(String sourceNode, String destinationNode){
        DestinationNode temp = new DestinationNode(sourceNode, destinationNode,0, "RunAsServer" );
        getSelf().tell(temp, ActorRef.noSender());
    }

    public RoutingTable getFingerTable() {
        return fingerTable;
    }

    public String getName() {
        return name;
    }
}
