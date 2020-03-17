package gov.epa.api;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;

@ApiModel(value = "Ncct Search Result")
public class NcctRecord implements Serializable {
    private static final long serialVersionUID = 811932162571176805L;
    private int gsid;
    private String casrn;
    private String dsstoxSubstanceId;
    private String name;
    private String cid;
    private String dsstoxCompoundId;
    private String saltSolvent;
    private String saltSolventIs;
    private String smiles;
    private String inChICode;
    private String inChIKey;
    private String mol;
    private String details;
    private String key;
    
    /**
     * @return the gsid
     */
    @JsonProperty
    public int getGsid() {
        return gsid;
    }

    /**
     * @param gsid the gsid to set
     */
    @JsonProperty
    public void setGsid(int gsid) {
        this.gsid = gsid;
    }

    /**
     * @return the casrn
     */
    @JsonProperty
    public String getCasrn() {
        return casrn;
    }

    /**
     * @param casrn the casrn to set
     */
    @JsonProperty
    public void setCasrn(String casrn) {
        this.casrn = casrn;
    }

    /**
     * @return the dsstoxSubstanceId
     */
    @JsonProperty
    public String getDsstoxSubstanceId() {
        return dsstoxSubstanceId;
    }

    /**
     * @param dsstoxSubstanceId the dsstoxSubstanceId to set
     */
    @JsonProperty
    public void setDsstoxSubstanceId(String dsstoxSubstanceId) {
        this.dsstoxSubstanceId = dsstoxSubstanceId;
    }

    /**
     * @return the name
     */
    @JsonProperty
    public String getName() {
        return name;
    }

    /**
     * @param name the name to set
     */
    @JsonProperty
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the cid
     */
    @JsonProperty
    public String getCid() {
        return cid;
    }

    /**
     * @param cid the cid to set
     */
    @JsonProperty
    public void setCid(String cid) {
        this.cid = cid;
    }

    /**
     * @return the dsstoxCompoundId
     */
    @JsonProperty
    public String getDsstoxCompoundId() {
        return dsstoxCompoundId;
    }

    /**
     * @param dsstoxCompoundId the dsstoxCompoundId to set
     */
    @JsonProperty
    public void setDsstoxCompoundId(String dsstoxCompoundId) {
        this.dsstoxCompoundId = dsstoxCompoundId;
    }

    /**
     * @return the saltSolvent
     */
    @JsonProperty
    public String getSaltSolvent() {
        return saltSolvent;
    }

    /**
     * @param saltSolvent the saltSolvent to set
     */
    @JsonProperty
    public void setSaltSolvent(String saltSolvent) {
        this.saltSolvent = saltSolvent;
    }

    /**
     * @return the saltSolventIs
     */
    @JsonProperty
    public String getSaltSolventIs() {
        return saltSolventIs;
    }

    /**
     * @param saltSolventIs the saltSolventIs to set
     */
    @JsonProperty
    public void setSaltSolventIs(String saltSolventIs) {
        this.saltSolventIs = saltSolventIs;
    }

    /**
     * @return the smiles
     */
    @JsonProperty
    public String getSmiles() {
        return smiles;
    }

    /**
     * @param smiles the smiles to set
     */
    @JsonProperty
    public void setSmiles(String smiles) {
        this.smiles = smiles;
    }

    /**
     * @return the inChICode
     */
    @JsonProperty
    public String getInChICode() {
        return inChICode;
    }

    /**
     * @param inChICode the inChICode to set
     */
    @JsonProperty
    public void setInChICode(String inChICode) {
        this.inChICode = inChICode;
    }

    /**
     * @return the inChIKey
     */
    @JsonProperty
    public String getInChIKey() {
        return inChIKey;
    }

    /**
     * @param inChIKey the inChIKey to set
     */
    @JsonProperty
    public void setInChIKey(String inChIKey) {
        this.inChIKey = inChIKey;
    }
    
    /**
     * @return the mol
     */
    @JsonProperty
    public String getMol() {
        return mol;
    }

    /**
     * @param mol the mol to set
     */
    @JsonProperty
    public void setMol(String mol) {
        this.mol = mol;
    }
    

    /**
     * @return the details
     */
    @JsonProperty
    public String getDetails() {
        return details;
    }

    /**
     * @param details the details to set
     */
    @JsonProperty
    public void setDetails(String details) {
        this.details = details;
    }

    /**
     * @return the key
     */
    @JsonProperty
    public String getKey() {
        return key;
    }

    /**
     * @param key the key to set
     */
    @JsonProperty
    public void setKey(String key) {
        this.key = key;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.format(
                "NcctRecord [gsid=%s, casrn=%s, dsstoxSubstanceId=%s, name=%s, cid=%s, dsstoxCompoundId=%s, saltSolvent=%s, saltSolventIs=%s, smiles=%s, inChICode=%s, inChIKey=%s, mol=%s, details=%s]",
                gsid, casrn, dsstoxSubstanceId, name, cid, dsstoxCompoundId, saltSolvent, saltSolventIs, smiles,
                inChICode, inChIKey, mol, details);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + gsid;
        return result;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        NcctRecord other = (NcctRecord) obj;
        if (gsid != other.gsid)
            return false;
        return true;
    }
    
}
