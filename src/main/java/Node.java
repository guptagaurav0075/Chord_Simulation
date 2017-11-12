import akka.actor.ActorRef;
import akka.actor.UntypedAbstractActor;
import scala.Int;

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
    public Node() {
        name = self().path().name();
        System.out.println("Creating New Node with key :"+name);
        this.nfo = new NodeFileOperations(name);
        fingerTable = new RoutingTable(name);
        fingerTable.informActorsToUpdateRoutingTable();
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
            }
        }
        else if(msg instanceof DestinationNode){
            if(((DestinationNode) msg).getDestinationNode().equals(name)){
                System.out.println("Number Of Hops to Reach from source node: "+((DestinationNode) msg).getSourceNode()+" to destination node: "+((DestinationNode) msg).getDestinationNode()+" is "+((DestinationNode) msg).getHopCount());
                if(((DestinationNode) msg).getPurpose().equals("RunAsServer")){
                    getSelf().tell("runInGeneral", ActorRef.noSender());
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
                runAsServerChoice(name, ((FileOperations) msg).getSourceNode());
            }else if(fingerTable.isResponsibleForActorForKey(Integer.valueOf(((FileOperations) msg).getHashValue()))!=-1){
                ((FileOperations) msg).setOptimizedHashValue(String.valueOf(fingerTable.isResponsibleForActorForKey(Integer.valueOf(((FileOperations) msg).getHashValue()))));
                fingerTable.getNearestActorFromKey(Integer.parseInt(((FileOperations) msg).getOptimizedHashValue())).tell(msg, ActorRef.noSender());
            }
            else{
                fingerTable.getNearestActorFromKey(Integer.parseInt(((FileOperations) msg).getOptimizedHashValue())).tell(msg, ActorRef.noSender());
                TimeUnit.SECONDS.sleep(1);
            }
        }
    }
    private void runInGeneral(){
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

            }
            else if(choice==4){

            }
            else if(choice==5){
                PrimaryServerClass.stopExecution();
                System.exit(1);
                break;
            }
            else{
                System.out.println("Kindly Enter a Valid Choice");
                getSelf().tell("runInGeneral", ActorRef.noSender());
            }
        }while (choice<1 && choice>5);
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
