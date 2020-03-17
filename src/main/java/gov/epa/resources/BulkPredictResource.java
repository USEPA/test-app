package gov.epa.resources;

import java.util.HashSet;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import com.codahale.metrics.annotation.Timed;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import gov.epa.api.BaseBulkPredictionQuery;
import gov.epa.api.BatchPredictionQuery;
import gov.epa.api.BulkPredictionQuery;
import gov.epa.api.PredictionResult;
import io.swagger.annotations.Api;

@Api
@Path("/bulk")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BulkPredictResource extends BasicResource {

	private static final Logger logger = LogManager.getLogger(BulkPredictResource.class);
	
    @POST
    @Timed
    @Path("/predict")
    public Response bulkPredict(@Valid BulkPredictionQuery query) throws Exception  {
        if (!isValid(query)) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        
        BasicResource res = new BasicResource() {};
        
        int format = getFormat(query);
        Set<String> endpoints = getEndpoints(query);
        Set<String> methods = getMethods(query);
        Set<WebReportType> reportTypes = getReportTypes(query);
        
        PredictionResult result = res.getPredictionResult(WebTEST.go(query.getQuery(), format, endpoints, methods, reportTypes));

        return Response.ok(result).build();
    }

    @POST
    @Timed
    @Path("/predictBatch")
    public Response batchPredict(@Valid BatchPredictionQuery query) throws Exception  {
        if (!isValid(query)) {
            return Response.status(Status.BAD_REQUEST).build();
        }
        
        BasicResource res = new BasicResource() {};
        
        int format = getFormat(query);
        Set<String> endpoints = getEndpoints(query);
        Set<String> methods = getMethods(query);
        Set<WebReportType> reportTypes = getReportTypes(query);
        
        PredictionResult result = res.getPredictionResult(WebTEST.go(query.getQuery(), format, endpoints, methods, reportTypes));

        return Response.ok(result).build();
    }
    
	private static Set<WebReportType> getReportTypes(BaseBulkPredictionQuery query) {
		Set<WebReportType> reportTypes = new HashSet<WebReportType>();
        if (query.getReportTypes() != null) {
            for (String type : query.getReportTypes()) {
                try {
                    reportTypes.add(Enum.valueOf(WebReportType.class, type.toUpperCase()));
                } catch (IllegalArgumentException ex) {
                    // ignore incorrect values
                }
            }
        }
		return reportTypes;
	}

	private static Set<String> getMethods(BaseBulkPredictionQuery query) {
		Set<String> methods = new HashSet<>();
        for (String m : query.getMethods()) {
            methods.add(TESTConstants.getFullMethod(m));
        }
		return methods;
	}

	private static Set<String> getEndpoints(BaseBulkPredictionQuery query) {
		Set<String> endpoints = new HashSet<>();
        for (String e : query.getEndpoints()) {
            endpoints.add(TESTConstants.getFullEndpoint(e));
        }
		return endpoints;
	}

	private static int getFormat(BaseBulkPredictionQuery query) {
		int format = TESTConstants.numFormatSMILES;
		switch (query.getFormat()) {
		case "MOL":
			format = TESTConstants.numFormatMOL;
			break;
		case "SMI":
			format = TESTConstants.numFormatSMI;
			break;
		case "SDF":
			format = TESTConstants.numFormatSDF;
			break;

		default:
			break;
		}
		return format;
	}

    private boolean isValid(BaseBulkPredictionQuery query) {
        if (query.getFormat() == null) {
            return false;
        }
        if (!query.getFormat().toUpperCase().equals("MOL") && 
        		!query.getFormat().toUpperCase().equals("SMILES") && 
        		!query.getFormat().toUpperCase().equals("SDF") && 
        		!query.getFormat().toUpperCase().equals("SMI")) {
            return false;
        }
        if (query.getEndpoints() == null || query.getEndpoints().isEmpty()) {
            return false;
        }
        if (query.getMethods() == null || query.getMethods().isEmpty()) {
            return false;
        }
        
        if ( query instanceof BulkPredictionQuery ) {
	        if (((BulkPredictionQuery)query).getQuery() == null || ((BulkPredictionQuery)query).getQuery().isEmpty()) {
	            return false;
	        }
        }
        else if ( query instanceof BatchPredictionQuery ) {
        	if (((BatchPredictionQuery)query).getQuery() == null || ((BatchPredictionQuery)query).getQuery().isEmpty()) {
	            return false;
	        }
        }
        else {
        	return false;
        }
        
        return true;
    }
}
