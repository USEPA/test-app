package gov.epa;

import io.dropwizard.Configuration;
import io.federecio.dropwizard.swagger.SwaggerBundleConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

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
