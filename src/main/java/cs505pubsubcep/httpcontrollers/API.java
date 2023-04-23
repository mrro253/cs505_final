package cs505pubsubcep.httpcontrollers;

import com.google.gson.Gson;
import cs505pubsubcep.CEP.accessRecord;
import cs505pubsubcep.database.Database;
import cs505pubsubcep.Launcher;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.*;

@Path("/api")
public class API {

    @Inject
    private javax.inject.Provider<org.glassfish.grizzly.http.server.Request> request;

    private Gson gson;
    Database db;

    public API() {
        gson = new Gson();
        db = new Database();
    }

    //check local
    //curl --header "X-Auth-API-key:1234" "http://localhost:8082/api/checkmycep"

    //check remote
    //curl --header "X-Auth-API-key:1234" "http://[linkblueid].cs.uky.edu:8082/api/checkmycep"
    //curl --header "X-Auth-API-key:1234" "http://localhost:8081/api/checkmycep"

    //check remote
    //curl --header "X-Auth-API-key:1234" "http://[linkblueid].cs.uky.edu:8081/api/checkmycep"

    @GET
    @Path("/checkmycep")
    @Produces(MediaType.APPLICATION_JSON)
    public Response checkMyEndpoint(@HeaderParam("X-Auth-API-Key") String authKey) {
        String responseString = "{}";
        try {

            //get remote ip address from request
            String remoteIP = request.get().getRemoteAddr();
            //get the timestamp of the request
            long access_ts = System.currentTimeMillis();
            System.out.println("IP: " + remoteIP + " Timestamp: " + access_ts);

            Map<String,String> responseMap = new HashMap<>();
            if(Launcher.cepEngine != null) {

                    responseMap.put("success", Boolean.TRUE.toString());
                    responseMap.put("status_desc","CEP Engine exists");

            } else {
                responseMap.put("success", Boolean.FALSE.toString());
                responseMap.put("status_desc","CEP Engine is null!");
            }

            responseString = gson.toJson(responseMap);


        } catch (Exception ex) {

            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();

            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }
    @GET
    @Path("/getteam")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getTeam(@HeaderParam("X-Auth-API-Key") String authKey) {
        String responseString = "{}";
        try {
            Map<String,Object> responseMap = new HashMap<>();
            responseMap.put("team_name", "ContactTracingTitans");
            responseMap.put("team_member_sids", Arrays.asList(12352407, 12402867, 12292147));
            int check = Launcher.isAppOnline();
            responseMap.put("app_status_code", check);
            responseString = gson.toJson(responseMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/reset")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetDb(@HeaderParam("X-Auth-API-Key") String authKey) {
        Database db = new Database();
        String responseString = "{}";
        try {
            boolean resetSuccessful = db.resetData(); // replace with actual method to reset data
            Map<String,Object> responseMap = new HashMap<>();
            responseMap.put("reset_status_code", resetSuccessful ? 1 : 0);
            responseString = gson.toJson(responseMap);
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
        return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    }

    @GET
    @Path("/zipalertlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getZipAlertList() {
        try {
            List<String> counts = db.getAlertZipcodes();
            if (counts != null && !counts.isEmpty()) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("ziplist", counts);
                return Response.ok(gson.toJson(responseMap)).build();
            } else {
                return Response.ok("{\"ziplist\": []}").build();
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
    }

    @GET
    @Path("/alertlist")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getStateAlertList() {
        try {
            List<String> zipList = db.getAlertZipcodes();
            if (zipList != null && zipList.size() >= 5) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("state_status", 1);
                return Response.ok(gson.toJson(responseMap)).build();
            } else {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("state_status", 0);
                return Response.ok(gson.toJson(responseMap)).build();
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
    }

    // WIP
    @GET
    @Path("/getconfirmedcontacts/{mrn}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getConfirmedContactsList( @PathParam("mrn") int mrn ) {
        try {
            List<String> contactList = db.getContacts(mrn);
            if (contactList != null) {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("contactlist", contactList);
                return Response.ok(gson.toJson(responseMap)).build();
            } else {
                Map<String, Object> responseMap = new HashMap<>();
                responseMap.put("contactlist", 0);
                return Response.ok(gson.toJson(responseMap)).build();
            }
        } catch (Exception ex) {
            StringWriter sw = new StringWriter();
            ex.printStackTrace(new PrintWriter(sw));
            String exceptionAsString = sw.toString();
            ex.printStackTrace();
            return Response.status(500).entity(exceptionAsString).build();
        }
    }

    // @GET
    // @Path("/getaccesscount")
    // @Produces(MediaType.APPLICATION_JSON)
    // public Response getAccessCount(@HeaderParam("X-Auth-API-Key") String authKey) {
    //     String responseString = "{}";
    //     try {

    //         //get remote ip address from request
    //         String remoteIP = request.get().getRemoteAddr();
    //         //get the timestamp of the request
    //         long access_ts = System.currentTimeMillis();
    //         System.out.println("IP: " + remoteIP + " Timestamp: " + access_ts);

    //         //generate event based on access
    //         String inputEvent = gson.toJson(new accessRecord(remoteIP,access_ts));
    //         System.out.println("inputEvent: " + inputEvent);

    //         //send input event to CEP
    //         Launcher.cepEngine.input(Launcher.inputStreamName, inputEvent);

    //         //generate a response
    //         Map<String,String> responseMap = new HashMap<>();
    //         responseMap.put("accesscoint",String.valueOf(Launcher.accessCount));
    //         responseString = gson.toJson(responseMap);

    //     } catch (Exception ex) {

    //         StringWriter sw = new StringWriter();
    //         ex.printStackTrace(new PrintWriter(sw));
    //         String exceptionAsString = sw.toString();
    //         ex.printStackTrace();

    //         return Response.status(500).entity(exceptionAsString).build();
    //     }
    //     return Response.ok(responseString).header("Access-Control-Allow-Origin", "*").build();
    // }


}
