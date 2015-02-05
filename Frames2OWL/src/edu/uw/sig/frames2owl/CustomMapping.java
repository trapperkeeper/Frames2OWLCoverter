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

/**
 * @author detwiler
 * @date Jan 2, 2013
 *
 */
public class CustomMapping implements Mapping
{
		
	/*
	//TODO: change this
    public  String getValidOWLFrameName(AbstractOWLModel kb, String suggestedName) {
        Assert.assertNotNull(suggestedName);
        String name = suggestedName;
        if (name.startsWith(":")) {
            name = "_" + name.substring(1);
        }
        int first = name.indexOf(':') + 1;
        for (int i = 0; i < name.length(); i++) {
            if (i != first - 1) {
                char c = name.charAt(i);
                if (!Character.isJavaIdentifierPart(c) && VALID_SYMBOLS.indexOf(c) < 0) {
                    name = name.replace(c, '_');
                }
            }
        }
        if (name.length() == first) {
            name = "_" + name;
        }
        else if (!Character.isJavaIdentifierStart(name.charAt(first))) {
            int x = first == 0 ? first : first - 1;
            name = name.substring(0, x) + "_" + name.substring(first);
        }
        if (!name.equals(suggestedName)) {
            if (kb != null && kb.getFrame(name) != null) {
                suggestedName = kb.getUniqueFrameName(name);
            }
            else {
                suggestedName = name;
            }
        }
        return suggestedName;
    }
    */
}
