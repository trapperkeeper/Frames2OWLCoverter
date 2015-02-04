/**
 * 
 */
package edu.uw.sig.frames2owl.slotconv;

import java.util.HashSet;
import java.util.Set;

import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLOntology;


import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Mar 10, 2014
 *
 */
public abstract class BaseReifiedPropertyConverter extends BaseSlotValueConverter
{
	protected Set<Slot> excludedSubSlots = new HashSet<Slot>();

	public BaseReifiedPropertyConverter(KnowledgeBase framesKB, Slot framesSlot,
			OWLOntology owlOnt, IRIUtils iriUtils, ConvUtils convUtils)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
	}
	
	public void addExcludedSlots(Set<Slot> excSlots)
	{
		this.excludedSubSlots.addAll(excSlots);
	}
}
