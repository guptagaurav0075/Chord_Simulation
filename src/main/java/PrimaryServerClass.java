import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by Gaurav on 11/10/17.
 */
public final class PrimaryServerClass {
    private static PrimaryServerClass ourInstance = new PrimaryServerClass();
    private ActorSystem system = ActorSystem.create("Chord_Algorithm");
    private TreeMap<Integer, ActorRef> nodeList = new TreeMap<>();
    private int MAX_N= 1024;
    private int LOG_N = getLOG_N_BASE_2();
    private int NUMBER_OF_NODES = 0;
    public static PrimaryServerClass getInstance() {
        return ourInstance;
    }

    private PrimaryServerClass() {
    }
    private int getLOG_N_BASE_2() {
        return (int) (Math.log(MAX_N) / Math.log(2));
    }
    void addNode(){
        if(NUMBER_OF_NODES>=MAX_N){
            return;
        }
        ActorRef temp = system.actorOf(Props.create(Node.class), Integer.toString(NUMBER_OF_NODES));
        nodeList.put(NUMBER_OF_NODES,temp);
        NUMBER_OF_NODES+=1;
    }
    public static void stopExecution(){
        System.out.println("Stopping to execute the program");
        getInstance().stoppingExecution();
    }
    private void stoppingExecution(){
        for(Map.Entry<Integer, ActorRef> entry :getInstance().getNodeList().entrySet()){
            getInstance().getSystem().stop(entry.getValue());
            getInstance().getNodeList().remove(entry.getKey());
        }
    }

    public static PrimaryServerClass getOurInstance() {
        return ourInstance;
    }

    public ActorSystem getSystem() {
        return system;
    }

    public TreeMap<Integer, ActorRef> getNodeList() {
        return nodeList;
    }

    public int getMAX_N() {
        return MAX_N;
    }

    public int getLOG_N() {
        return LOG_N;
    }

    public int getNUMBER_OF_NODES() {
        return NUMBER_OF_NODES;
    }
    public static void printFingerTables() throws InterruptedException {
        /*for (Map.Entry<Integer, ActorRef> entry : getInstance().getNodeList().entrySet()){
            System.out.println();
            System.out.println();
            entry.getValue().tell("printRoutingTable", ActorRef.noSender());
            TimeUnit.SECONDS.sleep(10);
        }*/

    }
    public static void returnFirstNodeAsServer(){
        getInstance().getNodeList().firstEntry().getValue().tell("runInGeneral", ActorRef.noSender());
    }
}
