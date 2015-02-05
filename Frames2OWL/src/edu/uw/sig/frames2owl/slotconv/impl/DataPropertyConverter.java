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
import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataMaxCardinality;
import org.semanticweb.owlapi.model.OWLDataMinCardinality;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;

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
public class DataPropertyConverter extends BaseSlotValueConverter
{
	protected OWLDataProperty property;
	
	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 */
	public DataPropertyConverter(KnowledgeBase framesKB, Slot framesSlot,
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
		
		property = df.getOWLDataProperty(propIRI);
		OWLAxiom declAx = df.getOWLDeclarationAxiom(property);
		man.applyChange(new AddAxiom(owlOnt, declAx));
		
		return true;
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#convertSlot()
	 */
	@Override
	public void convertSlot()
	{
		// TODO Auto-generated method stub
		super.convertSlot();
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
			parentPropIRIs.add(df.getOWLTopDataProperty().getIRI());
		}
		*/
		
    	for(IRI parentPropIRI : parentPropIRIs)
		{
    		OWLDataProperty owlParentDataProp = df.getOWLDataProperty(parentPropIRI);
			OWLAxiom subDataPropAx = df.getOWLSubDataPropertyOfAxiom(property, owlParentDataProp);
			man.applyChange(new AddAxiom(owlOnt, subDataPropAx));
		}
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyDomain()
	 */
	@Override
	protected void setPropertyDomain()
	{
		// add domain test
		//Collection<Cls> domain = framesSlot.getDirectDomain();
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
			OWLAxiom ax = df.getOWLDataPropertyDomainAxiom(property, union);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		else if(domainClses.size()==1)
		{
			// single domain class
			OWLClassExpression domainClsExp = domainClses.iterator().next();
			OWLAxiom ax = df.getOWLDataPropertyDomainAxiom(property, domainClsExp);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		
		// handle min cardinality
		int minCardVal = framesSlot.getMinimumCardinality();
		if(minCardVal>0)
		{
			OWLDataMinCardinality minCard = df.getOWLDataMinCardinality(minCardVal, property);
			OWLAxiom ax = df.getOWLDataPropertyDomainAxiom(property, minCard);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		
		// handle max cardinality
		int maxCardVal = framesSlot.getMaximumCardinality();
		if(maxCardVal>1)
		{
			OWLDataMaxCardinality maxCard = df.getOWLDataMaxCardinality(maxCardVal, property);
			OWLAxiom ax = df.getOWLDataPropertyDomainAxiom(property, maxCard);
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
		ValueType slotValType = framesSlot.getValueType();
		if(slotValType.equals(ValueType.BOOLEAN))
		{
			OWLAxiom ax = df.getOWLDataPropertyRangeAxiom(property, df.getBooleanOWLDatatype());
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		else if(slotValType.equals(ValueType.INTEGER))
		{
			
			OWLAxiom ax = df.getOWLDataPropertyRangeAxiom(property, df.getIntegerOWLDatatype());
			man.applyChange(new AddAxiom(owlOnt, ax));
			
			// check min and max values if present
			Number minVal = framesSlot.getMinimumValue();
			Number maxVal = framesSlot.getMaximumValue();
			if(minVal!=null)
			{
				OWLDatatypeRestriction restr = df.getOWLDatatypeMinInclusiveRestriction(minVal.intValue());
				OWLAxiom minAx = df.getOWLDataPropertyRangeAxiom(property, restr);
				man.applyChange(new AddAxiom(owlOnt, minAx));
			}
			if(maxVal!=null)
			{
				OWLDatatypeRestriction restr = df.getOWLDatatypeMaxInclusiveRestriction(maxVal.intValue());
				OWLAxiom maxAx = df.getOWLDataPropertyRangeAxiom(property, restr);
				man.applyChange(new AddAxiom(owlOnt, maxAx));
			}
		}
		else if(slotValType.equals(ValueType.FLOAT))
		{
			OWLAxiom ax = df.getOWLDataPropertyRangeAxiom(property, df.getFloatOWLDatatype());
			man.applyChange(new AddAxiom(owlOnt, ax));
			
			// check min and max values if present
			Number minVal = framesSlot.getMinimumValue();
			Number maxVal = framesSlot.getMaximumValue();
			if(minVal!=null)
			{
				OWLDatatypeRestriction restr = df.getOWLDatatypeMinInclusiveRestriction(minVal.doubleValue());
				OWLAxiom minAx = df.getOWLDataPropertyRangeAxiom(property, restr);
				man.applyChange(new AddAxiom(owlOnt, minAx));
			}
			if(maxVal!=null)
			{
				OWLDatatypeRestriction restr = df.getOWLDatatypeMaxInclusiveRestriction(maxVal.doubleValue());
				OWLAxiom maxAx = df.getOWLDataPropertyRangeAxiom(property, restr);
				man.applyChange(new AddAxiom(owlOnt, maxAx));
			}
		}
		else if(slotValType.equals(ValueType.STRING))
		{
			OWLDatatype stringDatatype = df.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
			OWLAxiom ax = df.getOWLDataPropertyRangeAxiom(property, stringDatatype);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		else if(slotValType.equals(ValueType.SYMBOL))
		{
			// enumeration of values
			Set<OWLLiteral> lits = new HashSet<OWLLiteral>();
			for(Object valObj : framesSlot.getAllowedValues())
			{
				OWLLiteral lit = df.getOWLLiteral(valObj.toString());
				lits.add(lit);
			}
			OWLDataOneOf oneOf = df.getOWLDataOneOf(lits);
			OWLAxiom ax = df.getOWLDataPropertyRangeAxiom(property, oneOf);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyInverse()
	 */
	@Override
	protected void setPropertyInverse()
	{
		// data properties do not have inverses
		
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		ValueType valType = framesSlot.getValueType();
		
		/*
		IRI owlPropIRI = property.getIRI();
		
		for(Object val : framesCls.getDirectOwnSlotValues(framesSlot))
		{
			OWLClassExpression expr = convUtils.genClassExprForVal(valType, val, owlPropIRI);
			OWLAxiom ax = df.getOWLSubClassOfAxiom(owlCls, expr);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		*/
		
		 // check type for direct template values
		Cls type = framesCls.getDirectType();
		Collection vals = new ArrayList(type.getDirectTemplateSlotValues(framesSlot));
		
		vals.addAll(framesCls.getDirectOwnSlotValues(framesSlot));
		
		for(Object val : vals)
		{
			OWLClassExpression expr = convUtils.genClassExprForVal(valType, val, propIRI);
			OWLAxiom ax = df.getOWLSubClassOfAxiom(owlCls, expr);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
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
		
		// data properties cannot be symmetric
		
		// now handle functional properties
		int maxCard = framesSlot.getMaximumCardinality();
		if(maxCard==1)
		{
			// set owl property to be functional
			OWLFunctionalDataPropertyAxiom funcAx = df.getOWLFunctionalDataPropertyAxiom(property);
			man.applyChange(new AddAxiom(owlOnt, funcAx));
		}
	}

}
