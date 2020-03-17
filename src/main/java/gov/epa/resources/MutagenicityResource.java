package gov.epa.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ToxPredictor.Application.TESTConstants;
import io.swagger.annotations.Api;

@Api
@Path("/" + TESTConstants.abbrevChoiceMutagenicity)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class MutagenicityResource extends BasicResource {

    protected int endpointId = TESTConstants.numChoiceMutagenicity;
    
    @Override
    protected int getEndpointId() {
        return endpointId;
    }
}
