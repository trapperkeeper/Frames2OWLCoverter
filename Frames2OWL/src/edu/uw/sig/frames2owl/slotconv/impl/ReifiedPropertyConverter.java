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
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.slotconv.BaseReifiedPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Dec 10, 2013
 *
 */
public class ReifiedPropertyConverter extends BaseReifiedPropertyConverter
{
	protected OWLObjectProperty property;

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 */
	public ReifiedPropertyConverter(KnowledgeBase framesKB, Slot framesSlot,
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
		
		property = df.getOWLObjectProperty(propIRI);
		OWLAxiom declAx = df.getOWLDeclarationAxiom(property);
		man.applyChange(new AddAxiom(owlOnt, declAx));
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#createSubPropertyAxioms()
	 */
	@Override
	protected void createSubPropertyAxioms()
	{
		// copied from ObjectPropertyConverter, probably not necessary
		// no hierarchy amongst reified slots at present
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
		Collection<Cls> domain = framesSlot.getDirectDomain();
		
		Set<OWLClassExpression> domainClses = new HashSet<OWLClassExpression>();
		for(Cls domainCls : domain)
		{
			IRI clsIRI;
			try
			{
				clsIRI = iriUtils.getIRIForFrame(domainCls);
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
		Collection<Cls> range = framesSlot.getAllowedClses();
		
		Set<OWLClassExpression> rangeClses = new HashSet<OWLClassExpression>();
		for(Cls rangeCls : range)
		{
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
			// single domain class
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
		// NOOP
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		// TODO handle class level range refinements
		
		// create a value class that is a subclass of the range class
		Collection<Instance> vals = framesCls.getDirectOwnSlotValues(framesSlot);
		
		// what was an instance in frames will be a class in OWL (to allow instantiation by individuals)
		for(Instance val : vals)
		{

			OWLClassExpression reifValExpression = createClassExpr(val);
			
			OWLObjectSomeValuesFrom existRestr = 
					df.getOWLObjectSomeValuesFrom(property, reifValExpression);
			OWLAxiom subAx = df.getOWLSubClassOfAxiom(owlCls, existRestr);
			AddAxiom addSubAxiom = new AddAxiom(owlOnt,subAx);
			man.applyChange(addSubAxiom);
			
			// Now set the subclass property
			// get instance type
			Cls type = val.getDirectType();
			IRI typeIRI;
			try
			{
				typeIRI = iriUtils.getIRIForFrame(type);
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				continue;
			}
			OWLClass owlParentClass = df.getOWLClass(typeIRI);
			
			// Now create the subclass axiom
			OWLAxiom axiom = df.getOWLSubClassOfAxiom(reifValExpression,owlParentClass);
			
			// add the subclass axiom to the ontology.
			AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
			
			// We now use the manager to apply the change
			man.applyChange(addAxiom);
		}
		

	}
	
	private OWLClassExpression createClassExpr(Instance val)
	{
		//TODO: need to handle more value types here
		
		
		// instance type will be parent class in OWL

		Set<OWLClassExpression> exprSet = new HashSet<OWLClassExpression>();
		
		Collection<Slot> subSlots = val.getOwnSlots();
		for(Slot subSlot : subSlots)
		{
			if(subSlot.isSystem()||excludedSubSlots.contains(subSlot))
				continue;
			ValueType subValType = subSlot.getValueType();
			IRI subSlotIRI = null;
			try
			{
				subSlotIRI = iriUtils.getIRIForSlot(subSlot);
				
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				continue;
			}
			for(Object subVal : val.getDirectOwnSlotValues(subSlot))
			{
				OWLClassExpression clsExpr = convUtils.genClassExprForVal(subValType, subVal, subSlotIRI);
				if(clsExpr!=null)
					exprSet.add(clsExpr);
			}
		}
		
		// create the intersection of all of the above restrictions
		OWLObjectIntersectionOf iof = df.getOWLObjectIntersectionOf(exprSet);
		
		//OWLAxiom intAx = df.getOWLEquivalentClassesAxiom(iof);
		//man.applyChange(new AddAxiom(owlOnt, intAx));
		return iof;
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
		
		// cannot yield a symmetric relation
		
		// now handle functional properties
		int maxCard = framesSlot.getMaximumCardinality();
		if(maxCard==1)
		{
			// set owl property to be functional
			OWLFunctionalObjectPropertyAxiom funcAx = df.getOWLFunctionalObjectPropertyAxiom(property);
			man.applyChange(new AddAxiom(owlOnt, funcAx));
		}		
	}
	
	/*
	private void copySlotValues(Cls framesCls, OWLClass owlCls)
	{
		Collection<Slot> sourceSlots = framesCls.getOwnSlots();
		for(Slot sourceSlot : sourceSlots)
		{
			SlotValueConverter converter = slot2ConvMap.get(sourceSlot);
			if(converter==null)
				continue;
			converter.convertSlotValues(framesCls, owlCls);
		}
	}
	*/
}
