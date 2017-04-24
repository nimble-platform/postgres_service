import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.core.Application;
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
    private static String url;

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
                url = credentials.get("uri").getAsString();
                try {
                    Class.forName("org.postgresql.Driver");
                    logger.info("Postgres driver exists");
                } catch (java.lang.ClassNotFoundException e) {
                    System.out.println(e.getMessage());
                }
            }
        }
    }

    @GET
    @Path("/data")
    public String printAllData() {
        if (url == null) {
            logger.error("URI is null");
            return "ERROR !!! postgres uri is null";
        }

        try {
            Connection db = DriverManager.getConnection(url);
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
        } catch (java.sql.SQLException e) {
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
