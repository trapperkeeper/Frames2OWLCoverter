/*******************************************************************************
 * Copyright (c) 2015 University of Washington Structural Informatics Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
/**
 * 
 */
package edu.uw.sig.frames2owl.util;

import java.util.List;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import edu.uw.sig.frames2owl.Converter;

/**
 * @author detwiler
 * @date May 5, 2014
 *
 */
public class BatchConverter
{
	private XMLConfiguration batchConfig;
	
	public BatchConverter(String configPath) throws ConfigurationException
	{
		batchConfig = new XMLConfiguration();
		batchConfig.setDelimiterParsingDisabled(true);
		batchConfig.load(configPath);
	}
	
	private void runBatch()
	{
		// read general configuration parameters
		List<HierarchicalConfiguration> convs = batchConfig.configurationsAt("conv");
		for(HierarchicalConfiguration conv : convs)
	    {
	    	// get arguments for run
			// i.e. resource/cho.pprj results/test.owl http://si.uw.edu/ont/fma.owl
			String framesPath = conv.getString("frames-path");
			String owlPath = conv.getString("owl-path");
			String owlURI = conv.getString("owl-uri");
			String configPath = conv.getString("conf-path");
			
			//System.err.println("will create run using args: "+ framesPath+" "+owlPath+" "+owlURI);
			Converter currConv = new Converter(framesPath, owlPath, owlURI, configPath);
			currConv.run();
	    }
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		BatchConverter converter;
		try
		{
			converter = new BatchConverter("resource/ocdm/ocdm_batch.xml");
			converter.runBatch();
		}
		catch (ConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

	}

}
