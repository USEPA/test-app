package gov.epa.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ToxPredictor.Application.TESTConstants;
import io.swagger.annotations.Api;

@Api
@Path("/" + TESTConstants.abbrevChoiceFHM_LC50)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class FHM_LC50Resource extends BasicResource {

    protected int endpointId = TESTConstants.numChoiceFHM_LC50;
    
    @Override
    protected int getEndpointId() {
        return endpointId;
    }
}
