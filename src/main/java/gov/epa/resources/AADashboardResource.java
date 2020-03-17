package gov.epa.resources;

import com.codahale.metrics.annotation.Timed;
import com.google.common.base.Strings;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.stream.JsonReader;

import AADashboard.Application.AADashboard;
import ToxPredictor.Application.TESTConstants;
import gov.epa.api.AADashboardBatchRequest;
import gov.epa.api.AADashboardRequest;
import gov.epa.api.Chemicals;
import io.swagger.annotations.Api;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.nio.file.Paths;

@Api
@Path("/AADashboard")
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class AADashboardResource extends BasicResource {

	private static final Logger logger = LogManager.getLogger(BulkPredictResource.class);

	@GET
	@Timed
	@Path("/comparisonAlternatives")
	public Response getComparisonOfAlternatives() {

		java.nio.file.Path currentRelativePath = Paths.get("");
		String basePath = currentRelativePath.toAbsolutePath().toString();

		String fileName = basePath + "/AA Dashboard/Output/realtime/flame retardant records.html";

		try {
			Gson gson = new Gson();
			JsonReader reader = new JsonReader(new FileReader(fileName));

			// ******************************************************************************************
			// following assumes javascript is changed to read: vm.chemicals=result
			JsonArray chemicals = gson.fromJson(reader, JsonArray.class);
			return Response.ok(chemicals.toString()).build();

			// ******************************************************************************************
			// following assumes that vm.chemicals=result.chemicals in the javascript:
//            JsonObject jsonObject = gson.fromJson(reader, JsonObject.class);
//            return Response.ok(jsonElement.toString()).build();
			// ******************************************************************************************
//            //create chemicals object to not break the javascript:
//            JsonObject jsonObject=new JsonObject();
//            jsonObject.add("chemicals",chemicals);
//            return Response.ok(jsonObject.toString()).build();
			// ******************************************************************************************

		} catch (FileNotFoundException e) {
			logger.catching(e);
			return Response.serverError().build();
		}
	}

	@POST
	@Timed
	@Path("/chemicals")
	public Response getChemicals(@Valid AADashboardRequest request) {
		if (!isValid(request)) {
			return Response.status(Status.BAD_REQUEST).build();
		}

		int format = TESTConstants.numFormatSMILES;
		switch (request.getFormat()) {
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
		try {
			Chemicals result = new AADashboard().generateDashboard(request.getQuery(), format);
			return Response.ok(result.toJSON()).build();
		} catch (Exception e) {
			logger.catching(e);
			return Response.serverError().build();
		}
	}

	@POST
	@Timed
	@Path("/batchChemicals")
	public Response getBatchChemicals(@Valid AADashboardBatchRequest request) {
		if (!isValidBatch(request)) {
			return Response.status(Status.BAD_REQUEST).build();
		}

		int format = TESTConstants.numFormatSMILES;
		switch (request.getFormat()) {
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
		try {
			Chemicals result = new AADashboard().generateDashboard(request.getQuery(), format);
			return Response.ok(result.toJSON()).build();
		} catch (Exception e) {
			logger.catching(e);
			return Response.serverError().build();
		}
	}

	private boolean isValid(AADashboardRequest request) {
		if (request.getFormat() == null) {
			return false;
		}

		if (!request.getFormat().toUpperCase().equals("MOL") && !request.getFormat().toUpperCase().equals("SMILES")
				&& !request.getFormat().toUpperCase().equals("SDF")
				&& !request.getFormat().toUpperCase().equals("SMI")) {
			return false;
		}

		if (Strings.isNullOrEmpty(request.getQuery())) {
			return false;
		}

		return true;
	}

	private boolean isValidBatch(AADashboardBatchRequest request) {
		if (request.getFormat() == null) {
			return false;
		}

		if (!request.getFormat().toUpperCase().equals("MOL") && !request.getFormat().toUpperCase().equals("SMILES")
				&& !request.getFormat().toUpperCase().equals("SDF")
				&& !request.getFormat().toUpperCase().equals("SMI")) {
			return false;
		}

		if (request.getQuery() == null) {
			for (String query : request.getQuery()) {
				if (Strings.isNullOrEmpty(query))
					return false;
			}
		}

		return true;
	}

}
