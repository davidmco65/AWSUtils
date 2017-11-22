package com.microstar.tap3.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.amazonaws.services.lambda.AWSLambda;
import com.amazonaws.services.lambda.AWSLambdaClientBuilder;
import com.amazonaws.services.lambda.model.DeleteFunctionRequest;
import com.amazonaws.services.lambda.model.DeleteFunctionResult;
import com.amazonaws.services.lambda.model.FunctionConfiguration;
import com.amazonaws.services.lambda.model.ListFunctionsResult;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionRequest;
import com.amazonaws.services.lambda.model.ListVersionsByFunctionResult;

public class CleanOldLambdaVersions {

	public static void main(String[] args) {
		// This will trim up to the last 3 versions of any lambda deployed to the account defined by AWS_PROFILE

		HashMap<String, Integer> maxArray = new HashMap<String, Integer>();
		
		AWSLambda client = AWSLambdaClientBuilder.defaultClient();
		
		ListFunctionsResult lfRes = client.listFunctions();
		for ( FunctionConfiguration fc : lfRes.getFunctions())
		{
			System.out.println(String.format("Found function %s, code size = %d, version = %s\n", 
					fc.getFunctionName(), fc.getCodeSize(), fc.getVersion()
					));
			maxArray.put(fc.getFunctionName(), null);
			
		}
		
		for ( String name : maxArray.keySet() )
		{
			ListVersionsByFunctionRequest lvfReq = new ListVersionsByFunctionRequest()
					.withFunctionName(name);
			ListVersionsByFunctionResult lvfRes = client.listVersionsByFunction(lvfReq);
			List<FunctionConfiguration> vers = lvfRes.getVersions();
			for ( FunctionConfiguration fc : vers )
			{
				try
				{
					if ( ! fc.getVersion().equals("$LATEST"))
					{
						System.out.println(String.format("Deleting function %s, version %s", name, fc.getVersion()));
						DeleteFunctionRequest dfReq = new DeleteFunctionRequest()
								.withFunctionName(name)
								.withQualifier(fc.getVersion());
						DeleteFunctionResult dfRes = client.deleteFunction(dfReq);
						System.out.println(String.format("Status = %d", dfRes.getSdkHttpMetadata().getHttpStatusCode()));
					}
				}
				catch ( Exception ex )
				{
					System.out.println("Delete failed: " + ex.getMessage());
				}
			}
		}
	}
	
	private static Integer parseInteger(String version)
	{
		Integer ret = null;
		try
		{
			ret = Integer.parseInt(version);
		}
		catch ( NumberFormatException nfe )
		{
			
		}
		return ret;
	}

}
