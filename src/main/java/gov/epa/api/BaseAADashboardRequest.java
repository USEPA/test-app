package gov.epa.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModelProperty;

public class BaseAADashboardRequest {
	
	private String format;
	
	@JsonProperty
	@ApiModelProperty(value = "Query format - SMILES, or MOL, or SMI, or SDF", required = true)
	public String getFormat() {
		return format;
	}
	@JsonProperty
	public void setFormat(String format) {
		this.format = format;
	}
	
}
