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
package edu.uw.sig.frames2owl.instconv.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntology;


import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.instconv.BaseInstanceConverter;
import edu.uw.sig.frames2owl.instconv.InstanceConverter;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date May 29, 2014
 *
 */
public class ExternalTermConverter extends BaseInstanceConverter
{
	private Slot nameSlot;
	private Slot mapSlot;
	//private OWLAnnotationProperty mapsToProperty;
	private Set<Slot> excludedSlots = new HashSet<Slot>();
	
	public ExternalTermConverter(KnowledgeBase framesKB, Cls framesRootCls, OWLOntology owlOnt, IRIUtils iriUtils, 
			ConvUtils convUtils)
	{
		super(framesKB, framesRootCls, owlOnt, iriUtils, convUtils);
	}
	

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.instconv.InstanceConverter#init(java.util.Map)
	 */
	@Override
	public boolean init(Map<String, String> initArgs)
	{
		boolean superInit = super.init(initArgs);	
		if(!superInit)
			return false;
		
		String nameSlotName = initArgs.get("name_slot");
		String mapSlotName = initArgs.get("map_slot");
		
		nameSlot = framesKB.getSlot(nameSlotName);
		mapSlot = framesKB.getSlot(mapSlotName);
		
		if(nameSlot==null||mapSlot==null)
			return false;
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.instconv.InstanceConverter#convertInst()
	 */
	@Override
	public void convertInsts()
	{
		/*
		 * <inst_conv_class type_name="Craniofacial external term" 
			conv_cls_name="edu.uw.sig.frames2owl.instconv.impl.ExternalTermConverter" 
			name_slot="External term name" 
			map_slot="OCDM equivalent" 
			direct_property_name="externalTerm" 
			excluded_slots=""/>
		 */
		
		Collection<Instance> instances = rootCls.getInstances();
		for(Instance instance : instances)
		{
			// first create primary mapping annotation
			
			// get frames source and target
			Cls source = (Cls)instance.getOwnSlotValue(mapSlot);
			if(source==null)
			{
				System.err.println("Error, external term found with no related OCDM class: "+instance.getBrowserText());
				continue;
			}
			String term = (String)instance.getOwnSlotValue(nameSlot);
			
			// get OWL equivalents for source and target
			IRI sourceIRI = null;
			try
			{
				sourceIRI = iriUtils.getIRIForFrame(source);
			}
			catch (IRIGenerationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(sourceIRI==null)
			{
				System.err.println("Error, null IRI in external term source");
			}
			
			OWLAnnotationValue termVal = df.getOWLLiteral(term);
			OWLAnnotation primaryExternalTermAnnot = df.getOWLAnnotation(
					df.getOWLAnnotationProperty(propIRI),
					termVal);
			
			// create annotations on previous annotation
			Set<OWLAnnotation> annotSet = new HashSet<OWLAnnotation>();
			for (Slot slot : (Collection<Slot>) instance.getOwnSlots())
			{
				if (slot.isSystem() 
						|| slot.equals(nameSlot) 
						|| slot.equals(mapSlot)
						|| excludedSubSlots.contains(slot)
						|| instance.getOwnSlotValueCount(slot) == 0)
					continue;

				Collection values = instance.getOwnSlotValues(slot);
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
			
			OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(sourceIRI, primaryExternalTermAnnot);
			OWLAxiom annotOnAnnotAxiom = axiom.getAnnotatedAxiom(annotSet);

			// add the axioms to the ontology.
			AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
			AddAxiom addAnnotOnAnnotAxiom = new AddAxiom(owlOnt, annotOnAnnotAxiom);

			// We now use the manager to apply the change
			man.applyChange(addAxiom);
			man.applyChange(addAnnotOnAnnotAxiom);
		}
	}

}
