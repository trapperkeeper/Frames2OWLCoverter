/**
 * 
 */
package edu.uw.sig.frames2owl.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.SubnodeConfiguration;
import org.apache.commons.configuration.XMLConfiguration;
import org.semanticweb.owlapi.model.OWLOntology;


import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.uw.sig.frames2owl.instconv.InstanceConverter;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;

/**
 * @author detwiler
 * @date Dec 27, 2012
 *
 */
public class ConfigReader
{
	private XMLConfiguration config;
	
	private Map<String,Boolean> configFlags = new HashMap<String,Boolean>();
	private Map<String,Class> slotConvMap = new TreeMap<String,Class>(String.CASE_INSENSITIVE_ORDER);//new HashMap<String,Class>();
	private Map<String,Map<String,String>> convInitArgsMap = new TreeMap<String,Map<String,String>>(String.CASE_INSENSITIVE_ORDER);//new HashMap<String,Map<String,String>>();
	private List<String> reifExclusions = new ArrayList<String>();
	private List<String> slotAnnotExclusions = new ArrayList<String>();
	private String domainClsName;
	private String domainSuperClsName;
	private String rangeClsName;
	private String rangeSuperClsName;
	
	private Map<String,Class> instConvMap = new TreeMap<String,Class>(String.CASE_INSENSITIVE_ORDER);
	private Map<String,Map<String,String>> instConvInitArgsMap = new TreeMap<String,Map<String,String>>(String.CASE_INSENSITIVE_ORDER);
	
	private Map<String,IRIConf> proj2IriConfMap = new HashMap<String,IRIConf>();
	
	public ConfigReader(String configPath) throws ConfigurationException
	{
	    config = new XMLConfiguration();
	    config.setDelimiterParsingDisabled(true);
	    config.load(configPath);
	    init();
	}
	
	private void init()
	{
		/*
		 <iri project='cho'>
			<value_source>FMAID</value_source>
			<value_comp>{value}</value_comp>
			<fragment_separator>#</fragment_separator>
		</iri>
		 */
		// read iri config info
		List<HierarchicalConfiguration> iriConfs = config.configurationsAt("iri");
		for(HierarchicalConfiguration iriConf : iriConfs)
	    {
	    	// get the project name
			String projectName = iriConf.getString("[@project]");
			String iriDomain = iriConf.getString("iri_domain");
			String valSource = iriConf.getString("value_source");
			String valComp = iriConf.getString("value_comp");
			String fragSep = iriConf.getString("fragment_separator");
			IRIConf currIriConf = new IRIConf(valSource,iriDomain,valComp,fragSep);
			proj2IriConfMap.put(projectName, currIriConf);
	    }
		
		// read config flags and add to map
		HierarchicalConfiguration flags = config.configurationAt("conf-flags");
		if(flags!=null)
		{
			Iterator<String> flagsIt = flags.getKeys();
			while(flagsIt.hasNext())
		    {
				// get the name of the flag
		    	String flagName = flagsIt.next();
		    	
		    	// get flag value as string
		    	boolean flagVal = flags.getBoolean(flagName);
		    	
		    	// put in flag map
				configFlags.put(flagName, flagVal);
		    }
		}
		//System.err.println(configFlags);
		
		// read general configuration parameters
		List<HierarchicalConfiguration> reifExcl = config.configurationsAt("general_conv_args.reif_exclusion");
		for(HierarchicalConfiguration exclusion : reifExcl)
	    {
	    	// get slot name and conversion class name
			String excludedSlots = exclusion.getString("[@excluded_slots]");
			for(String excl : excludedSlots.split(","))
				reifExclusions.add(excl);
	    }
		
		List<HierarchicalConfiguration> slotAnnotExcl = config.configurationsAt("general_conv_args.slot_annot_exclusion");
		for(HierarchicalConfiguration exclusion : slotAnnotExcl)
	    {
	    	// get slot name and conversion class name
			String excludedSlots = exclusion.getString("[@excluded_slots]");
			for(String excl : excludedSlots.split(","))
				slotAnnotExclusions.add(excl);
	    }
		
		// name and location of class that will be used to contruct named domain classes for annotation props
		List<HierarchicalConfiguration> domainConfs = config.configurationsAt("general_conv_args.domain");
		for(HierarchicalConfiguration domainConf : domainConfs)
	    {
	    	// get slot name and conversion class name
			domainClsName = domainConf.getString("[@cls-name]");
			domainSuperClsName = domainConf.getString("[@super-cls-name]");
	    }
		
		// name and location of class that will be used to contruct named range classes for annotation props
		List<HierarchicalConfiguration> rangeConfs = config.configurationsAt("general_conv_args.range");
		for(HierarchicalConfiguration rangeConf : rangeConfs)
	    {
	    	// get slot name and conversion class name
			rangeClsName = rangeConf.getString("[@cls-name]");
			rangeSuperClsName = rangeConf.getString("[@super-cls-name]");
	    }
		
		// read config file to determine which Java classes to use for which slot conversion
		List<HierarchicalConfiguration> slotConverters = config.configurationsAt("slot_conv_classes.slot_conv_class");
	    for(HierarchicalConfiguration slotConverter : slotConverters)
	    {
	    	// get slot name and conversion class name
			String slotName = slotConverter.getString("[@slot_name]");
			String convClsName = slotConverter.getString("[@conv_cls_name]");
			
			try
			{
				Class convClass = Class.forName(convClsName);
				slotConvMap.put(slotName, convClass);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
				continue;
			}
			
			// gather additional arguments if there are any
			Iterator<String> iter = slotConverter.getKeys();
			Map<String,String> attrMap = new HashMap<String,String>();
			while(iter.hasNext())
			{
				String key = iter.next();

				if(!key.equals("[@slot_name]")&&!key.equals("[@conv_cls_name]"))
				{
					//System.err.println("found key for init args = "+key+" for slot "+slotName);
					String attrVal = slotConverter.getString(key);
					key = key.replaceAll("^\\[@","");
					key = key.replaceAll("\\]$","");
					attrMap.put(key, attrVal);
				}
				
			}
			convInitArgsMap.put(slotName,attrMap);
	    }
	    
	    /*
	     <inst_conv_classes>
			<inst_conv_class type="Mapping" 
				conv_cls_name="edu.uw.sig.frames2owl.instconv.impl.MappingConverter" 
				source_slot="source" 
				target_slot="target" 
				direct_property_name="mapsTo" 
				excluded_slots=""/>
		</inst_conv_classes>
	     */
		
	    // read config file to determine which Java classes to use for which slot conversion
 		List<HierarchicalConfiguration> instConverters = config.configurationsAt("inst_conv_classes.inst_conv_class");
 	    for(HierarchicalConfiguration instConverter : instConverters)
 	    {
 	    	String typeName = instConverter.getString("[@type_name]");
			String convClsName = instConverter.getString("[@conv_cls_name]");
			
			try
			{
				Class convClass = Class.forName(convClsName);
				instConvMap.put(typeName, convClass);
			}
			catch (ClassNotFoundException e)
			{
				e.printStackTrace();
				continue;
			}
			
			// gather additional arguments if there are any
			Iterator<String> iter = instConverter.getKeys();
			Map<String,String> attrMap = new HashMap<String,String>();
			while(iter.hasNext())
			{
				String key = iter.next();

				if(!key.equals("[@type]")&&!key.equals("[@conv_cls_name]"))
				{
					//System.err.println("found key for init args = "+key+" for slot "+slotName);
					String attrVal = instConverter.getString(key);
					key = key.replaceAll("^\\[@","");
					key = key.replaceAll("\\]$","");
					attrMap.put(key, attrVal);
				}
				
			}
			instConvInitArgsMap.put(typeName,attrMap);
 	    }

	}
	
