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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
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
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Oct 17, 2013
 *
 */
public class ConceptNameConverter extends AnnotationPropertyConverter
{
	// properties in frames
	private Slot nameSlot;
		
	/**
	 * @param framesKB
	 * @param owlOnt
	 * @param owlUriString
	 */
	public ConceptNameConverter(KnowledgeBase framesKB, Slot framesSlot, OWLOntology owlOnt, IRIUtils iriUtils, ConvUtils convUtils)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
	}
	
	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#init(Map<String,String>)
	 */
	@Override
	public boolean init(Map<String,String> initArgs)
	{
		if(!super.init(initArgs))
			return false;
		
		// frames initialization
		//prefNameSlot = framesKB.getSlot("Preferred name");
		//synonymSlot = framesKB.getSlot("Synonym");
		//nonEngEquivSlot = framesKB.getSlot("Non-English equivalent");
		nameSlot = framesKB.getSlot("name");
		
		if(nameSlot==null)
			return false; 
		
		//super.setPropType(PropType.ANNOTATIONPROPERTY);
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.SlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Slot, org.semanticweb.owlapi.model.OWLProperty)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		// get Concept name instances
		Collection<Instance> conceptNames = framesCls.getDirectOwnSlotValues(framesSlot);
		
		// NOTE: we are not currently using the owl class that was passed in, but creating a URI from the frames class
		// TODO: I should be more consistent with this
		for(Instance conceptName : conceptNames)
			createAnnotOnAnnot(framesCls,framesSlot,conceptName);
	}
	
	/**
	 * For instance of concept name from frames, create an annotation on an annotation in owl
	 * @param ref concept name reference
	 */
	public void createAnnotOnAnnot(Frame sourceCls, Slot nameProp, Instance nameInst)
	{
		// Now create the annotation axiom
		IRI subjectIRI, propIRI = null;
		try
		{
			subjectIRI = iriUtils.getIRIForFrame(sourceCls); //getIRIForCls((Cls)sourceCls);
			propIRI = iriUtils.getIRIForSlot(nameProp); //getIRIForSlot(nameProp);
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
			return;
		}
		
		// make sure concept name has a name
		Object nameObj = nameInst.getDirectOwnSlotValue(nameSlot);
		if(nameObj==null)
			return;
		String name = (String)nameObj;
		
		/*
		 * 
		 * String label = frame.getName();
		OWLAnnotation labelAnnot = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(label, "en"));

		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(owlEnt.getIRI(), labelAnnot);
		man.applyChange(new AddAxiom(owlOnt, ax)); 
		 */
		//pm.getPrefixIRI(":"+nameProp.getName().replaceAll("\\s", "_"));
		OWLAnnotation nameAnnot = df.getOWLAnnotation(df.getOWLAnnotationProperty(propIRI), df.getOWLLiteral(name,""));
		
		
		// create annotations on previous annotation
		Set<OWLAnnotation> annotSet = new HashSet<OWLAnnotation>();
		for(Slot slot : (Collection<Slot>)nameInst.getOwnSlots())
		{
			if(slot.isSystem()||slot.equals(nameSlot)||nameInst.getOwnSlotValueCount(slot)==0)
				continue;
			
			Collection values = nameInst.getDirectOwnSlotValues(slot);
			IRI slotIRI;
			try
			{
				slotIRI = iriUtils.getIRIForSlot(slot); //getIRIForSlot(slot);
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				continue;
			}
			
			for(Object valObj : values)
			{
				String value = valObj.toString();
				OWLAnnotation annot = df.getOWLAnnotation(df.getOWLAnnotationProperty(slotIRI), df.getOWLLiteral(value,""));
				annotSet.add(annot);
			}
			
		}
		OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(subjectIRI, nameAnnot);
		OWLAxiom annotOnAnnotAxiom = axiom.getAnnotatedAxiom(annotSet);
		
		// add the axioms to the ontology.
		AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
		AddAxiom addAnnotOnAnnotAxiom = new AddAxiom(owlOnt,annotOnAnnotAxiom);
		
		// We now use the manager to apply the change
		man.applyChange(addAxiom);
		man.applyChange(addAnnotOnAnnotAxiom);

	}

}
