package gov.epa.api;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import ToxPredictor.Database.ChemIdType;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value = "Parameters for search in DSSTox database")
public class LookupRequest {
    
    private List<String> ids;
    private ChemIdType idsType;
    
    @JsonProperty
    @ApiModelProperty(value = "List of chemical identifiers to look up. All identifiers must be of the same type or type should be Any.", required = true)
    public List<String> getIds() {
        return ids;
    }

    @JsonProperty
    public void setIds(List<String> ids) {
        this.ids = ids;
    }

    @JsonProperty
    @ApiModelProperty(value = "Chemical identifier type", required = false)
    public ChemIdType getIdsType() {
        return idsType;
    }

    @JsonProperty
    public void setIdsType(ChemIdType idsType) {
        this.idsType = idsType;
    }
    
    @Override
    public String toString() {
        return String.format("SearchRequest [fields=%s, entries=%s]", ids, idsType.toString());
    }

}
