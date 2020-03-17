package gov.epa.api;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;


public class AADashboardBatchRequest extends BaseAADashboardRequest {
	
	private Set<String> query;
	
	@JsonProperty
	@ApiModelProperty(value = "Molecules expressed as SMILES, or MOL, or SMI, or SDF", required = true)
	public Set<String> getQuery() {
		return query;
	}
	@JsonProperty
	public void setQuery(Set<String> query) {
		this.query = query;
	}
	
    @Override
    public String toString() {
        return String.format("AADashboardBatchRequest [query=%s, format=%s, endpoints=%s, methods=%s, reportTypes=%s]",
                query, getFormat());
    }
}
