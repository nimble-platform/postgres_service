import javax.ws.rs.ApplicationPath;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;

/**
 * Created by evgeniyh on 24/04/17.
 */
@ApplicationPath("/")
@Path("/")
public class Main {
    @GET
    @Path("/data")
    public String printAllData() {
        return "All data";
    }

    @POST
    @Path("/record")
    public String sendRecord() {
        return "data has been sent";
    }
}
