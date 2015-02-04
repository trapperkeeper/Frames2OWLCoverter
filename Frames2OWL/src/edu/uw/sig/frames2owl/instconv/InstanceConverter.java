/**
 * 
 */
package edu.uw.sig.frames2owl.instconv;

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;

/**
 * @author detwiler
 * @date Oct 17, 2013
 *
 */
public interface InstanceConverter
{
	public boolean init(Map<String,String> initArgs);
	public void convertInsts();
}
