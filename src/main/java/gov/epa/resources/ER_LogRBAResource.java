package gov.epa.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ToxPredictor.Application.TESTConstants;
import io.swagger.annotations.Api;

@Api
@Path("/" + TESTConstants.abbrevChoiceER_LogRBA)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class ER_LogRBAResource extends BasicResource {

    protected int endpointId = TESTConstants.numChoiceER_LogRBA;

    @Override
    protected int getEndpointId() {
        return endpointId;
    }
}
