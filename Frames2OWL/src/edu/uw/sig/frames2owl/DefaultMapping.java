package edu.uw.sig.frames2owl;

import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLProperty;

import edu.stanford.smi.protege.model.Slot;

/**
 * @author detwiler
 * @date Jan 2, 2013
 *
 */
public class DefaultMapping implements Mapping
{
	Slot slotLabelSlot = null;
	OWLAnnotationProperty propLabelProp = null;
	
	boolean isCaseSensitive = true;
	
	Slot fromSlot = null;
	OWLProperty toProp = null;
	
	public DefaultMapping(String fromSlotName, boolean isCaseSensititve)
	{
		this.isCaseSensitive = isCaseSensititve;
		
		// stuff todo: also should take in name of frames slot label property and similar for owl
		// set up to and from comparison locations, do this in init
		// test to see if I can look up a property using its namespace prefix (i.e. rdfs:label)
	}
	
	/*
	public String getOWLPropConvForSlot(String slot)
	{
		// We will use a very 
	}
	*/
}
