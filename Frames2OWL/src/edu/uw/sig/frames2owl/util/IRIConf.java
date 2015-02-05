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

import edu.stanford.smi.protege.model.Slot;

/**
 * @author detwiler
 * @date Jun 10, 2014
 *
 */
public class IRIConf
{
	private String iriDomain;
	private String iriFragSep;
	private String iriValueComp;
	private String iriValSlotName;
	
	private boolean idsProvided = false;
	
	//private IDProvider idProvider;
	
	public IRIConf(String iriValSlotName, String iriDomain, String iriValueComp, String iriFragSep)
	{
		this.iriDomain = iriDomain;
		this.iriFragSep = iriFragSep;
		this.iriValueComp = iriValueComp;
		this.iriValSlotName = iriValSlotName;
		init();
	}
	
	private void init()
	{
		if(iriValSlotName.startsWith("[auto]"))
		{
			//idProvider = new IDProvider();
			setIdsProvided(true);
			
			int prefixLength = "[auto]".length();
			iriValSlotName = iriValSlotName.substring(prefixLength);
			//iriValSlot = framesKB.getSlot(keySlotName);
		}
	}
	
	public String getIRIValueSourceSlotName()
	{
		return iriValSlotName;
	}
	
	/**
	 * @return the iriDomain
	 */
	public String getIriDomain()
	{
		return iriDomain;
	}

	public String getIRIValueComp()
	{
		return iriValueComp;
	}
	
	public String getIRIFragSep()
	{
		return iriFragSep;
	}

	/**
	 * @return the idsProvided
	 */
	public boolean isIdsProvided()
	{
		return idsProvided;
	}

	/**
	 * @param idsProvided the idsProvided to set
	 */
	public void setIdsProvided(boolean idsProvided)
	{
		this.idsProvided = idsProvided;
	}

	/**
	 * @return the idProvider
	 */
	/*
	public IDProvider getIdProvider()
	{
		return idProvider;
	}*/
}
