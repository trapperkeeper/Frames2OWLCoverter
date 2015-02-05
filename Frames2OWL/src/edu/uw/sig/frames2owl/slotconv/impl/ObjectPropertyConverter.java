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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLSymmetricObjectPropertyAxiom;

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
 * @date Nov 5, 2013
 *
 */
public class ObjectPropertyConverter extends BaseSlotValueConverter
{
	protected OWLObjectProperty property;

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 */
	public ObjectPropertyConverter(KnowledgeBase framesKB, Slot framesSlot,
			OWLOntology owlOnt, IRIUtils iriUtils, ConvUtils convUtils)
	{
		super(framesKB, framesSlot, owlOnt, iriUtils, convUtils);
		
		/*
		boolean initSuccess = init();
		if(!initSuccess)
			System.exit(-1);
			*/
	}
	
	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#init(Map<String,String>)
	 */
	@Override
	public boolean init(Map<String,String> initArgs)
	{
		if(!super.init(initArgs))
			return false;
		
		property = df.getOWLObjectProperty(propIRI);
		OWLAxiom declAx = df.getOWLDeclarationAxiom(property);
		man.applyChange(new AddAxiom(owlOnt, declAx));
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		// TODO: This method may be incomplete !!!!
		// get OWL class
		if(framesSlot.getValueType().equals(ValueType.CLS))
		{
			// first handle slots with value type Cls
			for(Cls currCls : (Collection<Cls>)framesCls.getDirectOwnSlotValues(framesSlot))
			{
				IRI clsIRI;
				try
				{
					clsIRI = iriUtils.getIRIForFrame(currCls);
				}
				catch (IRIGenerationException e)
				{
					//e.printStackTrace();
					continue;
				}
				if(clsIRI==null)
					continue;
				OWLClass owlTargetCls = df.getOWLClass(clsIRI);
				//OWLObjectUnionOf union = df.getOWLObjectUnionOf(rangeClses);
				OWLObjectSomeValuesFrom existRestr = df.getOWLObjectSomeValuesFrom(property, owlTargetCls);
				OWLAxiom ax = df.getOWLSubClassOfAxiom(owlCls, existRestr);
				//OWLAxiom ax = df.getOWLObjectPropertyRangeAxiom(property, union);
				man.applyChange(new AddAxiom(owlOnt, ax));
			}
		}
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#createSubPropertyAxioms()
	 */
	@Override
	protected void createSubPropertyAxioms()
	{
		Collection<Slot> parentSlots = framesSlot.getDirectSuperslots();
		Collection<IRI> parentPropIRIs = new HashSet<IRI>();

		for(Slot parentSlot : parentSlots)
		{
			if(!parentSlot.isSystem())
			{
				try
				{
					parentPropIRIs.add(iriUtils.getIRIForFrame(parentSlot));
				}
				catch (IRIGenerationException e)
				{
					e.printStackTrace();
					continue;
				}
			}
		}
		/*
		if(parentPropIRIs.isEmpty())
		{
			parentPropIRIs.add(df.getOWLTopObjectProperty().getIRI());	
		}
		*/

    	for(IRI parentPropIRI : parentPropIRIs)
		{
    		OWLObjectProperty owlParentObjProp = df.getOWLObjectProperty(parentPropIRI);
			OWLAxiom subObjPropAx = df.getOWLSubObjectPropertyOfAxiom(property, owlParentObjProp);
			man.applyChange(new AddAxiom(owlOnt, subObjPropAx));
		}
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyDomain()
	 */
	@Override
	protected void setPropertyDomain()
	{		
		// add domain test
		//Collection<Cls> domain = new HashSet<Cls>(framesSlot.getDirectDomain());
		Queue<Cls> framesDomainQueue = new LinkedList<Cls>(framesSlot.getDirectDomain());
		
		Set<OWLClassExpression> domainClses = new HashSet<OWLClassExpression>();
		while(!framesDomainQueue.isEmpty())
		{
			Cls currFramesDomainCls = framesDomainQueue.remove();
			if(!convUtils.isRegularCls(currFramesDomainCls))
			{
				System.err.println("found non-regular class in domain for "+framesSlot.getName()+": "+
						currFramesDomainCls.getName());
				Set<Cls> regDomain = convUtils.getRegularRootsForTemplateCls(currFramesDomainCls);
				System.err.println("would instead use "+regDomain);
				framesDomainQueue.addAll(regDomain);
				continue;
			}
			
			IRI clsIRI;
			try
			{
				clsIRI = iriUtils.getIRIForFrame(currFramesDomainCls);				
			}
			catch (IRIGenerationException e)
			{
				//e.printStackTrace();
				continue;
			}
			OWLClass clsForIRI = df.getOWLClass(clsIRI);
			domainClses.add(clsForIRI);
		}
		
		if(domainClses.size()>1)
		{
			OWLObjectUnionOf union = df.getOWLObjectUnionOf(domainClses);
			OWLAxiom ax = df.getOWLObjectPropertyDomainAxiom(property, union);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		else if(domainClses.size()==1)
		{
			// single domain class
			OWLClassExpression domainClsExp = domainClses.iterator().next();
			OWLAxiom ax = df.getOWLObjectPropertyDomainAxiom(property, domainClsExp);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		
		// handle min cardinality
		int minCardVal = framesSlot.getMinimumCardinality();
		if(minCardVal>0)
		{
			OWLObjectMinCardinality minCard = df.getOWLObjectMinCardinality(minCardVal, property);
			OWLAxiom ax = df.getOWLObjectPropertyDomainAxiom(property, minCard);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		
		// handle max cardinality
		int maxCardVal = framesSlot.getMaximumCardinality();
		if(maxCardVal>1)
		{
			OWLObjectMaxCardinality maxCard = df.getOWLObjectMaxCardinality(maxCardVal, property);
			OWLAxiom ax = df.getOWLObjectPropertyDomainAxiom(property, maxCard);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		
		return;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyRange()
	 */
	@Override
	protected void setPropertyRange()
	{
		// add range test
		//Collection<Cls> range = framesSlot.getAllowedParents();
		Queue<Cls> framesRangeQueue = new LinkedList<Cls>(framesSlot.getAllowedParents());
		
		Set<OWLClassExpression> rangeClses = new HashSet<OWLClassExpression>();
		//for(Cls rangeCls : range)
		while(!framesRangeQueue.isEmpty())
		{
			Cls rangeCls = framesRangeQueue.remove();
			if(!convUtils.isRegularCls(rangeCls))
			{
				System.err.println("found non-regular class in range for "+framesSlot.getName()+": "+
						rangeCls.getName());
				Set<Cls> regRange = convUtils.getRegularRootsForTemplateCls(rangeCls);
				System.err.println("would instead use "+regRange);
				framesRangeQueue.addAll(regRange);
				continue;
			}
			
			IRI clsIRI;
			try
			{
				clsIRI = iriUtils.getIRIForFrame(rangeCls);
			}
			catch (IRIGenerationException e)
			{
				//e.printStackTrace();
				continue;
			}
			OWLClass clsForIRI = df.getOWLClass(clsIRI);
			rangeClses.add(clsForIRI);
		}
		
		if(rangeClses.size()>1)
		{
			OWLObjectUnionOf union = df.getOWLObjectUnionOf(rangeClses);
			OWLAxiom ax = df.getOWLObjectPropertyRangeAxiom(property, union);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		else if(rangeClses.size()==1)
		{
			// single range class
			OWLClassExpression rangeClsExp = rangeClses.iterator().next();
			OWLAxiom ax = df.getOWLObjectPropertyRangeAxiom(property, rangeClsExp);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
	}
	
	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyInverse()
	 */
	@Override
	protected void setPropertyInverse()
	{
		Slot inverseSlot = framesSlot.getInverseSlot();
		if(inverseSlot==null)
			return;
		
		IRI invPropIRI;
		try
		{
			invPropIRI = iriUtils.getIRIForFrame(inverseSlot);
		}
		catch (IRIGenerationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}
		OWLObjectProperty invProperty = df.getOWLObjectProperty(invPropIRI);
		OWLAxiom ax = df.getOWLInverseObjectPropertiesAxiom(property, invProperty);
		man.applyChange(new AddAxiom(owlOnt, ax));
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyCharacteristics()
	 */
	@Override
	protected void setPropertyCharacteristics()
	{
		/*
		 *  Considered characteristics: functional, inverse functional, transitive, symmetric, asymmetric,
		 *  reflexive, irreflexive
		 *  
		 *  Those that could actually be determined from Frames model: symmetric (in the limited
		 *  case of a slot that has itself as an inverse) and function (sort of, if we assume that no two class 
		 *  names ever refer to the same class intension).
		 */
		
		super.setPropertyCharacteristics();
		
		// first up symmetric
		Slot inverseSlot = framesSlot.getInverseSlot();
		if(inverseSlot!=null && inverseSlot.equals(framesSlot))
		{
			// set owl property to be symmetric
			OWLSymmetricObjectPropertyAxiom symmAx = df.getOWLSymmetricObjectPropertyAxiom(property);
			man.applyChange(new AddAxiom(owlOnt, symmAx));
		}
		
		// now handle functional properties
		int maxCard = framesSlot.getMaximumCardinality();
		if(maxCard==1)
		{
			// set owl property to be functional
			OWLFunctionalObjectPropertyAxiom funcAx = df.getOWLFunctionalObjectPropertyAxiom(property);
			man.applyChange(new AddAxiom(owlOnt, funcAx));
		}
		
		// handle other cardinality restrictions
		/*
		int minCard = framesSlot.getMinimumCardinality();
		if(minCard!=0)
		{
			OWLObjectMinCardinality minObjCard = df.getOWLObjectMinCardinality(minCard, property);
		}
		*/
	}
}
