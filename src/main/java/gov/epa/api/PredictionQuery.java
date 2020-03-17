package gov.epa.api;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "WebTEST prediction request")
public class PredictionQuery {
	private String query;
	private String format;
    private String method;
    private String report;
	
	@JsonProperty
	@ApiModelProperty(value = "Molecule expressed as SMILES or MOL", required = true)
	public String getQuery() {
		return query;
	}
	@JsonProperty
	public void setQuery(String query) {
		this.query = query;
	}
	@JsonProperty
	@ApiModelProperty(value = "Query format - SMILES or MOL")
	public String getFormat() {
		return format;
	}
	@JsonProperty
	public void setFormat(String format) {
		this.format = format;
	}
	@JsonProperty
	@ApiModelProperty(value = "Prediction method - one of the following: hc (Hierarchical Clustering), sm (Single Model), nn (Nearest Neighbour), gc (Group Contribution) or consensus (Default)")
	public String getMethod() {
		return method;
	}
	@JsonProperty
	public void setMethod(String method) {
		this.method = method;
	}
    /**
     * @return the report
     */
    public String getReport() {
        return report;
    }
    /**
     * @param report the report to set
     */
    public void setReport(String report) {
        this.report = report;
    }
	
	
}
