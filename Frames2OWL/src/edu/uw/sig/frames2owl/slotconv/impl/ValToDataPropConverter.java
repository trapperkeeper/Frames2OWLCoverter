/**
 * 
 */
package edu.uw.sig.frames2owl.slotconv.impl;

import java.util.Set;

import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Mar 11, 2014
 *
 */
public class ValToDataPropConverter extends MappedDataPropertyConverter
{

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param newPropName
	 * @param owlOnt
	 * @param iriUtils
	 * @param convUtils
	 * @param newDomainClassExprs
	 */
	public ValToDataPropConverter(KnowledgeBase framesKB, Slot framesSlot,
			String newPropName, OWLOntology owlOnt, IRIUtils iriUtils,
			ConvUtils convUtils, Set<OWLClassExpression> newDomainClassExprs)
	{
		super(framesKB, framesSlot, newPropName, owlOnt, iriUtils, convUtils,
				newDomainClassExprs);
	}
	
	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyCharacteristics()
	 */
	@Override
	protected void setPropertyCharacteristics()
	{
		// NOOP for now, might need to add something to here later
	}

}
