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

import java.util.Map;

import org.semanticweb.owlapi.model.OWLClass;

import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;

/**
 * @author detwiler
 * @date Oct 17, 2013
 *
 */
public interface SlotValueConverter
{
	public boolean init(Map<String,String> initArgs);
	public void convertSlot();
	public void convertSlotValues(Cls framesCls, OWLClass owlCls);
}
