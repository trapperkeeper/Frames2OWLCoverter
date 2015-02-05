/*******************************************************************************
 * Copyright (c) 2015 University of Washington Structural Informatics Group.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/
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
