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
