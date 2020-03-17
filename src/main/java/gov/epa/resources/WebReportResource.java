package gov.epa.resources;

import java.io.File;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.codahale.metrics.annotation.Timed;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebTEST;

import io.swagger.annotations.Api;

@Api
@Path("/report")
public class WebReportResource {

    @GET
    @Timed
    public Response predict(@QueryParam("id") String reportId, 
            @QueryParam("endpoint") String endpoint, 
            @QueryParam("method") String method, 
            @QueryParam("type") String type) throws Exception  {
    	
	    if (type == null) {
	        type = "html";
	    }
	    type = type.toLowerCase();
	    
	    if (method == null) {
	        method = "consensus";
	    }
	    
	    method = TESTConstants.getMethod(TESTConstants.getMethod(method)).replace(" ", "");
	    
	    if (endpoint == null) {
	        endpoint = "";
	    }
	    
	    endpoint = TESTConstants.getEndpoint(TESTConstants.getEndpoint(endpoint));
	    
	    if (reportId == null) {
	        reportId = "";
	    }
	    
	    File file = new File(String.format("%s/ToxRuns/ToxRun_%s/%s/PredictionResults%s.%s", 
	            WebTEST.reportFolderName, reportId, endpoint, method, type));
	    if (file.exists()) {
            return Response.ok(file, getContentType(type)).build();
	    } else {
	        return Response.status(404).entity("Report Not Found.").build();
	    }
    }
	
	private MediaType getContentType(String type) {
	    switch (type) {
        case "json":
            return new MediaType("application", "json", "UTF-8");
        case "pdf":
            return new MediaType("application", "pdf");
        default:
            return new MediaType("text", "html", "UTF-8");
        }
	}
}
