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
import java.util.UUID;

import org.apache.commons.configuration.ConfigurationException;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;

import sig.biostr.washington.edu.protege.KnowledgeBaseLoader;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Frame;
import edu.stanford.smi.protege.model.FrameID;
import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.uw.sig.frames2owl.exception.IRIGenerationException;

/**
 * @author detwiler
 * @date Oct 17, 2013
 *
 */
public class IRIUtils
{
	private KnowledgeBase framesKB;
	private OWLOntology owlOnt;
	
	private ConfigReader cReader;
	//private Map<String,IDProvider> proj2IDProviderMap = new HashMap<String,IDProvider>();
	//private IDProvider idProvider;
	//private IRI owlIRI;
	
	Map<FrameID,String> frame2ProjectMap;
	
	/*
	public IRIUtils(KnowledgeBase framesKB, OWLOntology owlOnt, ConfigReader cReader)
	{
		this.framesKB = framesKB;
		this.owlOnt = owlOnt;
		this.cReader = cReader;
		//this.owlIRI = IRI.create(owlUriString);
		
		if(!init())
		{
			System.err.println("Failed to initialize IRIUtils!");
			System.exit(-1);
		}
	}
	*/
	
	public IRIUtils(KnowledgeBase framesKB, OWLOntology owlOnt, ConfigReader cReader, 
			Map<FrameID,String> frame2ProjectMap)
	{
		this.framesKB = framesKB;
		this.owlOnt = owlOnt;
		this.cReader = cReader;
		this.frame2ProjectMap = frame2ProjectMap;
		
		if(!init())
		{
			System.err.println("Failed to initialize IRIUtils!");
			System.exit(-1);
		}
	}
	
	private boolean init()
	{
		if(frame2ProjectMap==null)
			buildFrame2ProjectMap();
		/*
		// read IRI config values
		String iriSlotName = cReader.getIRIValueSourceSlot();
		
		if(iriSlotName.startsWith("[auto]"))
		{
			//TODO, your are here!
			idProvider = new IDProvider();
			int prefixLength = "[auto]".length();
			String keySlotName = iriSlotName.substring(prefixLength);
			iriValSlot = framesKB.getSlot(keySlotName);
		}
		else
		{
			iriValSlot = framesKB.getSlot(iriSlotName);
			if(iriValSlot==null)
			{
				System.err.println("IRI value slot "+iriSlotName+" is null");
				return false;
			}
		}
		
		iriFragSep = cReader.getIRIFragSep();
		iriValueComp = cReader.getIRIValueComp();
		*/
		
		return true;
	}
	
	public IRI getIRIForFrame(Frame frame) throws IRIGenerationException
	{
		if(frame instanceof Slot)
			return getIRIForSlot((Slot)frame);
		else if(frame instanceof Cls)
			return getIRIForCls((Cls)frame);
		else
			throw new IRIGenerationException("Found invalid frame type in IRIUtils.getIRIForFrame()");
		
	}
	
	public IRI getIRIForCls(Cls cls) throws IRIGenerationException
	{
		IRIConf iriConf = cReader.getIriConfForProj(frame2ProjectMap.get(cls.getFrameID()));
		
		if(iriConf==null)
			throw new IRIGenerationException("no IRIConf for cls "+cls.getBrowserText());
		
		//IDProvider idProvider = iriConf.getIdProvider();
		Slot iriValSlot = framesKB.getSlot(iriConf.getIRIValueSourceSlotName());
		String iriFragSep = iriConf.getIRIFragSep();
		String iriValueComp = iriConf.getIRIValueComp();
		
		// make sure slot is present where IRI info resides
		if(!cls.hasOwnSlot(iriValSlot))
			throw new IRIGenerationException("could not generate IRI for frame "+cls.getBrowserText()+", IRI slot not found");
		
		// get value to construct IRI
		String value = null;
		if(iriConf.isIdsProvided())
		{
			String key = (String)cls.getOwnSlotValue(iriValSlot);
			value = IDProvider.getId(key);
		}
		else
		{
			value = (String)cls.getOwnSlotValue(iriValSlot);
		}
		if(value==null)
		{
			throw new IRIGenerationException("could not generate IRI for frame "+cls.getBrowserText()+", no value in IRI slot");
		}
		
		
		
		/*
		// test 
		//String tempResult = UUID.nameUUIDFromBytes(rootCls.getName().getBytes()).toString();
		//System.out.println(tempResult.hashCode()+" is UUID for "+rootCls.getName());
		//System.err.println("included? = "+cls.isIncluded());
		if(cls.isIncluded())
		{
			System.err.println(cls.getName()+" is from project "+frame2ProjectMap.get(cls.getFrameID()));
		}
		*/
	
		// construct IRI
		//String owlIRIString = owlOnt.getOntologyID().getOntologyIRI().toString();
		String project = frame2ProjectMap.get(cls.getFrameID());
		String owlIRIString = getProjectIRIBaseString(project);
		value = value.replaceAll("[\\s\\(\\)/]", "_");
		String iriString = owlIRIString+iriFragSep+iriValueComp.replace("{value}", value);
				
		IRI resultIRI = IRI.create(iriString);
		
		// return newly constructed IRI
		return resultIRI;
	}
	
