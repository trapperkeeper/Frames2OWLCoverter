/**
 * 
 */
package edu.uw.sig.frames2owl.slotconv.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.slotconv.BaseReifiedPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;
import edu.uw.sig.frames2owl.util.ConfigReader;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Feb 3, 2014
 *
 */
public class SplitReifiedPropertyConverter extends BaseReifiedPropertyConverter
{
	//protected Map<Slot,OWLProperty> subProps = new HashMap<OWLProperty>();
	//protected List<Slot> excludedSubSlots = new ArrayList<Slot>();
	protected Map<String,String> propRenameMap = new HashMap<String,String>();
	protected Map<Slot,SlotValueConverter> converterMap = new HashMap<Slot,SlotValueConverter>();
	protected Set<OWLClassExpression> newDomainClassExprs = new HashSet<OWLClassExpression>();

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 * @param convUtils
	 */
	public SplitReifiedPropertyConverter(KnowledgeBase framesKB,
			Slot framesSlot, OWLOntology owlOnt, IRIUtils iriUtils,
			ConvUtils convUtils)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
	}
	
	public boolean init(Map<String,String> initArgs)
	{
		if(!super.init(initArgs))
			return false;
		
		// determine if any slots are to be omitted
		String excludedSlotNames = initArgs.get("excluded_slots");
		if(excludedSlotNames!=null&&!excludedSlotNames.equals(""))
		{
			for(String excludedSlotName : excludedSlotNames.split(","))
			{
				Slot excludedSlot = framesKB.getSlot(excludedSlotName);
				if(excludedSlot!=null)
					excludedSubSlots.add(excludedSlot);
				else
				{
					System.err.println("could not locate excluded slot indicated in config: "+excludedSlotName);
					return false;
				}
			}
		}
		
		// Read the configuration to determine what the new direct slot naming scheme will be
		String propertyRenameMapStrings = initArgs.get("property_rename_map");
		String delimiter = initArgs.get("config_map_delimiter");
		if(delimiter==null||delimiter.equals(""))
			delimiter = "|"; // default delimiter
		if(propertyRenameMapStrings!=null&&!propertyRenameMapStrings.equals(""))
		{
			for(String propRenameMapString : propertyRenameMapStrings.split(","))
			{
				String[] propRenameMapEntry = propRenameMapString.split(delimiter);
				if(propRenameMapEntry.length!=2)
				{
					System.err.println("invalid slot rename map entry: "+propRenameMapEntry);
					continue;
				}
				String key = propRenameMapEntry[0];
				String val = propRenameMapEntry[1];
				propRenameMap.put(key, val);
			}
		}
		
		// get domain of reified slot
		Collection<Cls> domainClses = framesSlot.getDirectDomain();
		
		// create a set of owl classes for domain
		for(Cls domainCls : domainClses)
		{
			IRI clsIRI;
			try
			{
				clsIRI = iriUtils.getIRIForFrame(domainCls);
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				continue;
			}
			OWLClassExpression clsExpr = df.getOWLClass(clsIRI);
			newDomainClassExprs.add(clsExpr);
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		if(!framesSlot.getValueType().equals(ValueType.INSTANCE))
		{
			System.err.println("Error, SplitReifiedPropertyConverter used on non-Instance slot");
			return;
		}
		
		// this is the heart of this class
		Collection<Instance> reifInsts = (Collection<Instance>)framesCls.getOwnSlotValues(framesSlot);
		for(Instance reifInst : reifInsts)
		{
			Collection<Slot> slots = reifInst.getOwnSlots();
			for(Slot currSlot : slots)
			{
				if(currSlot.isSystem()||excludedSubSlots.contains(currSlot))
					continue;
				
				// get the OWL property for this slot
				String slotName = currSlot.getName();
				ValueType valType = currSlot.getValueType();
				String propRename = this.propRenameMap.get(slotName);
				
				// ignore unmapped slots
				if(propRename==null||propRename.equals(""))
				{
					// Some superclasses may not have a rename map or an exclusion for certain reified instance subslots
					// (as they will be handled elsewhere) just continue.
					//System.err.println("Error, subslot in split reified relation is neither excluded nor mapped: "+slotName);
					continue;
				}
				
				IRI currPropIRI = null;
				try
				{
					currPropIRI = iriUtils.getIRIForString(propRename);
				}
				catch (IRIGenerationException e)
				{
					e.printStackTrace();
				}
				if(currPropIRI==null)
					continue;
				
				/*
				// add a new subclass assertion to the ontology for each value
				for(Object framesVal : reifInst.getDirectOwnSlotValues(currSlot))
				{
					OWLClassExpression expr = convUtils.genClassExprForVal(valType, framesVal, currPropIRI);
					OWLAxiom ax = df.getOWLSubClassOfAxiom(owlCls, expr);
					man.applyChange(new AddAxiom(owlOnt, ax));
				}
				*/
				SlotValueConverter converter = converterMap.get(currSlot);
				if(converter==null)
				{
					if(valType==ValueType.CLS) // create mapped object property converter
					{
						converter = new MappedObjectPropertyConverter(framesKB, currSlot, propRename, 
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
						converter = new MappedDataPropertyConverter(framesKB, currSlot, propRename, 
								owlOnt, iriUtils, convUtils, newDomainClassExprs);
						Map<String,String> initArgs = new HashMap<String,String>();
						converter.init(initArgs);
						converter.convertSlot();
						
						// put converter in map, we don't need to do this every time
						converterMap.put(currSlot, converter);
					}
				}
				convertInstSlotValues(currSlot, currPropIRI, valType, reifInst, owlCls);
			}
		}

	}
	
	protected void convertInstSlotValues(Slot currSlot, IRI currPropIRI, ValueType valType, Instance reifInst, OWLClass owlCls)
	{
		// add a new subclass assertion to the ontology for each value
		for(Object framesVal : reifInst.getDirectOwnSlotValues(currSlot))
		{
			OWLClassExpression expr = convUtils.genClassExprForVal(valType, framesVal, currPropIRI);
			OWLAxiom ax = df.getOWLSubClassOfAxiom(owlCls, expr);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#createSubPropertyAxioms()
	 */
	@Override
	protected void createSubPropertyAxioms()
	{
		// noop, since we are dropping the actual reified property
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyDomain()
	 */
	@Override
	protected void setPropertyDomain()
	{
		// set domain of new sub properties
		
		//TODO ...
		/* The intuition is to set the domain of the new direct slots to the domain of the former reified slot.
		 * The problem with that plan is determining what those direct slots are, we will use our map.
		 */

		/*
		// get domain of reified slot
		Collection<Cls> domainClses = framesSlot.getDirectDomain();
		
		// create a set of owl classes for domain
		Set<OWLClassExpression> newDomainClassExprs = new HashSet<OWLClassExpression>();
		for(Cls domainCls : domainClses)
		{
			IRI clsIRI;
			try
			{
				clsIRI = iriUtils.getIRIForFrame(domainCls);
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				continue;
			}
			OWLClassExpression clsExpr = df.getOWLClass(clsIRI);
			newDomainClassExprs.add(clsExpr);
		}
		
		for(String propNameKey : propRenameMap.keySet())
		{
			String propName = propRenameMap.get(propNameKey);
			
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
			
			Slot origSlot = framesKB.getSlot(propNameKey);
			if(origSlot==null)
			{
				System.err.println("slot not found for reified split map");
				continue;
			}
			ValueType valType = origSlot.getValueType();
			OWLProperty currProp = convUtils.getOWLPropertyForName(propName, valType);
			OWLAxiom domainAx = convUtils.getPropertyDomainAxiom(newDomainClassExprs, currProp);
			man.applyChange(new AddAxiom(owlOnt, domainAx));
		}
		*/
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyRange()
	 */
	@Override
	protected void setPropertyRange()
	{
		// noop, since we are dropping the actual reified property
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyInverse()
	 */
	@Override
	protected void setPropertyInverse()
	{
		// noop, since we are dropping the actual reified property
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyCharacteristics()
	 */
	@Override
	protected void setPropertyCharacteristics()
	{
		// noop, since we are dropping the actual reified property
	}

}
