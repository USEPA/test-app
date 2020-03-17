package gov.epa.resources;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.validation.Valid;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;

import com.codahale.metrics.annotation.Timed;

import ToxPredictor.Application.TESTConstants;
import ToxPredictor.Application.WebReportType;
import ToxPredictor.Application.WebTEST;
import ToxPredictor.Utilities.FormatUtils;
import ToxPredictor.Utilities.TESTPredictedValue;
import gov.epa.api.PredictedValue;
import gov.epa.api.PredictionQuery;
import gov.epa.api.PredictionResult;

public abstract class BasicResource {
    
	private static final Logger logger = LogManager.getLogger(BasicResource.class);
	
    protected int endpointId = TESTConstants.numChoiceDescriptors;
    
    // must be overridden in implementations
    protected int getEndpointId() {
        return endpointId;
    }

    protected List<TESTPredictedValue> predict(String query, int f, int e, String abbrevMethod, String reportType) {
        Set<WebReportType> reportTypes = new HashSet<WebReportType>();
        if (reportType != null) {
            try {
                reportTypes.add(Enum.valueOf(WebReportType.class, reportType.toUpperCase()));
            } catch (IllegalArgumentException ex) {
                // ignore incorrect values
            }
        }
        
        List<TESTPredictedValue> values = null;
        
        try {
            values = WebTEST.go(query, f, e, Strings.isEmpty(abbrevMethod) ? TESTConstants.numChoiceConsensus : TESTConstants.getMethod(abbrevMethod), reportTypes);
        } catch (Exception ex) {
            logger.catching(ex);
        }
        
        return values;
    }
    
    protected PredictionResult getPredictionResult(List<TESTPredictedValue> predictions) throws Exception {
        
        PredictionResult res = new PredictionResult();
        res.setPredictionTime(Instant.now().toEpochMilli());
        res.setCondition("25°C");
        
        boolean singlePrediction = predictions != null && predictions.size() == 1 && predictions.get(0) != null;

        if (singlePrediction) {
            res.setEndpoint(predictions.get(0).endpoint);
            res.setMethod(predictions.get(0).method);
        }

        if (predictions == null) {
            return res;
        }
        
        for ( TESTPredictedValue v: predictions ) {
            if (v == null) {
                continue;
            }

            PredictedValue p = new PredictedValue();
            
            if (!singlePrediction) {
                p.setEndpoint(TESTConstants.getAbbrevEndpoint(v.endpoint));
                p.setMethod(TESTConstants.getAbbrevMethod(v.method));
            }

            String molarLogUnits = null; 
            String massUnits = null;

            if (!TESTConstants.isBinary(v.endpoint)) {
                if (TESTConstants.isLogMolar(v.endpoint))
                    molarLogUnits = TESTConstants.getMolarLogUnits(v.endpoint);
                
                massUnits = TESTConstants.getMassUnits(v.endpoint);
            }
            
            p.setId(v.id);
            p.setSmiles(v.smiles);
            
            if (TESTConstants.isBinary(v.endpoint)) {
                p.setExpActive(v.expActive);
                p.setPredActive(v.predActive);
            }
            else {
                if (TESTConstants.isLogMolar(v.endpoint)) {
                    if ( FormatUtils.isMaterial(v.expValLogMolar) )
                        p.setExpValMolarLog(FormatUtils.asString(v.expValLogMolar));
                    if ( FormatUtils.isMaterial(v.predValLogMolar) )
                        p.setPredValMolarLog(FormatUtils.asString(v.predValLogMolar));
                    p.setMolarLogUnits(molarLogUnits);
                }

                if ("°C".equals(massUnits)) {
                    if ( FormatUtils.isMaterial(v.expValMass) )
                        p.setExpValMass(FormatUtils.toD1(v.expValMass));
                    if ( FormatUtils.isMaterial(v.predValMass) )
                        p.setPredValMass(FormatUtils.toD1(v.predValMass));
                } else {
                    if ( FormatUtils.isMaterial(v.expValMass) )
                        p.setExpValMass(FormatUtils.asString(v.expValMass));
                    if ( FormatUtils.isMaterial(v.predValMass) )
                        p.setPredValMass(FormatUtils.asString(v.predValMass));
                }
                p.setMassUnits(massUnits);
            }
            
            p.setMessage(v.message);
            if ( !Strings.isEmpty(v.error) ) {
                p.setError(v.error);
            }
            p.setErrorCode(v.errorCode);
            
            p.setCasrn(v.casrn);
            p.setPreferredName(v.preferredName);
            p.setDtxsid(v.dtxsid);
            p.setInChICode(v.inChICode);
            p.setInChIKey(v.inChIKey);

            res.getPredictions().add(p);
        }
        
        return res;
    }
    
	public PredictionResult predict(PredictionQuery query, int endpoint) throws Exception  {
    	int f = TESTConstants.getFormat(query.getFormat());

    	if ( f == -1 )
    		f = TESTConstants.numFormatSMILES;
    	return getPredictionResult(predict(query.getQuery(), f, endpoint, query.getMethod(), query.getReport()));
    }
	
    @GET
    @Timed
    public Response predict(@QueryParam("smiles") String smiles, @QueryParam("method") String method) throws Exception  {
        PredictionResult result =  getPredictionResult(predict(smiles, TESTConstants.numFormatSMILES, getEndpointId(), method, null));

        if (result.getPredictions() == null ||
                result.getPredictions().size() == 0) {
            return Response.ok(result).status(Response.Status.EXPECTATION_FAILED).build();
        }

        return Response.ok(result).build();
    }

    @POST
    @Timed
    public Response predict(@Valid PredictionQuery query) throws Exception  {
        PredictionResult result =  predict(query, getEndpointId());

        if (result.getPredictions() == null ||
                result.getPredictions().size() == 0) {
            return Response.ok(result).status(Response.Status.EXPECTATION_FAILED).build();
        }

        return Response.ok(result).build();
    }

}
