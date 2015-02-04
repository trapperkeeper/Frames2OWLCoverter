/**
 * 
 */
package edu.uw.sig.frames2owl.instconv;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Jun 2, 2014
 *
 */
public class BaseInstanceConverter implements InstanceConverter
{
	protected Cls rootCls = null;
	protected KnowledgeBase framesKB = null;
	protected OWLOntology owlOnt = null;
	protected IRIUtils iriUtils = null;
	protected ConvUtils convUtils = null;
	
	protected Set<Slot> excludedSubSlots = new HashSet<Slot>();
	
	protected OWLOntologyManager man;
	protected OWLDataFactory df;
	
	protected IRI propIRI;
	
	public BaseInstanceConverter(KnowledgeBase framesKB, Cls framesRootCls, OWLOntology owlOnt, IRIUtils iriUtils, 
			ConvUtils convUtils)
	{
		this.framesKB = framesKB;
		this.rootCls = framesRootCls;
		this.owlOnt = owlOnt;
		this.iriUtils = iriUtils;
		this.convUtils = convUtils;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.instconv.InstanceConverter#init(java.util.Map)
	 */
	@Override
	public boolean init(Map<String, String> initArgs)
	{
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		
		try
		{
			if(initArgs.containsKey("direct_property_name"))
			{
				String annotPropName = initArgs.get("direct_property_name");
				propIRI = iriUtils.getIRIForString(annotPropName); 
			}
			else
			{
				//propIRI = iriUtils.getIRIForFrame(framesSlot);
				System.err.println("Warning, mapping instance converter config without direct property name");
				return false;
			}
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
			return false;
		}
		if(propIRI==null)
		{
			System.err.println("Null IRI for cls "+rootCls.getName()+" in BaseInstanceConverter.init()");
			return false;
		}
		
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
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.instconv.InstanceConverter#convertInsts()
	 */
	@Override
	public void convertInsts()
	{
		// TODO Auto-generated method stub

	}

}
