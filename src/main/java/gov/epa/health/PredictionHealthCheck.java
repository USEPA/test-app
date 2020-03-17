package gov.epa.health;

import com.codahale.metrics.health.HealthCheck;

public class PredictionHealthCheck extends HealthCheck
{
	public PredictionHealthCheck(String template)
	{
		
	}

	@Override
	protected Result check() throws Exception
	{
		return Result.healthy();
	}
}
