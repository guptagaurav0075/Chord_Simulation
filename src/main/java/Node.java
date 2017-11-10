import akka.actor.UntypedAbstractActor;

/**
 * Created by Gaurav on 11/8/17.
 */
public class Node extends UntypedAbstractActor{
    RoutingTable fingerTable;
    String name;
    public Node() {
        name = self().path().name();
        System.out.println("Creating New Node with key :"+name);
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

            }
        }

    }
}
