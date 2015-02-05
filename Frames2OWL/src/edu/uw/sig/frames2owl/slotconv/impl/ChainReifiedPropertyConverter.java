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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyExpression;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLProperty;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Jan 13, 2014
 *
 */
public class ChainReifiedPropertyConverter extends ReifiedPropertyConverter
{
	//IRI primaryPropIRI;
	//IRI superPropIRI;
	OWLObjectProperty primaryProp;
	OWLObjectProperty directProp;

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 * @param convUtils
	 */
	public ChainReifiedPropertyConverter(KnowledgeBase framesKB,
			Slot framesSlot, OWLOntology owlOnt, IRIUtils iriUtils,
			ConvUtils convUtils)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.impl.ReifiedPropertyConverter#init(java.util.Map)
	 */
	@Override
	public boolean init(Map<String, String> initArgs)
	{
		if(!super.init(initArgs))
			return false;
		
		String primarySlotName = initArgs.get("primary_slot");
		String superSlotName = initArgs.get("super_slot");
		String directSlotName = initArgs.get("direct_property_name");
		Slot primarySlot = primarySlotName==null?null:framesKB.getSlot(primarySlotName);
		Slot superSlot = superSlotName==null?null:framesKB.getSlot(superSlotName);
		//Slot superSlot = superSlotName==null?null:framesKB.getSlot(superSlotName);
		if(primarySlot==null)
		{
			System.err.println("Error, no slot found for primary_slot in chained property "+framesSlot.getName());
			return false;
		}  
		if(directSlotName==null)
		{
			System.err.println("Error, no super_slot attribute in config for chained property "+framesSlot.getName());
			return false;
		}
		
		try
		{
			IRI primaryPropIRI = iriUtils.getIRIForSlot(primarySlot);
			primaryProp = df.getOWLObjectProperty(primaryPropIRI);
			
			// create super prop
			IRI directPropIRI = iriUtils.getIRIForString(directSlotName);
			directProp = df.getOWLObjectProperty(directPropIRI);
			
			// add new super prop to ontology
			OWLAxiom declAx = df.getOWLDeclarationAxiom(directProp);
			man.applyChange(new AddAxiom(owlOnt, declAx));
			
			// if config suggests that the new prop should have a super prop, make it so
			if(superSlot!=null)
			{
				IRI superPropIRI = iriUtils.getIRIForSlot(superSlot);
				OWLObjectProperty superProperty = df.getOWLObjectProperty(superPropIRI);
				OWLAxiom subPropAx = df.getOWLSubObjectPropertyOfAxiom(directProp, superProperty);
				man.applyChange(new AddAxiom(owlOnt, subPropAx));
			}
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
		}
		if(primaryProp==null||directProp==null)
		{
			System.err.println("Error, either primary or super propery is null in ChainReifiedPropertyConverter for slot "
					+framesSlot);
			return false;
		}
		
		return true;
	}
	/*
	public boolean init(Map<String, String> initArgs)
	{
		if(!super.init(initArgs))
			return false;
		
		String primarySlotName = initArgs.get("primary_slot");
		String superSlotName = initArgs.get("super_slot");
		Slot primarySlot = primarySlotName==null?null:framesKB.getSlot(primarySlotName);
		Slot superSlot = superSlotName==null?null:framesKB.getSlot(superSlotName);
		if(primarySlot==null)
		{
			System.err.println("Error, no slot found for primary_slot in chained property "+framesSlot.getName());
			return false;
		}
		if(superSlot==null)
		{
			System.err.println("Error, no slot found for super_slot in chained property "+framesSlot.getName());
			return false;
		}
		
		try
		{
			IRI primaryPropIRI = iriUtils.getIRIForSlot(primarySlot);
			primaryProp = df.getOWLObjectProperty(primaryPropIRI);
			
			IRI superPropIRI = iriUtils.getIRIForSlot(primarySlot);
			superProp = df.getOWLObjectProperty(superPropIRI);
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
		}
		if(primaryProp==null||superProp==null)
		{
			System.err.println("Error, either primary or super propery is null in ChainReifiedPropertyConverter for slot "
					+framesSlot);
			return false;
		}
		
		return true;
	}
	*/

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#convertSlot()
	 */
	@Override
	public void convertSlot()
	{
		super.convertSlot();
		
		// declare property chain
		List<OWLObjectPropertyExpression> chainPropIRIs = new ArrayList<OWLObjectPropertyExpression>();
		chainPropIRIs.add(this.property);
		chainPropIRIs.add(this.primaryProp);
		
		// create the axiom
		OWLAxiom chainAx = df.getOWLSubPropertyChainOfAxiom(chainPropIRIs, this.directProp);
		
		// add the subclass axiom to the ontology.
		AddAxiom addAxiom = new AddAxiom(owlOnt, chainAx);
		
		// We now use the manager to apply the change
		man.applyChange(addAxiom);
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.impl.ReifiedPropertyConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		super.convertSlotValues(framesCls, owlCls);
		
		// what do I need to do here?
	}
}
