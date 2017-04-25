import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;

/**
 * Created by evgeniyh on 24/04/17.
 */
public class Test {
    public static void main(String[] args) {
        try {
            Class.forName("org.postgresql.Driver");
            Connection con = DriverManager.getConnection("INSERT_CONNECTION_STRING");
            PreparedStatement p = con.prepareStatement("INSERT INTO TEST_TABLE VALUES (?, ?);");
            p.setInt(1, 2);
            p.setString(2, "test_message");
            int insertRows = p.executeUpdate();
            if (insertRows == 1) {
                System.out.println("The row inserted successfully");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
