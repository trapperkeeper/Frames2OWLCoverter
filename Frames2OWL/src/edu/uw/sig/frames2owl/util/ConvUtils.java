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
package edu.uw.sig.frames2owl.util;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAnnotationValue;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataHasValue;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLProperty;

import sig.biostr.washington.edu.protege.KnowledgeBaseLoader;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;

/**
 * @author detwiler
 * @date Dec 18, 2013
 *
 */
public class ConvUtils
{
	private IRIUtils iriUtils;
	private OWLDataFactory df;
	
	private KnowledgeBase framesKB;
		
	// used for determining which classes exist outside of metaclass hierarchy
	Set<Cls> nonSysRoots = new HashSet<Cls>();
	
	//private Map<FrameID,String> frame2ProjectMap;
	
	public ConvUtils(IRIUtils iriUtils, OWLDataFactory df, KnowledgeBase framesKB)
	{
		this.iriUtils = iriUtils;
		this.df = df;
		
		/* This code is somewhat specific to FMA which merges the metaclass and 
		 * regular class hierarchy.
		 */
		this.framesKB = framesKB;
		Cls thing = framesKB.getRootCls();
		Collection<Cls> roots = thing.getDirectSubclasses();
		for(Cls root : roots)
		{
			if(!root.isSystem())
			{
				nonSysRoots.add(root);
			}
		}
		
		/*
		if(frame2ProjectMap!=null)
			this.frame2ProjectMap = frame2ProjectMap;
		else
			buildFrame2ProjectMap();
			*/
	}
	
	/*
	public ConvUtils(IRIUtils iriUtils, OWLDataFactory df, KnowledgeBase framesKB)
	{
		this.iriUtils = iriUtils;
		this.df = df;
		
		// This code is somewhat specific to FMA which merges the metaclass and 
		// regular class hierarchy.

		this.framesKB = framesKB;
		Cls thing = framesKB.getRootCls();
		Collection<Cls> roots = thing.getDirectSubclasses();
		for(Cls root : roots)
		{
			if(!root.isSystem())
			{
				nonSysRoots.add(root);
			}
		}
		
		// populate frame->project map
		buildFrame2ProjectMap();
	}
	 */
	
	/*
	private ConfigReader configReader;
	Map<Slot,SlotValueConverter> slot2ConvMap;
	
	public ConvUtils(ConfigReader configReader, Map<Slot,SlotValueConverter> slot2ConvMap)
	{
		this.configReader = configReader;
		this.slot2ConvMap = slot2ConvMap;
	}
	
	
	public void copySlotValues(Cls framesCls, OWLClass owlCls)
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
	
	public void copySlotValues(Instance framesInstance, OWLClass owlCls)
	{
		Collection<Slot> sourceSlots = framesInstance.getOwnSlots();
		for(Slot sourceSlot : sourceSlots)
		{
			SlotValueConverter converter = slot2ConvMap.get(sourceSlot);
			if(converter==null)
				continue;
			converter.convertSlotValues(framesCls, owlCls);
		}
	}
	*/
	
	/**
	 * This method is used to determine if a class exists outside of the metaclass hierarchy
	 * @param framesCls class to check
	 * @return true if found outside of metaclass hierarchy (may also be metaclass)
	 */
	public boolean isRegularCls(Cls framesCls)
	{
		if(framesCls.isSystem())
			return false;
		
		Cls thing = framesKB.getRootCls();
		if(framesCls.hasDirectSuperclass(thing))
			return true;
		
		for(Cls root : this.nonSysRoots)
		{
			if(framesCls.hasSuperclass(root))
				return true;
		}
		
		return false;
	}
	
	public Set<Cls> getRegularRootsForTemplateCls(Cls templateCls)
	{
		Set<Cls> roots = new HashSet<Cls>();
		Collection insts = templateCls.getDirectInstances();
		for(Object instObj : insts)
		{
			if(!(instObj instanceof Cls))
				continue;
			Cls instCls = (Cls)instObj;
			if(isRegularCls(instCls))
			{
				boolean covered = false;
				for(Cls root : roots)
				{
					if(instCls.hasSuperclass(root))
					{
						covered = true;
						break; // noop
					}
					else if(root.hasSuperclass(instCls))
					{
						// remove root from roots, add instCls
						roots.remove(root);
						roots.add(instCls);
						covered = true;
						break;
					}
				}
				
				if(!covered)
				{
					// instCls unrelated to any in roots, add it
					roots.add(instCls);
				}

			}
		}
		
		return roots;
	}
	

