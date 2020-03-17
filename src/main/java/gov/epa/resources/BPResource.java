package gov.epa.resources;

import javax.ws.rs.Consumes;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import ToxPredictor.Application.TESTConstants;
import io.swagger.annotations.Api;

@Api
@Path("/" + TESTConstants.abbrevChoiceBoilingPoint)
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class BPResource extends BasicResource {

    protected int endpointId = TESTConstants.numChoiceBoilingPoint;

    @Override
    protected int getEndpointId() {
        return endpointId;
    }
}
