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
