import akka.actor.ActorRef;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.InputMismatchException;
import java.util.Scanner;
import java.util.concurrent.TimeUnit;
/**
 * Created by Gaurav on 11/10/17.
 */
class NodeAsServer {
    private int nodeKey;
    private ActorRef currentNode;
    Scanner in = new Scanner(System.in);

    public NodeAsServer(int nodeKey, ActorRef currentNode) {
        this.nodeKey = nodeKey;
        this.currentNode = currentNode;
    }

    public void runAsAdministrator() throws InterruptedException, IOException, NoSuchAlgorithmException {
        int choice=-1;
        do {
            System.out.println(" ******* Menu Items for Node :"+nodeKey+" *******");
            System.out.println("\t\t1--> Introduce new file to system.");
            System.out.println("\t\t2--> Find file with a file name.");
            System.out.println("\t\t3--> Check number hops required to reach a particular Node.");
            System.out.println("\t\t4--> Print Finger Table.");
            System.out.println("\t\t5-->Exit from current Node as Server Node.");

            choice = in.nextInt();

            if(choice ==1||choice==2){
                handleFile(choice);
            }
            else if(choice==3){
                checkHops();
            }
            else if(choice==4){
                printFingerTable();
            }
            else if(choice==5){
                currentNode.tell("runInGeneral", ActorRef.noSender());
            }
            else {
                System.out.println("Kindly Enter a Valid Choice");
                currentNode.tell("useAsAdministrator", ActorRef.noSender());
            }
        }while (choice<1 && choice>5);
    }
    private void checkHops() throws InterruptedException {
        System.out.println("Enter the name of the Destination Node:");
        String destinationNode = in.next();
        System.out.println("Destination Node entered is :"+destinationNode);
        currentNode.tell(new DestinationNode(String.valueOf(nodeKey), destinationNode.trim(), 0, "CheckHopCount"), ActorRef.noSender());
    }
    private void printFingerTable() throws InterruptedException {
        System.out.println();
        currentNode.tell("printRoutingTable", ActorRef.noSender());
        System.out.println();
    }

    private void handleFile(int choice) throws IOException, NoSuchAlgorithmException, InterruptedException {
        Utility utl = new Utility();
        FileOperations fo;
        if(choice==1){
            System.out.println("\nKindly enter the name of the file with full path\n");
            String fileName = in.next();
            File file = new File(fileName.trim());
            String hashValue = String.valueOf(utl.generateHashString(file.getName(), PrimaryServerClass.getInstance().getLOG_N()));
            fo = new FileOperations(file.getName(), file.getAbsolutePath(), hashValue ,"Add", String.valueOf(nodeKey), hashValue);

        }else{
            System.out.println("\nKindly enter the name of the file you would like to search\n");
            String fileName = new String();
            try {
                fileName= in.next();
            }catch (InputMismatchException e){
                System.out.println("\n\nWrong input entered, try again!\n\n");
                currentNode.tell("useAsAdministrator", ActorRef.noSender());
            }
            fo = new FileOperations(fileName.trim(),String.valueOf(utl.generateHashString(fileName.trim(),PrimaryServerClass.getInstance().getLOG_N())), "Search", String.valueOf(nodeKey));
        }
        currentNode.tell(fo, ActorRef.noSender());
    }
    private void checkNodeExist(){
        File checkSourceDirectory = new File("ServerFiles");
        if(!checkSourceDirectory.exists()){
            checkSourceDirectory.mkdir();
        }
    }
    private void returnBackAsAdministrator() throws InterruptedException {
        TimeUnit.SECONDS.sleep(1);
        currentNode.tell("useAsAdministrator", ActorRef.noSender());
    }
}
