import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
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
    private int MAX_N= (1<<10);
    private int LOG_N = getLOG_N_BASE_2();
    private int NUMBER_OF_NODES = 0;
    private Utility utl = new Utility();
    public static PrimaryServerClass getInstance() {
        return ourInstance;
    }

    private PrimaryServerClass() {
        createSourceDirectory();
    }
    void addNode(int hash){
        if(NUMBER_OF_NODES>=MAX_N){
            return;
        }
        ActorRef temp = system.actorOf(Props.create(Node.class), String.valueOf(hash));
//        nodeList.put(Integer.valueOf(hash),temp);
        nodeList.put(hash,temp);
        NUMBER_OF_NODES+=1;
    }
    public void stopExecution(){
        System.out.println("Stopping to execute the program");
        stoppingExecution();
        System.exit(1);
    }
    private void stoppingExecution(){
        for(Map.Entry<Integer, ActorRef> entry :nodeList.entrySet()){
            system.stop(entry.getValue());
            nodeList.remove(entry.getKey());
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
    public void runInitialEntryPoint() throws IOException, NoSuchAlgorithmException {
        checkAndGenerateNode();
        getInstance().getNodeList().firstEntry().getValue().tell("runInGeneral", ActorRef.noSender());
    }
    public void checkAndGenerateNode() throws IOException, NoSuchAlgorithmException {
        String IP = utl.generateIP();
        int hash = utl.generateHashString(IP, LOG_N);
        if(getInstance().getNodeList().size()>0 && getNodeList().containsKey(Integer.valueOf(hash))){
            checkAndGenerateNode();
            return;
        }
        addNode(hash);
    }
    public void generateRandomServers(int numberOfServers) throws IOException, NoSuchAlgorithmException {
        for(int i=0; i<numberOfServers; i++){
            checkAndGenerateNode();
        }
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
        TimeUnit.SECONDS.sleep(1);
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
    public boolean checkServerExist(int serverKey){
        if(nodeList.containsKey(serverKey))
            return true;
        return false;
    }
    public void removeServerNode(int serverKey){
        nodeList.remove(serverKey);
        System.exit(1);
    }

}
