package gov.epa.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Parameters for search in NCCT database")
public class SearchRequest {
    
    private List<String> fields;
    private List<String> entries;
    private boolean exact;
    
    
    @JsonProperty
    @ApiModelProperty(value = "List of fields to search in. Available fields: casrn, gsid, dsstox_substance_id, preferred_name, cid, dsstox_compound_id, Canonical_QSARr, InChi_Code_QSARr, InChi_Key_QSARr", required = true)
    public List<String> getFields() {
        return fields;
    }

    @JsonProperty
    public void setFields(List<String> fields) {
        this.fields = fields;
    }

    @JsonProperty
    @ApiModelProperty(value = "List of entries to search", required = true)
    public List<String> getEntries() {
        return entries;
    }
    @JsonProperty
    public void setEntries(List<String> entries) {
        this.entries = entries;
    }

    @JsonProperty
    @ApiModelProperty(value = "Perform exact search?", required = false)
    public boolean isExact() {
        return exact;
    }
    @JsonProperty
    public void setExact(boolean exact) {
        this.exact = exact;
    }

    @Override
    public String toString() {
        return String.format("SearchRequest [fields=%s, entries=%s, exact=%s]", fields, entries, exact);
    }

}
