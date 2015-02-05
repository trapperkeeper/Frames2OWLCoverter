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

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;


import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.slotconv.BaseReifiedPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Mar 17, 2014
 * 
 */
public class ReifiedAnnotationPropertyConverter extends
		BaseReifiedPropertyConverter
{

	// properties in frames
	private Slot primarySlot;
	
	//private Map<Slot,AnnotationPropertyConverter> converterMap = new HashMap<Slot,AnnotationPropertyConverter>();

	/**
	 * @param framesKB
	 * @param owlOnt
	 * @param owlUriString
	 */
	public ReifiedAnnotationPropertyConverter(KnowledgeBase framesKB,
			Slot framesSlot, OWLOntology owlOnt, IRIUtils iriUtils,
			ConvUtils convUtils)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#init(Map<String
	 * ,String>)
	 */
	@Override
	public boolean init(Map<String, String> initArgs)
	{
		if (!super.init(initArgs))
			return false;

		String primarySlotName = initArgs.get("primary_slot");
		primarySlot = framesKB.getSlot(primarySlotName);

		if (primarySlot == null)
			return false;
		
		// determine if any slots are to be omitted
		String excludedSlotNames = initArgs.get("excluded_slots");
		if(excludedSlotNames!=null&&!excludedSlotNames.equals(""))
		{
			for(String excludedSlotName : excludedSlotNames.split(","))
			{
				Slot excludedSlot = framesKB.getSlot(excludedSlotName);
				if(excludedSlot!=null)
					excludedSubSlots.add(excludedSlot);
				else
				{
					System.err.println("could not locate excluded slot indicated in config: "+excludedSlotName);
					return false;
				}
			}
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * edu.uw.sig.frames2owl.slotconv.SlotValueConverter#convertSlotValues(edu
	 * .stanford.smi.protege.model.Slot,
	 * org.semanticweb.owlapi.model.OWLProperty)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		// get reified instances
		Collection<Instance> reifInsts = framesCls.getDirectOwnSlotValues(framesSlot);

		// NOTE: we are not currently using the owl class that was passed in,
		// but creating a URI from the frames class
		// TODO: I should be more consistent with this
		for (Instance reifInst : reifInsts)
			createAnnotOnAnnot(framesCls, framesSlot, reifInst);
	}

	/**
	 * For instance of concept name from frames, create an annotation on an
	 * annotation in owl
	 * 
	 * @param ref
	 *            concept name reference
	 */
	public void createAnnotOnAnnot(Frame sourceCls, Slot reifSlot,
			Instance reifInst)
	{
		// Now create the annotation axiom
		IRI subjectIRI, propIRI = null;
		try
		{
			subjectIRI = iriUtils.getIRIForFrame(sourceCls); 
			propIRI = iriUtils.getIRIForSlot(reifSlot); 
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
			return;
		}

		// make sure reified instance has a value for the primary slot
		Object primaryVal = reifInst.getDirectOwnSlotValue(primarySlot);
		ValueType primaryValType = primarySlot.getValueType();
		if (primaryVal == null)
			return;
		OWLAnnotationValue primaryAnnotVal = convUtils.getAnnotValForFramesVal(primaryValType, primaryVal);

		OWLAnnotation primaryAnnot = df.getOWLAnnotation(
				df.getOWLAnnotationProperty(propIRI),
				primaryAnnotVal);

		// create annotations on previous annotation
		Set<OWLAnnotation> annotSet = new HashSet<OWLAnnotation>();
		for (Slot slot : (Collection<Slot>) reifInst.getOwnSlots())
		{
			if (slot.isSystem() || slot.equals(primarySlot) || excludedSubSlots.contains(slot)
					|| reifInst.getOwnSlotValueCount(slot) == 0)
				continue;
			
			/*
			// if we haven't generated the base configuration for subslot already, do so now
			AnnotationPropertyConverter slotConv = converterMap.get(slot);
			if(slotConv==null)
			{
				// create new converter and run convertSlot method
				slotConv = new AnnotationPropertyConverter(framesKB,framesSlot,owlOnt,iriUtils,convUtils);
				
				// initialize converter
				// TODO: should the init args come from config?
				slotConv.init(new HashMap<String,String>());
				
				// convert slot (not values)
				slotConv.convertSlot();
			}
			*/

			Collection values = reifInst.getDirectOwnSlotValues(slot);
			IRI slotIRI;
			try
			{
				slotIRI = iriUtils.getIRIForSlot(slot); // getIRIForSlot(slot);
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				continue;
			}

			ValueType valType = slot.getValueType();
			for (Object valObj : values)
			{
				OWLAnnotationValue annotVal = convUtils.getAnnotValForFramesVal(valType, valObj);
				OWLAnnotation annot = df.getOWLAnnotation(
						df.getOWLAnnotationProperty(slotIRI),
						annotVal);
				annotSet.add(annot);
			}
		}
		
		OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(subjectIRI, primaryAnnot);
		OWLAxiom annotOnAnnotAxiom = axiom.getAnnotatedAxiom(annotSet);

		// add the axioms to the ontology.
		AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
		AddAxiom addAnnotOnAnnotAxiom = new AddAxiom(owlOnt, annotOnAnnotAxiom);

		// We now use the manager to apply the change
		man.applyChange(addAxiom);
		man.applyChange(addAnnotOnAnnotAxiom);
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#createSubPropertyAxioms()
	 */
	@Override
	protected void createSubPropertyAxioms()
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyDomain()
	 */
	@Override
	protected void setPropertyDomain()
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyRange()
	 */
	@Override
	protected void setPropertyRange()
	{
		// TODO Auto-generated method stub
		
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyInverse()
	 */
	@Override
	protected void setPropertyInverse()
	{
		// TODO Auto-generated method stub
		
	}

}
