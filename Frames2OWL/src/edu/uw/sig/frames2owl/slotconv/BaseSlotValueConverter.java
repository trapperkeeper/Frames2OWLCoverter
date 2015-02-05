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

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLInverseObjectPropertiesAxiom;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLProperty;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Oct 23, 2013
 *
 */
public abstract class BaseSlotValueConverter implements SlotValueConverter
{
	public enum PropType {
	    OBJECTPROPERTY, DATAPROPERTY, ANNOTATIONPROPERTY 
	}
	
	protected Slot framesSlot = null;
	protected KnowledgeBase framesKB = null;
	protected OWLOntology owlOnt = null;
	protected IRIUtils iriUtils = null;
	protected ConvUtils convUtils = null;
	
	protected OWLOntologyManager man;
	protected OWLDataFactory df;
	
	//protected PropType propType;
	protected IRI propIRI;
	
	protected boolean isCreated = false;
	
	/**
	 * Base constructor
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 * @param convUtils
	 */
	public BaseSlotValueConverter(KnowledgeBase framesKB, Slot framesSlot, OWLOntology owlOnt, IRIUtils iriUtils, 
			ConvUtils convUtils)
	{
		this.framesKB = framesKB;
		this.framesSlot = framesSlot;
		this.owlOnt = owlOnt;
		this.iriUtils = iriUtils;
		this.convUtils = convUtils;
	}
	
	public boolean init(Map<String,String> initArgs)
	{
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		
		try
		{
			if(initArgs.containsKey("newPropName"))
			{
				String newPropName = initArgs.get("newPropName");
				propIRI = iriUtils.getIRIForString(newPropName); 
			}
			else
				propIRI = iriUtils.getIRIForFrame(framesSlot);
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
			return false;
		}
		if(propIRI==null)
		{
			System.err.println("Null IRI for slot "+framesSlot+" in BaseSlotValueConverter.init()");
			return false;
		}
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.SlotValueConverter#convertSlot(edu.stanford.smi.protege.model.Slot)
	 */
	@Override
	public void convertSlot()
	{
		if(isCreated)
			return;
		
		isCreated = true;
		
		createSubPropertyAxioms();
		setPropertyDomain();
		setPropertyRange();
		setPropertyInverse();
		setPropertyCharacteristics();
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.SlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, edu.stanford.smi.protege.model.Slot, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public abstract void convertSlotValues(Cls framesCls, OWLClass owlCls);
	
	protected abstract void createSubPropertyAxioms();	
	protected abstract void setPropertyDomain();
	protected abstract void setPropertyRange();
	protected abstract void setPropertyInverse();
	
	protected void setPropertyCharacteristics()
	{
		Collection<Slot> charSlots = framesSlot.getOwnSlots();
		for(Slot charSlot : charSlots)
		{
			if(charSlot.isSystem()) // should eventually check and see if slot is excluded in config
				continue;
			
			Collection framesVals = framesSlot.getDirectOwnSlotValues(charSlot);
			ValueType framesValType = charSlot.getValueType();
			//IRI owlCharPropIRI = iriUtils.getIRIForSlot(charSlot);
			OWLAnnotationProperty owlCharProp = convUtils.getOWLAnnotationPropertyForSlot(charSlot);
			
			for(Object framesVal : framesVals)
			{
				OWLAnnotationValue annotVal =  convUtils.getAnnotValForFramesVal(framesValType, framesVal);
				OWLAnnotation annot = df.getOWLAnnotation(owlCharProp, annotVal);
				OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(propIRI,annot);
				
				// add the axioms to the ontology.
				AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
				
				// We now use the manager to apply the change
				man.applyChange(addAxiom);
			}
		}
		
	}
	
}
