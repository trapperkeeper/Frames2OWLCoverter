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
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataOneOf;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDatatypeDefinitionAxiom;
import org.semanticweb.owlapi.model.OWLDatatypeRestriction;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLFunctionalDataPropertyAxiom;
import org.semanticweb.owlapi.model.OWLFunctionalObjectPropertyAxiom;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectMaxCardinality;
import org.semanticweb.owlapi.model.OWLObjectMinCardinality;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLFacet;

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
public class AnnotationPropertyConverter extends BaseSlotValueConverter
{
	protected OWLAnnotationProperty property;
	
	protected OWLClass domainClass = null;
	protected OWLClass rangeClass = null;
	
	protected OWLClass annotDomainClass = null;
	protected OWLClass annotRangeClass = null;

	/**
	 * @param framesKB
	 * @param framesSlot
	 * @param owlOnt
	 * @param iriUtils
	 */
	public AnnotationPropertyConverter(KnowledgeBase framesKB, Slot framesSlot,
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
		
		property = df.getOWLAnnotationProperty(propIRI);
		OWLAxiom declAx = df.getOWLDeclarationAxiom(property);
		man.applyChange(new AddAxiom(owlOnt, declAx));
		
		//create OWL parent classes to hold named classes for annotation property domains and ranges
		String frag = propIRI.getFragment();
		System.out.println("annot property frag = "+frag);
		
		String propDomainClassName = "Domain_of_"+frag;
		//String propRangeClassName = "Range_of_"+frag;
		IRI annotDomainIRI = null;
		//IRI annotRangeIRI = null;
		try
		{
			annotDomainIRI = iriUtils.getClassIRIForString(propDomainClassName);
			//annotRangeIRI = iriUtils.getClassIRIForString(propRangeClassName);
		}
		catch (IRIGenerationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		if(annotDomainIRI!=null && domainClass!=null)
		{
			annotDomainClass = df.getOWLClass(annotDomainIRI);
			addLabel(annotDomainClass, propDomainClassName);
			
			// Now create the subclass axiom
			OWLAxiom axiom = df.getOWLSubClassOfAxiom(annotDomainClass, domainClass);
			
			// add the subclass axiom to the ontology.
			AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
			
			// We now use the manager to apply the change
			man.applyChange(addAxiom);
		}
		
		return true;
	}
	
	private void addLabel(OWLEntity owlEnt, String label)
	{
		OWLAnnotation labelAnnot = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(label, "en"));

		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(owlEnt.getIRI(), labelAnnot);
		man.applyChange(new AddAxiom(owlOnt, ax)); 
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
    		OWLAnnotationProperty owlParentAnnotProp = df.getOWLAnnotationProperty(parentPropIRI);
			OWLAxiom subAnnotPropAx = df.getOWLSubAnnotationPropertyOfAxiom(property, owlParentAnnotProp);
			man.applyChange(new AddAxiom(owlOnt, subAnnotPropAx));
		}
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyDomain()
	 */
	@Override
	protected void setPropertyDomain()
	{
		if(annotDomainClass==null)
			return; // not setting domain
		
		// create a class for this domain if necessary
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
			// first create and add equivalent class axiom
			OWLObjectUnionOf union = df.getOWLObjectUnionOf(domainClses);
			OWLAxiom equivAx = df.getOWLEquivalentClassesAxiom(annotDomainClass, union);
			man.applyChange(new AddAxiom(owlOnt, equivAx));
			
			// now create and add domain axiom
			OWLAxiom domainAx = df.getOWLAnnotationPropertyDomainAxiom(property, annotDomainClass.getIRI());
			man.applyChange(new AddAxiom(owlOnt, domainAx));
		}
		else if(domainClses.size()==1)
		{
			// single domain class
			OWLClassExpression domainClsExp = domainClses.iterator().next();
			IRI domainClsIRI = domainClsExp.asOWLClass().getIRI();
			
			// first create and add equivalent class axiom

			// you are here!!!!
			OWLAxiom equivAx = df.getOWLEquivalentClassesAxiom(annotDomainClass, domainClsExp);
			man.applyChange(new AddAxiom(owlOnt, equivAx));
						
			
			OWLAxiom ax = df.getOWLAnnotationPropertyDomainAxiom(property, annotDomainClass.getIRI());
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		
		// I don't think that there is anything to be done about min and max cardinality 
		// (will not be supported).
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyRange()
	 */
	@Override
	protected void setPropertyRange()
	{
		if(rangeClass==null)
			return; // not setting range
		
		ValueType slotValType = framesSlot.getValueType();
		if(slotValType==ValueType.CLS)
		{
			setObjectPropertyRange();
		}
		else if(slotValType==ValueType.INSTANCE)
		{
			System.err.println("instance value type not supported in AnnotationPropertyConverter.setPropertyRange()");
		}
		else
		{
			setDataPropertyRange();
		}
		
		
	}
	
	private void setDataPropertyRange()
	{
		String frag = propIRI.getFragment();
		
		ValueType slotValType = framesSlot.getValueType();
		if(slotValType.equals(ValueType.BOOLEAN))
		{
			OWLAxiom ax = df.getOWLAnnotationPropertyRangeAxiom(property, df.getBooleanOWLDatatype().getIRI());
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
		else if(slotValType.equals(ValueType.INTEGER))
		{
			
			OWLAxiom ax = df.getOWLAnnotationPropertyRangeAxiom(property, df.getIntegerOWLDatatype().getIRI());
			man.applyChange(new AddAxiom(owlOnt, ax));
			
			/* this works, but would need to be done for max as well, and done for floats/doubles
			// handle min value if defined
			Number minVal = framesSlot.getMinimumValue();
			if(minVal == null)
				return;
			
			OWLDatatypeRestriction integerMin = df.getOWLDatatypeRestriction(
					df.getIntegerOWLDatatype(), OWLFacet.MIN_INCLUSIVE, df.getOWLLiteral(minVal.intValue()));
			String propRangeMinValName = "Range_min_val_of_"+frag;
			IRI minValRestrIRI = null;
			try
			{
				minValRestrIRI = iriUtils.getIRIForString(propRangeMinValName);
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				return;
			}
			OWLDatatype minDataType = df.getOWLDatatype(minValRestrIRI);
			OWLDatatypeDefinitionAxiom 	defnAx = df.getOWLDatatypeDefinitionAxiom(minDataType, integerMin); 
			man.applyChange(new AddAxiom(owlOnt, defnAx));
			OWLAxiom minAx = df.getOWLAnnotationPropertyRangeAxiom(property, minDataType.getIRI());
			man.applyChange(new AddAxiom(owlOnt, minAx));
			*/
		}
		else if(slotValType.equals(ValueType.FLOAT))
		{
			OWLAxiom ax = df.getOWLAnnotationPropertyRangeAxiom(property, df.getFloatOWLDatatype().getIRI());
			man.applyChange(new AddAxiom(owlOnt, ax));
			
			// TODO: not yet implemented
			/*
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
			*/
		}
		else if(slotValType.equals(ValueType.STRING))
		{
			OWLDatatype stringDatatype = df.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI());
			OWLAxiom ax = df.getOWLAnnotationPropertyRangeAxiom(property, stringDatatype.getIRI());
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
			
			if(lits.isEmpty())
				return;
			
			OWLDataOneOf oneOf = df.getOWLDataOneOf(lits);
			
			String propRangeOneOfValName = "Range_val_one_of_"+frag;
			IRI oneOfValRestrIRI = null;
			try
			{
				oneOfValRestrIRI = iriUtils.getIRIForString(propRangeOneOfValName);
			}
			catch (IRIGenerationException e)
			{
				e.printStackTrace();
				return;
			}
			OWLDatatype oneOfDataType = df.getOWLDatatype(oneOfValRestrIRI);
			OWLDatatypeDefinitionAxiom 	defnAx = df.getOWLDatatypeDefinitionAxiom(oneOfDataType, oneOf); 
			man.applyChange(new AddAxiom(owlOnt, defnAx));
			OWLAxiom oneOfRangeAx = df.getOWLAnnotationPropertyRangeAxiom(property, oneOfDataType.getIRI());
			man.applyChange(new AddAxiom(owlOnt, oneOfRangeAx));
			
			/* TODO, not implemented correctly, may not be possible
			String propRangeClassName = "Range_vals_of_"+frag;
			IRI annotRangeValsIRI = null;
			try
			{
				annotRangeValsIRI = iriUtils.getClassIRIForString(propRangeClassName);
			}
			catch (IRIGenerationException e1)
			{
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if(annotRangeValsIRI!=null && rangeClass!=null)
			{
				OWLClass annotRangeValsClass = df.getOWLClass(annotRangeValsIRI);
				addLabel(annotRangeValsClass,propRangeClassName);
				
				// Now create the subclass axiom
				OWLAxiom axiom = df.getOWLSubClassOfAxiom(annotRangeValsClass, rangeClass);
				
				// add the subclass axiom to the ontology.
				AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
				
				// We now use the manager to apply the change
				man.applyChange(addAxiom);
				
				OWLDataOneOf oneOf = df.getOWLDataOneOf(lits);
				
				OWLAxiom equivAx = df.getOWLEquivalentClassesAxiom(annotRangeValsClass, oneOf);
				man.applyChange(new AddAxiom(owlOnt, equivAx));
				
				OWLAxiom ax = df.getOWLAnnotationPropertyRangeAxiom(property, annotRangeValsClass.getIRI());
				man.applyChange(new AddAxiom(owlOnt, ax));
			}
			*/
			
			/*
			OWLDataOneOf oneOf = df.getOWLDataOneOf(lits);
			OWLAxiom ax = df.getOWLAnnotationPropertyRangeAxiom(property, oneOf);
			man.applyChange(new AddAxiom(owlOnt, ax));
			*/
		}
	}
	
	private void setObjectPropertyRange()
	{
		// add range test
		//Collection<Cls> range = framesSlot.getAllowedParents();
		Queue<Cls> framesRangeQueue = new LinkedList<Cls>(framesSlot.getAllowedParents());
		if(framesRangeQueue.isEmpty())
			return;
		
		String frag = propIRI.getFragment();
		String propRangeClassName = "Range_of_"+frag;
		IRI annotRangeIRI = null;
		try
		{
			annotRangeIRI = iriUtils.getClassIRIForString(propRangeClassName);
		}
		catch (IRIGenerationException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if(annotRangeIRI!=null && rangeClass!=null)
		{
			annotRangeClass = df.getOWLClass(annotRangeIRI);
			addLabel(annotRangeClass,propRangeClassName);
			
			// Now create the subclass axiom
			OWLAxiom axiom = df.getOWLSubClassOfAxiom(annotRangeClass, rangeClass);
			
			// add the subclass axiom to the ontology.
			AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
			
			// We now use the manager to apply the change
			man.applyChange(addAxiom);
		}
		
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
			// first create and add equivalent class axiom
			OWLObjectUnionOf union = df.getOWLObjectUnionOf(rangeClses);
			OWLAxiom equivAx = df.getOWLEquivalentClassesAxiom(annotRangeClass, union);
			man.applyChange(new AddAxiom(owlOnt, equivAx));
			
			// now create and add range axiom
			OWLAxiom rangeAx = df.getOWLAnnotationPropertyRangeAxiom(property, annotRangeClass.getIRI());
			man.applyChange(new AddAxiom(owlOnt, rangeAx));
		}
		else if(rangeClses.size()==1)
		{
			// single domain class
			OWLClassExpression rangeClsExp = rangeClses.iterator().next();
			IRI rangeClsIRI = rangeClsExp.asOWLClass().getIRI();
			
			// first create and add equivalent class axiom

			OWLAxiom equivAx = df.getOWLEquivalentClassesAxiom(annotRangeClass, rangeClsExp);
			man.applyChange(new AddAxiom(owlOnt, equivAx));
						
			
			OWLAxiom ax = df.getOWLAnnotationPropertyRangeAxiom(property, rangeClsIRI);
			man.applyChange(new AddAxiom(owlOnt, ax));
		}
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyInverse()
	 */
	@Override
	protected void setPropertyInverse()
	{
		// TODO do I need to implement this?
		// will we have any annotation props with object values? - Yes
		// can annotation properties have inverses? - I think not
		
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#convertSlotValues(edu.stanford.smi.protege.model.Cls, org.semanticweb.owlapi.model.OWLClass)
	 */
	@Override
	public void convertSlotValues(Cls framesCls, OWLClass owlCls)
	{
		Collection framesVals = framesCls.getDirectOwnSlotValues(framesSlot);
		ValueType framesValType = framesSlot.getValueType();
		IRI owlPropIRI = property.getIRI();
		for(Object framesVal : framesVals)
		{
			OWLAnnotationValue annotVal =  convUtils.getAnnotValForFramesVal(framesValType, framesVal);
			OWLAnnotation annot = df.getOWLAnnotation(property, annotVal);
			OWLAxiom axiom = df.getOWLAnnotationAssertionAxiom(owlCls.getIRI(),annot);
			
			// add the axioms to the ontology.
			AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
			
			// We now use the manager to apply the change
			man.applyChange(addAxiom);
		}	
	}

	/* (non-Javadoc)
	 * @see edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter#setPropertyCharacteristics()
	 */
	@Override
	protected void setPropertyCharacteristics()
	{
		super.setPropertyCharacteristics();
		
		// a straight reified conversion (without property chains) cannot be symmetric
		
		// now handle functional properties
		int maxCard = framesSlot.getMaximumCardinality();
		if(maxCard==1)
		{
			// set owl property to be functional
			if(property.isOWLObjectProperty())
			{
				OWLFunctionalObjectPropertyAxiom funcAx = 
						df.getOWLFunctionalObjectPropertyAxiom(property.asOWLObjectProperty());
				man.applyChange(new AddAxiom(owlOnt, funcAx));
			}
			else if(property.isOWLDataProperty())
			{
				OWLFunctionalDataPropertyAxiom funcAx = 
						df.getOWLFunctionalDataPropertyAxiom(property.asOWLDataProperty());
				man.applyChange(new AddAxiom(owlOnt, funcAx));
			}
		}		
	}

	/**
	 * @param domain the domain to set
	 */
	public void setDomainClass(OWLClass domain)
	{
		this.domainClass = domain;
	}

	/**
	 * @param range the range to set
	 */
	public void setRangeClass(OWLClass range)
	{
		this.rangeClass = range;
	}
}
