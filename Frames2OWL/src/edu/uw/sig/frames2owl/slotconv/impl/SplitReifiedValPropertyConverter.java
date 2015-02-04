/**
 * 
 */
package edu.uw.sig.frames2owl.slotconv.impl;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Mar 5, 2014
 *
 */
public class SplitReifiedValPropertyConverter extends SplitReifiedPropertyConverter
{
	//protected Set<OWLProperty> valMappedProps = new HashSet<OWLProperty>();
	//protected Map<String,Map<String,IRI>> slotName2ValPropMap = new HashMap<String,Map<String,IRI>>();
	protected Map<String,Map<String,String>> slotName2ValPropMap = new HashMap<String,Map<String,String>>();
	protected Slot valueSlot;
	//protected Map<String,IRI> val2PropIRIMap = new HashMap<String,IRI>();

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 * @param convUtils
	 */
	public SplitReifiedValPropertyConverter(KnowledgeBase framesKB,
			Slot framesSlot, OWLOntology owlOnt, IRIUtils iriUtils,
			ConvUtils convUtils)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.impl.SplitReifiedPropertyConverter#init(java.util.Map)
	 */
	@Override
	public boolean init(Map<String, String> initArgs)
	{
		// slot->property maps will be handled by super class
		if (!super.init(initArgs))
			return false;

		// check for the initialization argument that tells where the map from
		// vals to properties is (val->property)
		String val2PropFilePaths = initArgs.get("value_to_property_map");
		
		String valSlotName = initArgs.get("value_slot");
		if(valSlotName!=null)
			valueSlot = framesKB.getSlot(valSlotName);
		if(valueSlot==null)
		{
			System.err.println("Error, invalid value slot specified in config for SplitReifiedValuePropertyConverter");
			return false;
		}
		
		// Read the configuration to determine what the new direct slot naming scheme will be
		String delimiter = initArgs.get("config_map_delimiter");
		if(delimiter==null||delimiter.equals(""))
			delimiter = "|"; // default delimiter
		if(val2PropFilePaths!=null&&!val2PropFilePaths.equals(""))
		{
			for(String val2PropFilePath : val2PropFilePaths.split(","))
			{
				String[] val2PropFilePathEntry = val2PropFilePath.split(delimiter);
				if(val2PropFilePathEntry.length!=2)
				{
					System.err.println("invalid value-to-property config map entry: "+val2PropFilePathEntry);
					continue;
				}
				String key = val2PropFilePathEntry[0];
				String val = val2PropFilePathEntry[1];
								
				// read the value to property map from file
				Properties prop = new Properties();
				InputStream input = null;

				try
				{

					input = new FileInputStream(val);

					// load a properties file
					prop.load(input);
					
					//Map<String,IRI> val2PropIRIMap = new HashMap<String,IRI>();
					Map<String,String> val2PropNameMap = new HashMap<String,String>();

					for (String slotString : prop.stringPropertyNames())
					{
						String propString = prop.getProperty(slotString);
						if(propString==null||propString.equals(""))
						{
							System.err.println("warning, bad value to property map for slot: "+framesSlot);
							continue;
						}
						/*
						IRI newPropIRI = null;
						try
						{
							newPropIRI = iriUtils.getIRIForString(propString);
						}
						catch (IRIGenerationException e)
						{
							e.printStackTrace();
						}
						if(newPropIRI==null)
							continue;
							*/
						//val2PropIRIMap.put(slotString, newPropIRI);
						val2PropNameMap.put(slotString, propString);
						//System.out.println(key + " => " + value);
					}
					
					//slotName2ValPropMap.put(key, val2PropIRIMap);
					slotName2ValPropMap.put(key, val2PropNameMap);

				}
				catch (IOException ex)
				{
					ex.printStackTrace();
				}
				finally
				{
					if (input != null)
					{
						try
						{
							input.close();
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}

		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.impl.SplitReifiedPropertyConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		// all of the slot->renamed_property value conversion is handled by superclass
		super.convertSlotValues(framesCls, owlCls);
		
		// here we are just handling the value->property conversions
		Collection<Instance> reifInsts = (Collection<Instance>)framesCls.getOwnSlotValues(framesSlot);
		for(Instance reifInst : reifInsts)
		{
			
			/*
			 * iterate over slotName2ValPropMap keys instead of reifInst own slots to get slots
			 * to process here!
			 */
			Set<String> slotNames = slotName2ValPropMap.keySet();
			for(String slotName : slotNames)
			{
				Slot currSlot = framesKB.getSlot(slotName);
				
				/*
				if(currSlot.isSystem()||excludedSubSlots.contains(currSlot))
					continue;
					*/
				
				if(currSlot==null)
				{
					System.err.println("could not find slot: "+slotName+" in SplitReifiedValPropertyConverter");
					continue;
				}
				
				// get the OWL property for this slot
				ValueType valType = valueSlot.getValueType();
				//Map<String,IRI> valToIRIMap = slotName2ValPropMap.get(slotName);
				Map<String,String> valToPropNameMap = slotName2ValPropMap.get(slotName);
				
				// this converter maps the string representation of the vals onto properties
				List vals = reifInst.getDirectOwnSlotValues(currSlot);
				for(Object val : vals)
				{
					String valString = val.toString();
					//IRI currPropIRI = valToIRIMap.get(valString);
					String propName = valToPropNameMap.get(valString);
					
					SlotValueConverter converter = converterMap.get(currSlot);
					if(converter==null)
					{
						if(valType==ValueType.CLS) // create mapped object property converter
						{
							converter = new MappedObjectPropertyConverter(framesKB, currSlot, propName, 
									owlOnt, iriUtils, convUtils, newDomainClassExprs);
							Map<String,String> initArgs = new HashMap<String,String>();
							converter.init(initArgs);
							converter.convertSlot();
							
							// put converter in map, we don't need to do this every time
							converterMap.put(currSlot, converter);
						}
						else if(valType==ValueType.INSTANCE) // ERROR, we don't currently allow this
						{
							System.err.println("Warning, skipping slot "+slotName+
									" in SplitReifiedPropertyConverter because it has Instance data type");
						}
						else // create mapped data property converter
						{
							converter = new MappedDataPropertyConverter(framesKB, currSlot, propName, 
									owlOnt, iriUtils, convUtils, newDomainClassExprs);
							Map<String,String> initArgs = new HashMap<String,String>();
							converter.init(initArgs);
							converter.convertSlot();
							
							// put converter in map, we don't need to do this every time
							converterMap.put(currSlot, converter);
						}
					}
					
					IRI currPropIRI = null;
					try
					{
						currPropIRI = iriUtils.getIRIForString(propName);
					}
					catch (IRIGenerationException e)
					{
						e.printStackTrace();
					}
					if(currPropIRI==null)
						continue;
					transferValueSlotVals(currPropIRI, valType, reifInst, owlCls);
				}
			}
		}
	}
	
	protected void transferValueSlotVals(IRI currPropIRI, ValueType valType, Instance reifInst, OWLClass owlCls)
	{
		// add a new subclass assertion to the ontology for each value
		for(Object framesVal : reifInst.getDirectOwnSlotValues(valueSlot))
		{
			OWLClassExpression expr = convUtils.genClassExprForVal(valType, framesVal, currPropIRI);
			OWLAxiom ax = df.getOWLSubClassOfAxiom(owlCls, expr);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
	}
}
