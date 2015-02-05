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
package edu.uw.sig.frames2owl;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.semanticweb.owlapi.MakeClassesMutuallyDisjoint;
import org.semanticweb.owlapi.OWLCompositeOntologyChange;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.io.OWLXMLOntologyFormat;
import org.semanticweb.owlapi.io.RDFXMLOntologyFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnonymousIndividual;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLEntity;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyChange;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLProperty;
import org.semanticweb.owlapi.util.SimpleIRIMapper;

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
import edu.uw.sig.frames2owl.instconv.InstanceConverter;
import edu.uw.sig.frames2owl.slotconv.BaseReifiedPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.SlotValueConverter;
import edu.uw.sig.frames2owl.slotconv.BaseSlotValueConverter.PropType;
import edu.uw.sig.frames2owl.slotconv.impl.AnnotationPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.impl.DataPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.impl.ObjectPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.impl.ReifiedPropertyConverter;
import edu.uw.sig.frames2owl.slotconv.impl.SplitReifiedPropertyConverter;
import edu.uw.sig.frames2owl.util.ConfigReader;
import edu.uw.sig.frames2owl.util.ConvUtils;
import edu.uw.sig.frames2owl.util.IRIConf;
import edu.uw.sig.frames2owl.util.IRIUtils;

/**
 * @author detwiler
 * @date Dec 7, 2012
 *
 */
public class Converter
{
	//private String framesPPRJ;
	//private String owlFile;
	//String owlOutputDir;
	//String owlOutputName;
	//String owlIRIString;
	private ConfigReader cReader;
	
	private String owlPath;
	//IRI owlIRI;
	private IRIUtils iriUtils;
	private ConvUtils convUtils;
	
	//String iriFragSep;
	//String iriValueComp;
	//Slot iriValSlot;
	
	private KnowledgeBase framesKB;
	private OWLOntology owlOnt;
	
	private OWLOntologyManager man;
	private OWLDataFactory df;
	
	private Boolean makeSiblingClsesDisj = false;
	private Boolean createAnnotDomainRangeClses = false;
	private Map<Slot,SlotValueConverter> slot2ConvMap = new HashMap<Slot,SlotValueConverter>();
	//private Map<Cls,InstanceConverter> cls2InstConvMap = new HashMap<Cls,SlotValueConverter>();
	//private SlotValueConverter defaultConverter;
	
	// additional reified sub-relationship exclusions
	private Set<Slot> reifExclusions = new HashSet<Slot>();
	private Set<Slot> slotAnnotExclusions = new HashSet<Slot>();
	
	private OWLClass domainClass = null;
	private OWLClass rangeClass = null;
	
	public Converter(String framesPath, String owlPath, String owlUriString, String configPath)
	{
		//this.framesPPRJ = framesPath;
		//this.owlFile = owlPath;	
		this.owlPath = owlPath;
		
		//String owlIRIString = owlUriString;
		//this.owlIRI = IRI.create(owlUriString);
		
		boolean initSuccess = init(framesPath, owlUriString, configPath, null);
		if(!initSuccess)
		{
			System.err.println("Error: converter init failed");
			System.exit(-1);
		}
	}
	
	public Converter(String framesPath, String owlPath, String owlUriString, 
			String configPath, Map<FrameID,String> frame2ProjectMap)
	{
		this.owlPath = owlPath;
		
		boolean initSuccess = init(framesPath, owlUriString, configPath, frame2ProjectMap);
		if(!initSuccess)
		{
			System.err.println("Error: converter init failed");
			System.exit(-1);
		}
	}
	
