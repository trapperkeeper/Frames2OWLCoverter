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
