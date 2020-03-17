package gov.epa.resources;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.codahale.metrics.annotation.Timed;

import AADashboard.Application.MySQL_DB;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Database.ResolverDb;
import gov.epa.api.LookupRequest;
import gov.epa.api.LookupResult;
import gov.epa.api.SearchRequest;
import gov.epa.api.SearchResult;
import io.swagger.annotations.Api;

@Api
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
public class NcctSearchResource {
    
	private static final Logger logger = LogManager.getLogger(NcctSearchResource.class);
	
    public static final int MAX_RECORD_COUNT = 100;

    @Path("search")
    @POST
    @Timed
    public Response search(@Valid SearchRequest query) throws Exception  {
        SearchResult result = new SearchResult();
        try {
            result.setResult(new MySQL_DB().searchNcctDb(WebTEST.DB_Path_NCCT_ID_Records, query, MAX_RECORD_COUNT));
        } catch (RuntimeException ex) {
            logger.catching(ex);
        }

        return Response.ok(result).build();
    }

    @Path("lookup")
    @POST
    @Timed
    public Response lookup(@Valid LookupRequest query) throws Exception  {
    	LookupResult result = new LookupResult();
        try {
        	result.setResult(ResolverDb.lookup(query.getIds(), query.getIdsType()));
        } catch (RuntimeException ex) {
            logger.catching(ex);
        }

        return Response.ok(result).build();
    }
}
