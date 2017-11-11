/**
 * Created by Gaurav on 11/10/17.
 */
public class testClass {
    public static void main(String[] args) throws InterruptedException {
        PrimaryServerClass PSC = PrimaryServerClass.getInstance();
        for (int i = 0; i < 1024; i++) {
            PSC.addNode();
        }
        PSC.returnFirstNodeAsServer();
    }
}
