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

/**
 * Created by evgeniyh on 24/04/17.
 */
@ApplicationPath("/")
@Path("/")
public class Main extends Application {
    private final static Logger logger = Logger.getLogger(Main.class);
    private static String URL_TEMPLATE = "jdbc:postgresql://$HOST:$PORT/$USER";
    private static String url;
    private static String user;
    private static String password;

    static {
        String vcapServices = System.getenv("VCAP_SERVICES");
        if (vcapServices == null) {
            logger.error("Vcap services is null - the app isn't running im bluemix");
        } else {
            JsonObject jsonObject = (JsonObject) (new JsonParser().parse(vcapServices));
            JsonArray elephantBind = jsonObject.getAsJsonArray("elephantsql");
            if (elephantBind == null) {
                logger.error("Couldn't find elephantSql key in vcap services env variable");
            } else {
                JsonObject credentials = elephantBind.get(0).getAsJsonObject().get("credentials").getAsJsonObject();
                logger.info("Initialising credentials");
                System.out.println(credentials.toString());
                String uri = credentials.get("uri").getAsString();
                setCredentials(uri);
                validatePostgresDriver();
            }
        }
    }

    private static void validatePostgresDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            logger.info("Postgres driver exists");
        } catch (ClassNotFoundException e) {
            logger.error("Could not find the JDBC driver!", e);
        }
    }

    private static void setCredentials(String uri) {
        URI parsed = URI.create(uri);
        String host = parsed.getHost();
        int port = parsed.getPort();

        String[] credentials = parsed.getRawUserInfo().split(":");
        user = credentials[0];
        password = credentials[1];

        url = URL_TEMPLATE
                .replace("$HOST", host)
                .replace("$USER", user)
                .replace("$PORT", String.valueOf(port));
    }

    @GET
    @Path("/data")
    public String printAllData() {
        try {
            Driver driver = new Driver();
            if (!driver.acceptsURL(url)) {
                logger.error("Driver doesn't accept the URL");
                return "ERROR ";
            }

            logger.debug("Connecting to the db");
            Connection db = DriverManager.getConnection(url, user, password);
            logger.debug("Connected");

            String query = "SELECT table_name FROM information_schema.tables WHERE table_schema='public'";
            logger.debug("Executing query " + query);
            try (Statement st = db.createStatement();
                 ResultSet rs = st.executeQuery(query)) {
                
                if (!rs.isBeforeFirst()) {
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
            logger.debug("Query executed successfully ");
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