	public OWLClassExpression genClassExprForVal(ValueType framesValType, Object framesVal, IRI owlPropIRI)
	{
		OWLClassExpression resultExpr = null;
		OWLProperty property;
		
		if(framesValType.equals(ValueType.CLS))
		{
			property = df.getOWLObjectProperty(owlPropIRI);
			
			Cls currCls = (Cls)framesVal;
			// create existential restriction
			IRI clsIRI = null;
			try
			{
				clsIRI = iriUtils.getIRIForFrame(currCls);
			}
			catch (IRIGenerationException e)
			{
				System.err.println(e);
			}
			if(clsIRI==null)
				return resultExpr;
			OWLClass owlTargetCls = df.getOWLClass(clsIRI);
			OWLObjectSomeValuesFrom existRestr = 
					df.getOWLObjectSomeValuesFrom((OWLObjectProperty)property, owlTargetCls);
			resultExpr = existRestr;
		}
		else if(framesValType.equals(ValueType.INSTANCE))
		{
			//noop for now
			System.err.println("attempting to create class expression for instance value type for owl property "
					+owlPropIRI.toString());
		}
		else if(framesValType.equals(ValueType.BOOLEAN))
		{
			property = df.getOWLDataProperty(owlPropIRI);
			
			Boolean currBool = (Boolean)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currBool);
			OWLDataHasValue hasValue = df.getOWLDataHasValue((OWLDataProperty)property,currLit);
			resultExpr = hasValue;
		}
		else if(framesValType.equals(ValueType.STRING)||framesValType.equals(ValueType.SYMBOL))
		{
			property = df.getOWLDataProperty(owlPropIRI);
			
			String currString = (String)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currString);
			OWLDataHasValue hasValue = df.getOWLDataHasValue((OWLDataProperty)property,currLit);
			resultExpr = hasValue;
		}
		else if(framesValType.equals(ValueType.INTEGER))
		{
			property = df.getOWLDataProperty(owlPropIRI);
			
			Integer currInt = (Integer)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currInt);
			OWLDataHasValue hasValue = df.getOWLDataHasValue((OWLDataProperty)property,currLit);
			resultExpr = hasValue;
		}
		else if(framesValType.equals(ValueType.FLOAT))
		{
			property = df.getOWLDataProperty(owlPropIRI);
			
			Float currFloat = (Float)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currFloat);
			OWLDataHasValue hasValue = df.getOWLDataHasValue((OWLDataProperty)property,currLit);
			resultExpr = hasValue;
		}
		
		
		return resultExpr;
	}
	
	public OWLAnnotationValue getAnnotValForFramesVal(ValueType framesValType, Object framesVal)
	{
		OWLAnnotationValue annotVal = null;
		
		if(framesValType.equals(ValueType.CLS))
		{
			Cls currCls = (Cls)framesVal;
			IRI clsIRI = null;
			try
			{
				clsIRI = iriUtils.getIRIForFrame(currCls);
			}
			catch (IRIGenerationException e)
			{
				System.err.println(e);
			}
			annotVal = clsIRI;
		}
		else if(framesValType.equals(ValueType.INSTANCE))
		{
			//noop for now
		}
		else if(framesValType.equals(ValueType.BOOLEAN))
		{
			Boolean currBool = (Boolean)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currBool);
			annotVal = currLit;
		}
		else if(framesValType.equals(ValueType.STRING)||framesValType.equals(ValueType.SYMBOL))
		{
			String currString = (String)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currString);
			annotVal = currLit;
		}
		else if(framesValType.equals(ValueType.INTEGER))
		{
			Integer currInt = (Integer)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currInt);
			annotVal = currLit;
		}
		else if(framesValType.equals(ValueType.FLOAT))
		{
			Float currFloat = (Float)framesVal;
			OWLLiteral currLit = df.getOWLLiteral(currFloat);
			annotVal = currLit;
		}
		
		return annotVal;
	}
	
	public OWLProperty getOWLPropertyForSlot(Slot framesSlot)
	{
		OWLProperty prop = null;
		
		// get iri for slot
		IRI owlPropIRI;
		try
		{
			owlPropIRI = iriUtils.getIRIForSlot(framesSlot);
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
			return null;
		}
		
		// get value type for slot
		ValueType valType = framesSlot.getValueType();
		
		if(valType.equals(ValueType.CLS)||valType.equals(ValueType.INSTANCE)) // object property
		{
			prop = df.getOWLObjectProperty(owlPropIRI);
		}
		else // data type property
		{
			prop = df.getOWLDataProperty(owlPropIRI);
		}
		
		return prop;
	}
	
	public OWLAnnotationProperty getOWLAnnotationPropertyForSlot(Slot framesSlot)
	{
		OWLAnnotationProperty prop = null;
		
		// get iri for slot
		IRI owlPropIRI;
		try
		{
			owlPropIRI = iriUtils.getIRIForSlot(framesSlot);
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
			return null;
		}
		
		prop = df.getOWLAnnotationProperty(owlPropIRI);
		
		return prop;
	}
	
	public OWLProperty getOWLPropertyForName(String propName, ValueType valType)
	{
		OWLProperty prop = null;
		
		// get iri for property
		IRI owlPropIRI;
		try
		{
			owlPropIRI = iriUtils.getIRIForString(propName);
		}
		catch (IRIGenerationException e)
		{
			e.printStackTrace();
			return null;
		}
		
		if(valType.equals(ValueType.CLS)||valType.equals(ValueType.INSTANCE)) // object property
		{
			prop = df.getOWLObjectProperty(owlPropIRI);
		}
		else // data type property
		{
			prop = df.getOWLDataProperty(owlPropIRI);
		}
		
		return prop;
	}
	
	
	public OWLAxiom getPropertyDomainAxiom(Set<OWLClassExpression> newDomainClassExprs, OWLProperty property)
	{
		OWLAxiom ax = null;
		
		if(newDomainClassExprs.size()>1)
		{
			OWLObjectUnionOf union = df.getOWLObjectUnionOf(newDomainClassExprs);
			if(property.isOWLObjectProperty())
			{
				OWLObjectProperty objProperty = property.asOWLObjectProperty();
				ax = df.getOWLObjectPropertyDomainAxiom(objProperty, union);
				return ax;
			}
			else if(property.isOWLDataProperty())
			{
				OWLDataProperty dataProperty = property.asOWLDataProperty();
				ax = df.getOWLDataPropertyDomainAxiom(dataProperty, union);
				return ax;
			}
			/*
			else if(property.isOWLAnnotationProperty())
			{
				// NOOP for now, not sure this make sense for annotation propert
				OWLAnnotationProperty annotProperty = property.asOWLAnnotationProperty();
				OWLAxiom ax = df.getOWLAnnotationPropertyDomainAxiom(annotProperty, union.);
				return ax;
			}
			*/
		}
		else if(newDomainClassExprs.size()==1)
		{
			// single domain class
			OWLClassExpression domainClsExp = newDomainClassExprs.iterator().next();
			if(property.isOWLObjectProperty())
			{
				OWLObjectProperty objProperty = property.asOWLObjectProperty();
				ax = df.getOWLObjectPropertyDomainAxiom(objProperty, domainClsExp);
				return ax;
			}
			else if(property.isOWLDataProperty())
			{
				OWLDataProperty dataProperty = property.asOWLDataProperty();
				ax = df.getOWLDataPropertyDomainAxiom(dataProperty, domainClsExp);
				return ax;
			}
			/*
			else if(property.isOWLAnnotationProperty())
			{
				// NOOP for now, not sure this make sense for annotation property
				OWLAnnotationProperty annotProperty = property.asOWLAnnotationProperty();
				OWLAxiom ax = df.getOWLAnnotationPropertyDomainAxiom(annotProperty, domainClsExp);
				return ax;
			}
			 */
		}
		
		// TODO: determine scenarios that cause our code to reach here (possibly annotation properties)
		return ax;
	}
	
	/**
	 * Get all of the included projects (for use with batch conversions)
	 * @return URI of each included project
	 */
	public Collection<URI> getIncludedProjects()
	{
		if(framesKB==null)
			return null;
		else
			return framesKB.getProject().getIncludedProjects();
	}
}
