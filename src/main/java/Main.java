import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.log4j.Logger;
import org.postgresql.Driver;

import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Application;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
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
        logger.info("Initialising url, password and user from vcap credentials");
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
                String uri = credentials.get("uri").getAsString();
                setCredentials(uri);
                validatePostgresDriver();
            }
        }
    }

    private static void validatePostgresDriver() {
        try {
            Class.forName("org.postgresql.Driver");
            logger.debug("Postgres driver exists - no exception was thrown");
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
        Driver driver = new Driver();
        if (!driver.acceptsURL(url)) {
            logger.error("Driver doesn't accept the URL");
            throw new RuntimeException("The created URL doesn't fit the postgres driver");
        }
    }

    @GET
    @Path("/test_table")
    public String printAllData() {
        try {
            Connection db = getConnection();
            String query = "SELECT * FROM TEST_TABLE";
//            String query = "SELECT table_name FROM information_schema.tables WHERE table_schema='public'";
//            String query = "CREATE TABLE TEST_TABLE(\n" +
//                    "   ID INT PRIMARY KEY     NOT NULL,\n" +
//                    "   MESSAGE        TEXT    NOT NULL);";
            logger.debug("Executing query " + query);
            try (Statement st = db.createStatement();
                 ResultSet rs = st.executeQuery(query)) {
                logger.debug("Query executed successfully ");
                if (!rs.isBeforeFirst()) {
                    return "Query was empty";
                } else {
                    JsonArray table = resultSetToJsonArray(rs);
                    return table.toString().replace("{", "\n{");
                }
            }
        } catch (Exception e) {
            return "ERROR !!! " + e.getMessage();
        }
    }

    @POST
    @Path("/insert/test_table")
    public String sendRecord(@QueryParam("id") int id,
                             @QueryParam("message") String message) {
        try {
            Connection con = getConnection();
            PreparedStatement p = con.prepareStatement("INSERT INTO TEST_TABLE VALUES (?, ?);");
            p.setInt(1, id);
            p.setString(2, message);
            int insertRows = p.executeUpdate();
            if (insertRows == 1) {
                logger.debug("The row inserted successfully");
            }
        } catch (Exception e) {
            logger.error("Error on post a record command", e);
            return "ERROR" + e.getMessage();
        }
        return "data has been sent";
    }

    private Connection getConnection() throws SQLException {
        logger.debug("Connecting to the db");
        Connection db = DriverManager.getConnection(url, user, password);
        logger.debug("Connected");

        return db;
    }

    private JsonArray resultSetToJsonArray(ResultSet resultSet) throws Exception {
        JsonArray jsonArray = new JsonArray();
        ResultSetMetaData metaData = resultSet.getMetaData();
        int columnsCount = metaData.getColumnCount();

        String[] columns = new String[columnsCount];
        for (int i = 0; i < columnsCount; i++) {
            columns[i] = metaData.getColumnLabel(i + 1);
        }
        JsonObject obj;
        while (resultSet.next()) {
            obj = new JsonObject();
            for (int i = 0; i < columnsCount; i++) {
                obj.addProperty(columns[i], resultSet.getObject(i + 1).toString());
            }
            jsonArray.add(obj);
        }
        return jsonArray;
    }
}
