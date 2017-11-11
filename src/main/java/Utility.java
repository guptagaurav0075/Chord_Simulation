import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

public class Utility {
    public String generateHashString(String message, int mBits) throws NoSuchAlgorithmException, IOException {
        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        byte[] mdbytes = digest.digest(message.getBytes("UTF-8"));
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < mdbytes.length; i++) {
            sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 10).substring(1));
        }
        int numOfChar = numOfCharacters(mBits);
        System.out.println(sb.toString().substring(0, numOfChar));
        return sb.toString().substring(0, numOfChar);
    }
    private int numOfCharacters(int mBits){
        int val = (int) Math.pow(2, mBits);
        return String.valueOf(val).length();
    }
    public String generateIP(){
        Random r = new Random();
        return r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256) + "." + r.nextInt(256);
    }
}
