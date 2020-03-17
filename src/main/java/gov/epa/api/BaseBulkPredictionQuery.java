package gov.epa.api;

import java.util.Set;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WebTEST prediction bulk request")
public class BaseBulkPredictionQuery {
	private String format;
    private Set<String> endpoints;
    private Set<String> methods;
    private Set<String> reportTypes;
	
	@JsonProperty
	@ApiModelProperty(value = "Query format - SMILES, or MOL, or SMI, or SDF")
	public String getFormat() {
		return format;
	}
	@JsonProperty
	public void setFormat(String format) {
		this.format = format;
	}
	@JsonProperty
	@ApiModelProperty(value = "Prediction methods - list of the following values: hc (Hierarchical Clustering), sm (Single Model), nn (Nearest Neighbour), gc (Group Contribution) or consensus (Default)")
	public Set<String> getMethods() {
		return methods;
	}
	@JsonProperty
	public void setMethods(Set<String> methods) {
		this.methods = methods;
	}
    public Set<String> getEndpoints() {
        return endpoints;
    }
    public void setEndpoints(Set<String> endpoints) {
        this.endpoints = endpoints;
    }
    public Set<String> getReportTypes() {
        return reportTypes;
    }
    public void setReportTypes(Set<String> reportTypes) {
        this.reportTypes = reportTypes;
    }
	
}
