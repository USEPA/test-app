package gov.epa.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WebTEST prediction bulk request")
public class BulkPredictionQuery extends BaseBulkPredictionQuery {
	private String query;
	
	@JsonProperty
	@ApiModelProperty(value = "Molecule expressed as SMILES, or MOL, or SMI, or SDF", required = true)
	public String getQuery() {
		return query;
	}
	@JsonProperty
	public void setQuery(String query) {
		this.query = query;
	}
    @Override
    public String toString() {
        return String.format("BulkPredictionQuery [query=%s, format=%s, endpoints=%s, methods=%s, reportTypes=%s]",
                query, getFormat(), getEndpoints(), getMethods(), getReportTypes());
    }
	
}
