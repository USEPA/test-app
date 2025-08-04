package gov.epa;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.dropwizard.core.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

public class WebTESTConfiguration extends Configuration
{
	/*
	 *@Valid
    @NotNull
    private JerseyClientConfiguration jerseyClient = new JerseyClientConfiguration();

    @JsonProperty("jerseyClient")
    public JerseyClientConfiguration getJerseyClientConfiguration() {
        return jerseyClient;
    }
	 * */
    
    @JsonProperty("swagger")
    public SwaggerBundleConfiguration swaggerBundleConfiguration = new SwaggerBundleConfiguration();
    
}
