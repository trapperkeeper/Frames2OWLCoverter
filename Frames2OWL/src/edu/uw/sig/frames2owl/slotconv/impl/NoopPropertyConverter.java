/**
 * 
 */
package edu.uw.sig.frames2owl.slotconv.impl;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;


import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Jan 7, 2014
 *
 */
public class NoopPropertyConverter implements SlotValueConverter
{

	/**
	 * This converter is intended to be used for slots that are configured 
	 * such that there should not be an equivalent counterpart in OWL 
	 * (i.e. dropped slots).
	 */
	public NoopPropertyConverter(KnowledgeBase framesKB, Slot framesSlot,
			OWLOntology owlOnt, IRIUtils iriUtils, ConvUtils convUtils)
	{
		// noop
	}
	
	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#init(Map<String,String>)
	 */
	@Override
	public boolean init(Map<String,String> initArgs)
	{
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.SlotValueConverter#convertSlot()
	 */
	@Override
	public void convertSlot()
	{
		// noop

	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.SlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		// noop

	}

}
