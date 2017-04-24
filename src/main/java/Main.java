import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.postgresql.Driver;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

/**
 * Created by evgeniyh on 24/04/17.
 */
@ApplicationPath("/")
@Path("/")
public class Main extends Application {
    private final static Logger logger = Logger.getLogger(Main.class);
    private static String url;
    private static String user;
    private static String password;

    static {
        String vcapServices = System.getenv("VCAP_SERVICES");
        if (vcapServices == null) {
            logger.error("Vcap services is null - the app isn't running im bluemix");
        }
        if (vcapServices != null) {
            JsonObject jsonObject = (JsonObject) (new JsonParser().parse(vcapServices));
            JsonArray elephantBind = jsonObject.getAsJsonArray("elephantsql");
            if (elephantBind == null) {
                logger.error("Couldn't find elephantSql key in vcap services env variable");
            } else {
                JsonObject credentials = elephantBind.get(0).getAsJsonObject().get("credentials").getAsJsonObject();
                logger.info("Initialising credentials");
                System.out.println(credentials.toString());
                String uri = credentials.get("uri").getAsString();
                url = setCredentials(uri);
                try {
                    Class.forName("org.postgresql.Driver");
                    logger.info("Postgres driver exists");
                } catch (java.lang.ClassNotFoundException e) {
                    System.out.println("Could not find the JDBC driver!");
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    private static void setCredentials(String uri) {
        URI uri = URI.create("postgres://bnxkduhy:SHIq7uaDh41r-TwVD9WBTrGC2vk-VqHw@qdjjtnkv.db.elephantsql.com:5432/bnxkduhy");

        System.out.println(uri.getHost());
        System.out.println(uri.getUserInfo());
        System.out.println(uri.getAuthority());
        System.out.println(uri.getPort());

        String[] credentials = uri.getRawUserInfo().split(":");
        System.out.println(credentials[0]);
        System.out.println(credentials[1]);

    }

    @GET
    @Path("/data")
    public String printAllData() {
        try {
            Driver driver = new Driver();
            if (driver.acceptsURL(url)) {
                logger.info("SUCCESS " + url);
            } else {
                logger.error("Doesn't accept " + url);
            }

            url = "postgres://qdjjtnkv.db.elephantsql.com:5432/";
            if (driver.acceptsURL(url)) {
                logger.info("SUCCESS" + url);
            }


            url = "jdbc:postgresql://qdjjtnkv.db.elephantsql.com:5432/bnxkduhy";
            Properties props = new Properties();
            props.setProperty("user", "bnxkduhy");
            props.setProperty("password", "SHIq7uaDh41r-TwVD9WBTrGC2vk-VqHw");
            props.setProperty("ssl", "false");

            if (driver.acceptsURL(url)) {
                logger.info("SUCCESS" + url);
            }

            Connection db = DriverManager.getConnection(url, props);
            System.out.println("Connected");
            try (Statement st = db.createStatement();
                 ResultSet rs = st.executeQuery("select * from information_schema.tables")) {
                System.out.println("Executed query");
                if (!rs.next()) {
                    return "Query was empty";
                }
//            ResultSet rs = st.executeQuery("SELECT * FROM people");
                while (rs.next()) {
                    System.out.print("Column 1 returned ");
                    System.out.println(rs.getString(2));
                    System.out.print("Column 2 returned ");
                    System.out.println(rs.getString(3));
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return "SUCCESS";
    }

    @POST
    @Path("/record")
    public String sendRecord() {
        return "data has been sent";
    }
}
