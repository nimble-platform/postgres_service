/**
 * Created by evgeniyh on 24/04/17.
 */
public class Test {
    public static void main(String[] args) {

        System.out.println(uri.getHost());
        System.out.println(uri.getUserInfo());
        System.out.println(uri.getAuthority());
        System.out.println(uri.getPort());

        String[] credentials = uri.getRawUserInfo().split(":");
        System.out.println(credentials[0]);
        System.out.println(credentials[1]);
        

    }
}