	public IRI getIRIForSlot(Slot slot) throws IRIGenerationException
	{
		/*
		 * TODO: the following line is using the iriConf for the umbrella project, instead of the project where slot is found, for 2 reasons:
		 * 1. slot URIs are currently always based on :NAME, so we don't presently need other id provider stuff
		 * 2. slots of the same name occur in multiple projects (collision), so we'd need to code around this
		 */
		IRIConf iriConf = cReader.getIriConfForProj(framesKB.getProject().getName()/*frame2ProjectMap.get(slot.getFrameID())*/);
		//IDProvider idProvider = iriConf.getIdProvider();
		//Slot iriValSlot = framesKB.getSlot(iriConf.getIRIValueSourceSlotName());
		String iriFragSep = iriConf.getIRIFragSep();
		String iriDomain = iriConf.getIriDomain();
		//String iriValueComp = iriConf.getIRIValueComp();
		
		// get value to construct IRI
		String value = slot.getName();
		if(value==null)
		{
			throw new IRIGenerationException("could not generate IRI for slot "+slot.getBrowserText());
		}
		
		// construct IRI
		String owlIRIString = iriDomain;// owlOnt.getOntologyID().getOntologyIRI().toString();
		String iriString = owlIRIString+iriFragSep+value.replaceAll("[\\s\\(\\)/]", "_");
		///iriString = iriString.replaceAll("[\\(\\)/]", "");
		IRI resultIRI = IRI.create(iriString);
		
		// return newly constructed IRI
		return resultIRI;
	}
	
	public IRI getIRIForString(String value) throws IRIGenerationException
	{
		IRIConf iriConf = cReader.getIriConfForProj(framesKB.getProject().getName());
		String iriFragSep = iriConf.getIRIFragSep();
		String iriDomain = iriConf.getIriDomain();
		
		// get value to construct IRI
		if(value==null||value.equals(""))
		{
			throw new IRIGenerationException("could not generate IRI for null or empty string value");
		}
		
		// construct IRI
		String owlIRIString = iriDomain;//owlOnt.getOntologyID().getOntologyIRI().toString();
		String iriString = owlIRIString+iriFragSep+value.replaceAll("[\\s\\(\\)/]", "_");
		///iriString = iriString.replaceAll("[\\(\\)/]", "");
		IRI resultIRI = IRI.create(iriString);
		
		// return newly constructed IRI
		return resultIRI;
	}
	
	public IRI getClassIRIForString(String value) throws IRIGenerationException
	{
		IRIConf iriConf = cReader.getIriConfForProj(framesKB.getProject().getName());
		String iriFragSep = iriConf.getIRIFragSep();
		String iriDomain = iriConf.getIriDomain();
		
		// get value to construct IRI
		if(value==null||value.equals(""))
		{
			throw new IRIGenerationException("could not generate IRI for null or empty string value");
		}
		
		// get value to construct IRI
		String idValue = null;
		if(iriConf.isIdsProvided())
		{
			idValue = IDProvider.getId(value);
		}
		else
		{
			idValue = value;
		}
		
		if(idValue==null)
		{
			throw new IRIGenerationException("could not generate IRI for string "+value);
		}
		
		// construct IRI
		String owlIRIString = iriDomain;//owlOnt.getOntologyID().getOntologyIRI().toString();
		String iriString = owlIRIString+iriFragSep+idValue.replaceAll("[\\s\\(\\)/]", "_");
		///iriString = iriString.replaceAll("[\\(\\)/]", "");
		IRI resultIRI = IRI.create(iriString);
		
		// return newly constructed IRI
		return resultIRI;
	}
	
