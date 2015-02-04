/**
 * 
 */
package edu.uw.sig.frames2owl.slotconv;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;

/**
 * @author detwiler
 * @date Oct 17, 2013
 *
 */
public interface SlotValueConverter
{
	public boolean init(Map<String,String> initArgs);
	public void convertSlot();
	public void convertSlotValues(Cls framesCls, OWLClass owlCls);
}
