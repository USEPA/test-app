package gov.epa.resources;

import ToxPredictor.Application.model.PredictionResults;
import io.swagger.annotations.Api;

import com.codahale.metrics.annotation.Timed;
import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

@Api
@Path("/")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class PredictionResultsResource extends BasicResource {

	private static final Logger logger = LogManager.getLogger(PredictionResultsResource.class);
	
    @GET
    @Timed
    @Path("/predictionResults")
    public Response getPredictionResults(@QueryParam("jsonURL") String jsonURL) {

        // return the JSON file that was generated by WebTEST.java
        if (StringUtils.isEmpty(jsonURL) || jsonURL.equalsIgnoreCase("undefined")) {
            // for now, just serving this Fathead minnow JSON file by default if no jsonURL was passed in
            jsonURL = "/web-reports/ToxRuns/ToxRun_91-20-3/Fathead minnow LC50 (96 hr)/PredictionResultsConsensus.json";
        }
        java.nio.file.Path currentRelativePath = Paths.get("");
        String basePath = currentRelativePath.toAbsolutePath().toString();

        String fileName = basePath + jsonURL;

        try {
            Gson gson = new Gson();
            JsonReader reader = new JsonReader(new FileReader(fileName));
            PredictionResults predictionResults = gson.fromJson(reader, PredictionResults.class);

            return Response.ok(predictionResults).build();

        } catch (FileNotFoundException e) {
            logger.catching(e);

            return Response.serverError().build();
        }

    }

    @GET
    @Timed
    @Path("/predictionResults/image")
    @Produces("image/png")
    public Response getPredictionResultsImage(@QueryParam("fileURL")
                                              String fileURL) {
        // return the image that was generated by WebTEST.java
        if (StringUtils.isEmpty(fileURL)) {
            return Response.serverError().build();
        }
        
        File file = new File(fileURL);

        return Response.ok(file).build();
    }
}