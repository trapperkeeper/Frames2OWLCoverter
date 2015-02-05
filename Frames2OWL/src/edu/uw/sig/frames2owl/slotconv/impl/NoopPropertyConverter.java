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
