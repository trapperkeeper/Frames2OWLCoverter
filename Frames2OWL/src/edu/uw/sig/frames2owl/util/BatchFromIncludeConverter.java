/**
 * 
 */
package edu.uw.sig.frames2owl.util;

import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.XMLConfiguration;

import edu.stanford.smi.protege.model.FrameID;
import edu.uw.sig.frames2owl.Converter;

/**
 * @author detwiler
 * @date May 6, 2014
 *
 */
public class BatchFromIncludeConverter
{
	private XMLConfiguration batchFromIncludeConfig;
	
	public BatchFromIncludeConverter(String configPath) throws ConfigurationException
	{
		batchFromIncludeConfig = new XMLConfiguration();
		batchFromIncludeConfig.setDelimiterParsingDisabled(true);
		batchFromIncludeConfig.load(configPath);
	}
	
	private void runBatch()
	{
		// read general configuration parameters
		HierarchicalConfiguration conv = batchFromIncludeConfig.configurationAt("conv");
		
		// run converter for primary 
		String framesPath = conv.getString("frames-path");
		String owlPath = conv.getString("owl-path");
		String owlURI = conv.getString("owl-uri");
		String configPath = conv.getString("conf-path");
		
		
		//System.err.println("will create run using args: "+ framesPath+" "+owlPath+" "+owlURI);
		Converter mainConv = new Converter(framesPath, owlPath, owlURI, configPath);
		mainConv.run();
		
		// reuse frame2ProjectMap
		Map<FrameID,String> frame2ProjectMap = mainConv.getFrame2ProjectMap();
		
		// get all of the included ont configs
		List<HierarchicalConfiguration> inclConfs = conv.configurationsAt("include");
		Map<String,HierarchicalConfiguration> name2ConfMap = new HashMap<String,HierarchicalConfiguration>();
		for(HierarchicalConfiguration inclConf : inclConfs)
		{
			String name = inclConf.getString("[@name]");
			if(name==null||name.equals(""))
			{
				System.err.println("Include configuration with no name attribute");
				continue;
			}
			name2ConfMap.put(name,inclConf);
		}
		
		// create and run converter for each included ont
		Collection<URI> includedProjURIs = mainConv.getIncludedProjects();
		for(URI includedProjURI : includedProjURIs)
		{
			String ontName = getOntNameFromURI(includedProjURI);
			HierarchicalConfiguration include = name2ConfMap.get(ontName);
			if(include==null)
			{
				System.err.println("Included ontology with no corresponding batch config entry");
				continue;
			}
			System.err.println(include);
			
			//TODO you are here!
			String includeFramesPath = includedProjURI.getPath();
			String includeOwlPath = include.getString("owl-path");
			String includeOwlURI = include.getString("owl-uri");
			String includeConfigPath = include.getString("conf-path");
			Converter importConv = new Converter(includeFramesPath, includeOwlPath, includeOwlURI, 
					includeConfigPath, frame2ProjectMap);
			importConv.run();
		}
		
		/*
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
	    */
	}
	
	private String getOntNameFromURI(URI uri)
	{
		String path = uri.getPath();
		int lastSlashInd = path.lastIndexOf('/');
		int lastPeriodInd = path.lastIndexOf('.');
		String fileName = path.substring(lastSlashInd+1, lastPeriodInd);
		return fileName;
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		BatchFromIncludeConverter converter;
		try
		{
			converter = new BatchFromIncludeConverter(args[0]);
			converter.runBatch();
		}
		catch (ConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