	/*
	public IRI getIRIForCls(Cls cls) throws IRIGenerationException
	{
		// make sure slot is present where IRI info resides
		if(!cls.hasOwnSlot(iriValSlot))
			throw new IRIGenerationException("could not generate IRI for cls "+cls.getBrowserText()+", IRI slot not found");
		
		// get value to construct IRI
		String value = (String)cls.getOwnSlotValue(iriValSlot);
		if(value==null)
		{
			throw new IRIGenerationException("could not generate IRI for cls "+cls.getBrowserText()+", no value in IRI slot");
		}
		
		// construct IRI
		String owlIRIString = owlIRI.toString();
		String iriString = owlIRIString+iriFragSep+value;
		IRI resultIRI = IRI.create(iriString);
		
		// return newly constructed IRI
		return resultIRI;
	}
	
	
	public IRI getIRIForSlot(Slot slot) throws IRIGenerationException
	{
		// get value to construct IRI
		String value = slot.getName();
		if(value==null)
		{
			throw new IRIGenerationException("could not generate IRI for slot "+slot.getBrowserText());
		}
		
		// construct IRI
		String owlIRIString = owlIRI.toString();//owlOnt.getOntologyID().getOntologyIRI().toString();
		String iriString = owlIRIString+iriFragSep+value.replaceAll("[\\s\\(\\)/]", "_");
		///iriString = iriString.replaceAll("[\\(\\)/]", "");
		IRI resultIRI = IRI.create(iriString);
		
		// return newly constructed IRI
		return resultIRI;
	}
	*/

	/**
	 * @return the owlIRI
	 */
	public IRI getOwlIRI()
	{
		return owlOnt.getOntologyID().getOntologyIRI();
	}
	
	public String getProjectIRIBaseString(String project)
	{
		IRIConf iriConf = cReader.getIriConfForProj(project);
		//String iriFragSep = iriConf.getIRIFragSep();
		String iriDomain = iriConf.getIriDomain();
		return iriDomain;
		/*
		String base = getIRIBaseString();
		String iriString = base+"/"+project;//+".owl"; //TODO, perhaps this should be externalized to a map
		return iriString;
		*/
	}
	
	/*
	public String getIRIBaseString()
	{
		String ontIRIString = getOwlIRI().toString();
		int lastSlashInd = ontIRIString.lastIndexOf('/');
		
		String baseIRIString = ontIRIString.substring(0,lastSlashInd);
		return baseIRIString;
	}
	*/

	/**
	 * @return the frame2ProjectMap
	 */
	public Map<FrameID, String> getFrame2ProjectMap()
	{
		return frame2ProjectMap;
	}
	
	private void buildFrame2ProjectMap()
	{
		System.err.println("building frame to project map");
		
		frame2ProjectMap = new HashMap<FrameID,String>();
		
		// build project set
		Set<Project> projects = new HashSet<Project>();
		Project mainProject = framesKB.getProject();
		projects.add(mainProject);
		for(Object currProjectURIObj : mainProject.getIncludedProjects())
		{
			URI currProjectURI = (URI)currProjectURIObj;
			Collection errors = new ArrayList();
			Project currProject = Project.loadProjectFromURI(currProjectURI, errors);
			
			if(currProject==null&&!errors.isEmpty())
		    {
		      System.out.println("create project failed for: "+currProjectURI);
		      KnowledgeBaseLoader.displayErrors(errors);
		      continue;
		    }
			
			projects.add(currProject);
		}

		for(Project project : projects)
		{
			// get project name
			String projName = project.getName();
			
			// get all non-system, non-included frames. add to frame map
			Collection<Frame> frames = project.getKnowledgeBase().getFrames();
			for(Frame currFrame : frames)
			{
				if(!currFrame.isSystem()&&!currFrame.isIncluded())
					frame2ProjectMap.put(currFrame.getFrameID(),projName);
			}
		}
		
		System.err.println("frame to project map building complete");
	}

}