	/*
	public String getIRIValueSourceSlot(String projName)
	{
		String valSource = config.getString("iri.value_source");
		return valSource;
	}
	
	public String getIRIValueComp(String projName)
	{
		String valComp = config.getString("iri.value_comp");
		return valComp;
	}
	
	public String getIRIFragSep(String projName)
	{
		String fragSep = config.getString("iri.fragment_separator");  
		return fragSep;
	}
	*/
	
	public Map<String,Class> getSlotConvClassMap()
	{
		return this.slotConvMap;
	}
	
	public Map<String,Map<String,String>> getConvInitArgsMap()
	{
		return this.convInitArgsMap;
	}

	/**
	 * @return the instConvMap
	 */
	public Map<String, Class> getInstConvMap()
	{
		return instConvMap;
	}

	/**
	 * @return the instConvInitArgsMap
	 */
	public Map<String, Map<String, String>> getInstConvInitArgsMap()
	{
		return instConvInitArgsMap;
	}

	/**
	 * @return the reifExclusions
	 */
	public List<String> getReifExclusions()
	{
		return reifExclusions;
	}
	
	/**
	 * @return the reifExclusions
	 */
	public List<String> getSlotAnnotExclusions()
	{
		return slotAnnotExclusions;
	}
	
	/**
	 * @return the domainClsName
	 */
	public String getDomainClsName()
	{
		return domainClsName;
	}

	/**
	 * @return the domainSuperClsName
	 */
	public String getDomainSuperClsName()
	{
		return domainSuperClsName;
	}

	/**
	 * @return the rangeClsName
	 */
	public String getRangeClsName()
	{
		return rangeClsName;
	}

	/**
	 * @return the rangeSuperClsName
	 */
	public String getRangeSuperClsName()
	{
		return rangeSuperClsName;
	}

	/**
	 * @return the configFlags
	 */
	public Map<String, Boolean> getConfigFlags()
	{
		return configFlags;
	}
	
	/**
	 * @param projName the name of the project whose IRIConf we want
	 * @return the IRIConf for the given project
	 */
	public IRIConf getIriConfForProj(String projName)
	{
		return proj2IriConfMap.get(projName);
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		ConfigReader cReader = null;
		try
		{
			cReader = new ConfigReader("resource/ocdm/chmmo-config.xml");
			//System.err.println(cReader.getIRIValueSourceSlot());
			//System.err.println(cReader.getDefaultConv());
		}
		catch (ConfigurationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
