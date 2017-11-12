import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.File;
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
    private int MAX_N= (1<<3);
    private int LOG_N = getLOG_N_BASE_2();
    private int NUMBER_OF_NODES = 0;
    public static PrimaryServerClass getInstance() {
        return ourInstance;
    }

    private PrimaryServerClass() {
        createSourceDirectory();
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



    private void createSourceDirectory(){

        File checkSourceDirectory = new File("ServerFiles");
        if(!checkSourceDirectory.exists()){
            checkSourceDirectory.mkdir();
        }
    }
    public String getSourceDirectoryPath(){
        return (new File("ServerFiles")).getAbsolutePath();
    }

    public static void returnFirstNodeAsServer() throws InterruptedException {
        TimeUnit.SECONDS.sleep(10);
        getInstance().getNodeList().firstEntry().getValue().tell("runInGeneral", ActorRef.noSender());
    }
    private int getLOG_N_BASE_2() {
        int power = 0;
        int index = 0;
        while (power < MAX_N) {
            index += 1;
            power = (int) Math.pow(2, index);
        }
        return index;
    }

}
