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

import java.io.File;
import java.util.Collections;
import java.util.concurrent.ConcurrentNavigableMap;

import org.mapdb.DB;
import org.mapdb.DBMaker;

/**
 * @author detwiler
 * @date May 13, 2014
 *
 */
public class IDProvider
{
	private static ConcurrentNavigableMap<String,Integer> map = null;
	private static int initialID = 1;
	private static int currMaxIdVal;
	
	static 
	{
		init();
	}
	
	/*
	public IDProvider()
	{
		init();
	}
	
	public IDProvider(int initialID)
	{
		this.initialID = initialID;
		init();
	}
	*/
	private static void init(int initialID)
	{
		IDProvider.initialID = initialID;
		init();
	}
	
	private static void init()
	{
		// configure and open database using builder pattern.
	    // all options are available with code auto-completion.
	    DB db = DBMaker.newFileDB(new File("resource/id_map/id_db"))
	               .closeOnJvmShutdown()
	               .make();
	
	    // open existing an collection (or create new)
	    map = db.getTreeMap("framesId2OwlId");
	    
	    currMaxIdVal = getMaxId();
	}
	
	private static int getMaxId()
	{
		if(map.isEmpty())
			return initialID;
		
		Integer maxVal = Collections.max(map.values());
		return maxVal;
	}
	
	public static String getId(String input)
	{
		Integer id = map.get(input);
		
		// if there is not already an id for this input, generate a new one
		if(id==null)
			id = generateNewId(input);
		
		return String.format("%07d", id);
	}
	
	private static Integer generateNewId(String input)
	{
		// increment max id value and add new id mapping to map
		Integer newId = new Integer(currMaxIdVal++);
		map.put(input, newId);
		return newId;
	}
	
	private static void clearMap()
	{
		map.clear();
	}
	
	/*
	private void test()
	{
	    // configure and open database using builder pattern.
	    // all options are available with code auto-completion.
	    DB db = DBMaker.newFileDB(new File("resource/id_map/id_db"))
	               .closeOnJvmShutdown()
	               .make();
	
	    // open existing an collection (or create new)
	    ConcurrentNavigableMap<String,Integer> map = db.getTreeMap("framesId2OwlId");
	
	    
	    map.put(1, "one");
	    map.put(2, "two");
	    // map.keySet() is now [1,2]
	
	    db.commit();  //persist changes into disk
	
	    map.put(3, "three");
	    // map.keySet() is now [1,2,3]
	    db.rollback(); //revert recent changes
	    // map.keySet() is now [1,2]
	     
	    
	    System.err.print(map);
	
	    db.close();
	}
	*/


	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		// TODO Auto-generated method stub
		//IDProvider provider = new IDProvider();
		System.err.println("ID for test1 = "+IDProvider.getId("test1"));
		System.err.println("ID for test2 = "+IDProvider.getId("test2"));
		System.err.println("ID for test3 = "+IDProvider.getId("test3"));
		
		System.err.println("ID for test1 = "+IDProvider.getId("test1"));
		System.err.println("ID for test2 = "+IDProvider.getId("test2"));
		System.err.println("ID for test3 = "+IDProvider.getId("test3"));
		
		//System.err.println(String.format("%08d", 13));
		//System.err.println(String.format("%08d", 2));
		//provider.test();

	}

}