	private boolean init(String framesPath, String owlUriString, String configPath, 
			Map<FrameID,String> frame2ProjectMap) 
	{
		try
		{
			cReader = new ConfigReader(configPath);
		}
		catch (ConfigurationException e1)
		{
			e1.printStackTrace();
			return false;
		}
		
		// load frames ontology
		framesKB = KnowledgeBaseLoader.loadKB(framesPath);
		if(framesKB==null)
		{
			System.err.println("Error: frames ontology not found");
			return false;
		}
		
		// create new owl ontology
		man = OWLManager.createOWLOntologyManager();
		df = man.getOWLDataFactory();
		try
		{
			owlOnt = man.createOntology(IRI.create(owlUriString));
		}
		catch (OWLOntologyCreationException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
		
		this.iriUtils = new IRIUtils(framesKB, owlOnt, cReader,frame2ProjectMap);
		this.convUtils = new ConvUtils(iriUtils,df,framesKB);
		
		// handle import statements (for includes in frames)
		//String owlIRIString = owlOnt.getOntologyID().getOntologyIRI().toString();
		
		/*
		for(URI includedFileURI : (Collection<URI>)framesKB.getProject().getDirectIncludedProjectURIs())
		{
			// this block parses the name of the included ontology out of the include file path
			// (that is the only place it is really represented)
			String path = includedFileURI.getPath();
			
			int lastSlashInd = path.lastIndexOf('/');
			//int lastPeriodInd = path.lastIndexOf('.');
			//String fileName = path.substring(lastSlashInd+1, lastPeriodInd);
			String fileName = path.substring(lastSlashInd+1);
			
			// create import statement
			// construct IRI
			IRI importIRI = IRI.create(fileName);
			System.err.println("will gen import statement for "+importIRI);
			
			// insert import statement
			OWLImportsDeclaration importDec = df.getOWLImportsDeclaration(importIRI);
			man.applyChange(new AddImport(owlOnt,importDec));
		}
		*/
		
		String owlExt = ".owl";
		for(URI includedFileURI : (Collection<URI>)framesKB.getProject().getDirectIncludedProjectURIs())
		{
			// this block parses the name of the included ontology out of the include file path
			// (that is the only place it is really represented)
			String path = includedFileURI.getPath();
			
			int lastSlashInd = path.lastIndexOf('/');
			int lastPeriodInd = path.lastIndexOf('.');
			String fileName = path.substring(lastSlashInd+1, lastPeriodInd);
			
			// create import statement
			// construct IRI
			String iriString = /*iriUtils.getProjectIRIBaseString(fileName);iriUtils.getIRIBaseString()//*/fileName+owlExt;
			IRI importIRI = IRI.create(iriString);
			System.err.println("will gen import statement for "+importIRI);
			
			// insert import statement
			OWLImportsDeclaration importDec = df.getOWLImportsDeclaration(importIRI);
			man.applyChange(new AddImport(owlOnt,importDec));
		}
		
		// determine if we should assert that sibling classes are disjoint
		Map<String,Boolean> configFlags = cReader.getConfigFlags();
		makeSiblingClsesDisj = configFlags.get("disjoint-siblings");
		
		// determine if domain and range classes will be created, set-up if necessary
		createAnnotDomainRangeClses = configFlags.get("create-annot-domain-range");
		if(createAnnotDomainRangeClses==null)
			createAnnotDomainRangeClses = false;
		if(createAnnotDomainRangeClses==true)
		{
			String domainClsName = cReader.getDomainClsName();
			if(domainClsName!=null)
			{
				try
				{
					IRI domainClassIRI = iriUtils.getClassIRIForString(domainClsName);
					domainClass = df.getOWLClass(domainClassIRI);
					addLabel(domainClass, domainClsName);
					
					// Now create the subclass axiom
					OWLAxiom axiom = df.getOWLSubClassOfAxiom(domainClass, df.getOWLThing());
					
					// add the subclass axiom to the ontology.
					AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
					
					// We now use the manager to apply the change
					man.applyChange(addAxiom);
				}
				catch (IRIGenerationException e)
				{
					e.printStackTrace();
				}
				
			}
			String rangeClsName = cReader.getRangeClsName();
			if(rangeClsName!=null)
			{
				try
				{
					IRI rangeClassIRI = iriUtils.getClassIRIForString(rangeClsName);
					rangeClass = df.getOWLClass(rangeClassIRI);
					addLabel(rangeClass, rangeClsName);
					
					// Now create the subclass axiom
					OWLAxiom axiom = df.getOWLSubClassOfAxiom(rangeClass, df.getOWLThing());
					
					// add the subclass axiom to the ontology.
					AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
					
					// We now use the manager to apply the change
					man.applyChange(addAxiom);
				}
				catch (IRIGenerationException e)
				{
					e.printStackTrace();
				}
			}
		}
		
		Map<String,Class> slotConvClassMap = cReader.getSlotConvClassMap();
		Map<String,Map<String,String>>	slotInitArgsMap = cReader.getConvInitArgsMap();
		
		// build reified slot exclusion set
		List<String> excReifSubSlots = cReader.getReifExclusions();
		for(String subSlotName : excReifSubSlots)
		{
			Slot currSlot = framesKB.getSlot(subSlotName);
			if(currSlot==null)
			{
				System.err.println("unknown slot marked for exclusion: "+subSlotName);
				continue;
			}
			reifExclusions.add(currSlot);
		}
		
		// build slot annotation exclusion set
		List<String> excSlotAnnotSlots = cReader.getSlotAnnotExclusions();
		for(String slotName : excSlotAnnotSlots)
		{
			Slot currSlot = framesKB.getSlot(slotName);
			if(currSlot==null)
			{
				System.err.println("unknown slot marked for exclusion: "+slotName);
				continue;
			}
			slotAnnotExclusions.add(currSlot);
		}
		//TODO, the above exclusions are not yet passed to any converters !!!
		
		
		for(Slot currSlot : (Collection<Slot>)framesKB.getSlots())
		{
			if(currSlot.isSystem()||currSlot.isIncluded())
				continue;
			
			String slotName = currSlot.getName();			
			try
			{
				//SlotValueConverter currConverter = null;
				Class convClass = slotConvClassMap.get(slotName);
				Map<String,String> initArgs = slotInitArgsMap.get(slotName);
				if(convClass==null)
					convClass = guessSlotConverter(currSlot);
				if(initArgs==null) // use empty map
					initArgs = new HashMap<String,String>();
				SlotValueConverter converter = (SlotValueConverter)convClass.asSubclass(SlotValueConverter.class)
						.getConstructor(KnowledgeBase.class,Slot.class,OWLOntology.class,IRIUtils.class,ConvUtils.class)
						.newInstance(framesKB,currSlot,owlOnt,iriUtils,convUtils);
				
				// add additional reified exclusions if necessary
				if(converter instanceof BaseReifiedPropertyConverter)
				{
					BaseReifiedPropertyConverter reifConv = (BaseReifiedPropertyConverter)converter;
					reifConv.addExcludedSlots(reifExclusions);
				}
				
				// add domain and range classes for annotation property if needed
				if(converter instanceof AnnotationPropertyConverter && 
						createAnnotDomainRangeClses==true)
				{
					AnnotationPropertyConverter annotConv = (AnnotationPropertyConverter)converter;
					annotConv.setDomainClass(domainClass);
					annotConv.setRangeClass(rangeClass);
				}
				
				// init with any args from the config file
				boolean initSuccess = converter.init(initArgs);
				if(!initSuccess)
				{
					System.err.println("Failed to initialize converter for slot "+slotName);
					System.exit(-1);
				}
				slot2ConvMap.put(currSlot, converter);
			}
			catch (IllegalArgumentException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (SecurityException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (InstantiationException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (IllegalAccessException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (InvocationTargetException e)
			{
				e.printStackTrace();
				continue;
			}
			catch (NoSuchMethodException e)
			{
				e.printStackTrace();
				continue;
			}
		}
		
		/*
		
		// create default slot value converter
		Class defaultConverterClass = cReader.getDefaultConv();
		try
		{
			defaultConverter = (SlotValueConverter)defaultConverterClass.asSubclass(SlotValueConverter.class)
					.getConstructor(KnowledgeBase.class,Slot.class,OWLOntology.class,IRIUtils.class)
					.newInstance(framesKB,owlOnt,iriUtils);
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
		}
		*/
				
		/*
		try
		{
			// create config reader
			cReader = new ConfigReader("resource/config.xml");
			
			// read IRI config values
			String iriSlotName = cReader.getIRIValueSourceSlot();
			iriValSlot = framesKB.getSlot(iriSlotName);
			if(iriValSlot==null)
			{
				System.err.println("IRI value slot is null");
				return false;
			}
			
			iriFragSep = cReader.getIRIFragSep();
			iriValueComp = cReader.getIRIValueComp();
			
		}
		catch (ConfigurationException e)
		{
			e.printStackTrace();
			return false;
		}
		*/
		
		return true;
	}
	
	public void run()
	{
		// create class stubs
		createClses();
		
		// process slots first
		createSlots();
		
		// save owl ontology
		saveOnt(owlOnt);
	}
	
	private Class guessSlotConverter(Slot slot)
	{
		// nothing can be automatically inferred to be an annotation property
		ValueType slotValueType = slot.getValueType();
		if(slotValueType.equals(ValueType.CLS))
		{
			return ObjectPropertyConverter.class;
		}
		else if(slotValueType.equals(ValueType.INSTANCE))
		{
			return  ReifiedPropertyConverter.class;
		}
		else
		{
			return DataPropertyConverter.class;
		}
	}
	
	/*
	private IRI iriUtils.getIRIForFrame(Frame frame) throws IRIGenerationException
	{
		// make sure slot is present where IRI info resides
		if(!frame.hasOwnSlot(iriValSlot))
			throw new IRIGenerationException("could not generate IRI for frame "+frame.getBrowserText()+", IRI slot not found");
		
		// get value to construct IRI
		String value = (String)frame.getOwnSlotValue(iriValSlot);
		if(value==null)
		{
			throw new IRIGenerationException("could not generate IRI for frame "+frame.getBrowserText()+", no value in IRI slot");
		}
		
		// construct IRI
		String owlIRIString = owlOnt.getOntologyID().getOntologyIRI().toString();
		String iriString = owlIRIString+iriFragSep+iriValueComp.replace("{value}", value);
		IRI resultIRI = IRI.create(iriString);
		
		// return newly constructed IRI
		return resultIRI;
	}
	*/
	
	private boolean createClses()
	{
		return createClses(null);
	}
	
	private boolean createClses(Cls parentCls)
	{		
		// get the OWL class for parent
		OWLClass owlParent = null;
		try
		{
			if(parentCls!=null)
			{
				if(!parentCls.isSystem())
					owlParent = df.getOWLClass(iriUtils.getIRIForFrame(parentCls));
				else
					owlParent = df.getOWLThing();
			}
		}
		catch (IRIGenerationException e1)
		{
			// TODO: double check if this is how this exception should be handled
			//e1.printStackTrace();
			return false;
		}
		
		Collection<Cls> rootClses = null;
		if(parentCls == null)
		{
			Cls thing = framesKB.getCls(":THING");
			Collection<Cls> allSubs = thing.getDirectSubclasses();
			/*
			Collection<Cls> nonSysSubs = new ArrayList<Cls>();
			for(Cls sub : allSubs)
			{
				if(!sub.isSystem())
					nonSysSubs.add(sub);
			}
			rootClses = nonSysSubs;
			*/
			rootClses = allSubs;
		}
		else
			rootClses = parentCls.getDirectSubclasses();
		
		if(rootClses==null)
		{
			return false;
		}
		
		// create OWL counterpart for parentCls, then move on to children
		
		boolean isOK = true;
		
		Set<OWLClass> siblings = new HashSet<OWLClass>();
		
		for(Cls rootCls : rootClses)
		{
			// check to see if it is an included root
			if(rootCls.isIncluded())
				continue;
			
			OWLClass owlClass = null;
			
			if(!rootCls.isSystem())
			{
				// create OWL class for this Frames Cls
				IRI clsIRI = null;
				try
				{
					clsIRI = iriUtils.getIRIForFrame(rootCls);
				}
				catch (IRIGenerationException e)
				{
					System.err.println(e.getMessage());
					continue;
				}
				if(clsIRI==null)
					continue;
				owlClass = df.getOWLClass(clsIRI);
				
				// add an rdfs label
				addLabelForFrame(owlClass, rootCls);
				
				if(parentCls!=null)
				{							
					// Now create the subclass axiom
					OWLAxiom axiom = df.getOWLSubClassOfAxiom(owlClass, owlParent);
					
					// add the subclass axiom to the ontology.
					AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
					
					// We now use the manager to apply the change
					man.applyChange(addAxiom);
				}
				
				copySlotValues(rootCls,owlClass);
				
				convertInsts(rootCls,owlClass);
				
				// add class to sibling collection (to make disjoint later if needed)
				siblings.add(owlClass);
			}
			
			// create stub classes for child clses
			//Collection<Cls> subs = rootCls.getDirectSubclasses();
			isOK = isOK && createClses(rootCls);
		}
		
		// if configured, make all sibling classes mutually disjoint
		if(makeSiblingClsesDisj && siblings.size()>1)
			makeDisjoint(siblings);
		
		return isOK;
	}
	
	
	//TODO: Root Classes are being ignored during this process
	/*
	private boolean createClses(Cls parentCls)
	{		
		// get the OWL class for parent
		OWLClass owlParent = null;
		try
		{
			if(parentCls!=null)
			{
				if(!parentCls.isSystem())
					owlParent = df.getOWLClass(iriUtils.getIRIForFrame(parentCls));
				else
					owlParent = df.getOWLThing();
			}
		}
		catch (IRIGenerationException e1)
		{
			// TODO: double check if this is how this exception should be handled
			//e1.printStackTrace();
			return false;
		}
		
		Collection<Cls> rootClses = null;
		if(parentCls == null)
		{
			Cls thing = framesKB.getCls(":THING");
			Collection<Cls> allSubs = thing.getDirectSubclasses();
			Collection<Cls> nonSysSubs = new ArrayList<Cls>();
			for(Cls sub : allSubs)
			{
				if(!sub.isSystem())
					nonSysSubs.add(sub);
			}
			rootClses = nonSysSubs;
		}
		else
			rootClses = parentCls.getDirectSubclasses();
		
		if(rootClses==null)
		{
			return false;
		}
		
		boolean isOK = true;
		
		Set<OWLClass> siblings = new HashSet<OWLClass>();
		
		for(Cls rootCls : rootClses)
		{
			// check to see if it is an included root
			if(rootCls.isIncluded())
				continue;
			
			OWLClass owlClass = null;
			
			if(!rootCls.isSystem())
			{
				// create OWL class for this Frames Cls
				IRI clsIRI = null;
				try
				{
					clsIRI = iriUtils.getIRIForFrame(rootCls);
				}
				catch (IRIGenerationException e)
				{
					System.err.println(e.getMessage());
					continue;
				}
				if(clsIRI==null)
					continue;
				owlClass = df.getOWLClass(clsIRI);
				
				// add an rdfs label
				addLabelForFrame(owlClass, rootCls);
				
				if(parentCls!=null)
				{							
					// Now create the subclass axiom
					OWLAxiom axiom = df.getOWLSubClassOfAxiom(owlClass, owlParent);
					
					// add the subclass axiom to the ontology.
					AddAxiom addAxiom = new AddAxiom(owlOnt, axiom);
					
					// We now use the manager to apply the change
					man.applyChange(addAxiom);
					
					copySlotValues(rootCls,owlClass);
					
					convertInsts(rootCls,owlClass);
					
					// add class to sibling collection (to make disjoint later if needed)
					siblings.add(owlClass);
				}
			}
			
			// create stub classes for child clses
			//Collection<Cls> subs = rootCls.getDirectSubclasses();
			isOK = isOK && createClses(rootCls);
		}
		
		// if configured, make all sibling classes mutually disjoint
		if(makeSiblingClsesDisj && siblings.size()>1)
			makeDisjoint(siblings);
		
		return isOK;
	}*/
	
	private boolean convertInsts(Cls rootCls, OWLClass owlClass)
	{
		// create individual converter if appropriate
		String clsName = rootCls.getName();
		Class instConvClass = cReader.getInstConvMap().get(clsName);
		Map<String, String> instConvInitArgs = cReader.getInstConvInitArgsMap().get(clsName);
		
		if(instConvClass==null)
			return true; // this is not wrong, but there are no instances to convert
		if(instConvInitArgs==null) // use empty map
			instConvInitArgs = new HashMap<String,String>();
		
		try
		{
			InstanceConverter converter = (InstanceConverter)instConvClass.asSubclass(InstanceConverter.class)
					.getConstructor(KnowledgeBase.class,Cls.class,OWLOntology.class,IRIUtils.class,ConvUtils.class)
					.newInstance(framesKB,rootCls,owlOnt,iriUtils,convUtils);
			
			// init with any args from the config file
			boolean initSuccess = converter.init(instConvInitArgs);
			if(!initSuccess)
			{
				System.err.println("Failed to initialize converter for instances of "+rootCls.getBrowserText());
				System.exit(-1);
			}
			converter.convertInsts();
		}
		catch (IllegalArgumentException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (SecurityException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (InstantiationException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (IllegalAccessException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (InvocationTargetException e)
		{
			e.printStackTrace();
			return false;
		}
		catch (NoSuchMethodException e)
		{
			e.printStackTrace();
			return false;
		}
		
		return true;
	}
	
	private void makeDisjoint(Set<OWLClass> siblings)
	{
		/*
		if(siblings==null||siblings.isEmpty()||siblings.size()==1)
		{
			System.err.println("Attempt to create invalid sibling disjointness assertions for "+siblings);
		}*/
		OWLCompositeOntologyChange compChange = new MakeClassesMutuallyDisjoint(df,siblings,false,owlOnt);
		List<OWLOntologyChange> changes = compChange.getChanges();
		for(OWLOntologyChange change : changes)
		{
			man.applyChange(change); 
		}
		//System.err.println(compChange.getChanges());
	}
	
	private void addLabelForFrame(OWLEntity owlEnt, Frame frame)
	{
		String label = frame.getName();
		OWLAnnotation labelAnnot = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(label, "en"));

		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(owlEnt.getIRI(), labelAnnot);
		man.applyChange(new AddAxiom(owlOnt, ax)); 
	}
	
	private void addLabel(OWLEntity owlEnt, String label)
	{
		OWLAnnotation labelAnnot = df.getOWLAnnotation(df.getRDFSLabel(), df.getOWLLiteral(label, "en"));

		OWLAxiom ax = df.getOWLAnnotationAssertionAxiom(owlEnt.getIRI(), labelAnnot);
		man.applyChange(new AddAxiom(owlOnt, ax)); 
	}
	
	private boolean createSlots()
	{
		Collection<Slot> slots = framesKB.getSlots();
		for(Slot currSlot : slots)
		{
			// check to see if it is an included slot
			if(currSlot.isIncluded()||currSlot.isSystem())
				continue;
			
			SlotValueConverter conv = this.slot2ConvMap.get(currSlot);
			if(conv!=null)
				conv.convertSlot();
		}
		
		return true;
	}

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
	
	private boolean saveOnt(OWLOntology ont)
	{
		try
		{
			//File outDir = new File(owlOutputDir);
			File output = new File(owlPath);
			IRI documentIRI = IRI.create(output);
			
			 // Now save a copy to another location in OWL/XML format (i.e. disregard
			// the format that the ontology was loaded in).
			//File f = File.createTempFile("owlapiexample", "example1.xml");
			//IRI documentIRI2 = IRI.create(output);
			IRIConf conf = cReader.getIriConfForProj(framesKB.getProject().getName());
			RDFXMLOntologyFormat format = new RDFXMLOntologyFormat();
			format.setDefaultPrefix(conf.getIriDomain()+conf.getIRIFragSep());
			man.saveOntology(ont, format, documentIRI);
			
			// Remove the ontology from the manager
			man.removeOntology(ont);
			
			//output.delete(); 
		}
		catch (OWLOntologyStorageException e)
		{
			e.printStackTrace();
			return false;
		}
		/*
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
		*/
		
		return true;
	}

	/**
	 * @return the convUtils
	 */
	public Collection<URI> getIncludedProjects()
	{
		return convUtils.getIncludedProjects();
	}

	/**
	 * @return the convUtils
	 */
	public Map<FrameID,String> getFrame2ProjectMap()
	{
		return iriUtils.getFrame2ProjectMap();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		Converter conv = new Converter(args[0],args[1],args[2],args[3]);
		conv.run();
		
		/*
		for(Slot slot : (Collection<Slot>)conv.framesKB.getSlots())
		{
			if(!slot.isSystem())
				System.err.println(slot.getName());
		}
		*/

	}

}
