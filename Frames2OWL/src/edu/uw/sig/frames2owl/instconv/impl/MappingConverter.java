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
public class MappingConverter extends BaseInstanceConverter
{
	private Slot sourceSlot;
	private Slot targetSlot;
	private OWLAnnotationProperty mapsToProperty;
	private Set<Slot> excludedSlots = new HashSet<Slot>();
	
	public MappingConverter(KnowledgeBase framesKB, Cls framesRootCls, OWLOntology owlOnt, IRIUtils iriUtils, 
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
		
		String sourceSlotName = initArgs.get("source_slot");
		String targetSlotName = initArgs.get("target_slot");
		
		sourceSlot = framesKB.getSlot(sourceSlotName);
		targetSlot = framesKB.getSlot(targetSlotName);
		
		if(sourceSlot==null||targetSlot==null)
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
		 * <inst_conv_class type_name="Mapping" 
			conv_cls_name="edu.uw.sig.frames2owl.instconv.impl.MappingConverter" 
			source_slot="source" 
			target_slot="target" 
			direct_property_name="mapsTo" 
			excluded_slots=""/>
		 */
		
		Collection<Instance> instances = rootCls.getInstances();
		for(Instance instance : instances)
		{
			// first create primary mapping annotation
			
			// get frames source and target
			Cls source = (Cls)instance.getOwnSlotValue(sourceSlot);
			if(source==null)
			{
				System.err.println("Error, mapping found with no source: "+instance.getBrowserText());
				continue;
			}
			Cls target = (Cls)instance.getOwnSlotValue(targetSlot);
			
			// get OWL equivalents for source and target
			IRI sourceIRI = null;
			IRI targetIRI = null;
			try
			{
				sourceIRI = iriUtils.getIRIForFrame(source);
				if(target!=null)
					targetIRI = iriUtils.getIRIForFrame(target);
				else
					targetIRI = df.getOWLNothing().getIRI();
			}
			catch (IRIGenerationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(sourceIRI==null||targetIRI==null)
			{
				System.err.println("Error, null IRI in either source or target of a mapping");
			}
			
			OWLAnnotation primaryMappingAnnot = df.getOWLAnnotation(
					df.getOWLAnnotationProperty(propIRI),
					targetIRI);
			
			// create annotations on previous annotation
			Set<OWLAnnotation> annotSet = new HashSet<OWLAnnotation>();
			for (Slot slot : (Collection<Slot>) instance.getOwnSlots())
			{
				if (slot.isSystem() 
						|| slot.equals(sourceSlot) 
						|| slot.equals(targetSlot)
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
			
			OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(sourceIRI, primaryMappingAnnot);
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
