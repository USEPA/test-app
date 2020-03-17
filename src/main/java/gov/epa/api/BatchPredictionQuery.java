package gov.epa.api;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WebTEST prediction bulk request")
public class BatchPredictionQuery extends BaseBulkPredictionQuery {
	private Set<String> structures;
	
	@JsonProperty
	@ApiModelProperty(value = "Molecules expressed as SMILES, or MOL, or SMI, or SDF", required = true)
	public Set<String> getStructures() {
		return structures;
	}
	@JsonProperty
	public void setStructures(Set<String> structures) {
		this.structures = structures;
	}
	
    @Override
    public String toString() {
        return String.format("BulkPredictionQuery [structures=%s, format=%s, endpoints=%s, methods=%s, reportTypes=%s]",
        		structures, getFormat(), getEndpoints(), getMethods(), getReportTypes());
    }
	public Set<String> getQuery() {
		return structures;
	}
	
}
