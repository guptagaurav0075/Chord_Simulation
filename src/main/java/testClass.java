import java.io.IOException;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gaurav on 11/10/17.
 */
public class testClass {
    public static void main(String[] args) throws InterruptedException, IOException, NoSuchAlgorithmException {
        PrimaryServerClass PSC = PrimaryServerClass.getInstance();
        PSC.runInitialEntryPoint();

//       @Deprecated
        /*for (int i = 0; i < 1024; i++) {
            PSC.addNode();
        }
        PSC.returnFirstNodeAsServer();*/
    }
}
