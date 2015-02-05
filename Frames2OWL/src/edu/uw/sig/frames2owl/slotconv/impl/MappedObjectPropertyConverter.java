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

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Mar 4, 2014
 *
 */
public class MappedObjectPropertyConverter extends ObjectPropertyConverter
{
	private String newPropName = null;
	private Set<OWLClassExpression> newDomainClassExprs = new HashSet<OWLClassExpression>();

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 * @param convUtils
	 */
	public MappedObjectPropertyConverter(KnowledgeBase framesKB, Slot framesSlot, String newPropName,
			OWLOntology owlOnt, IRIUtils iriUtils, ConvUtils convUtils, 
			Set<OWLClassExpression> newDomainClassExprs)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
		this.newPropName = newPropName;
		this.newDomainClassExprs = newDomainClassExprs;
	}

	
	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyDomain()
	 */
	@Override
	protected void setPropertyDomain()
	{
		ValueType valType = framesSlot.getValueType();
		OWLProperty currProp = df.getOWLObjectProperty(propIRI);
		OWLAxiom domainAx = convUtils.getPropertyDomainAxiom(newDomainClassExprs, currProp);
		if(domainAx!=null)
			man.applyChange(new AddAxiom(owlOnt, domainAx));
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#init(java.util.Map)
	 */
	@Override
	public boolean init(Map<String, String> initArgs)
	{
		initArgs.put("newPropName", newPropName);
		return super.init(initArgs);
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
