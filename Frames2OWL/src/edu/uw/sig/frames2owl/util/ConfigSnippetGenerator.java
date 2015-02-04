/**
 * 
 */
package edu.uw.sig.frames2owl.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

/**
 * @author detwiler
 * @date Mar 25, 2014
 *
 */
public class ConfigSnippetGenerator
{
	private List<String> slotNameList = new ArrayList<String>();
	
	public void readSlotNames(String filePath)
	{
		File slotFile = new File(filePath);
		BufferedReader br = null;
		try{
			br = new BufferedReader(new FileReader(slotFile));
			for(String line; (line = br.readLine()) != null; ) {
		       slotNameList.add(line.trim());
		    }
			
			br.close();
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public void genNoopSnippets()
	{
		/*
		 * <slot_conv_class slot_name="anatomical morphology" 
			conv_cls_name="edu.uw.sig.frames2owl.slotconv.impl.NoopPropertyConverter" />
		 */
		for(String slotName : slotNameList)
		{
			System.err.println("<slot_conv_class slot_name=\""+slotName+"\"");
			System.err.println("	conv_cls_name=\"edu.uw.sig.frames2owl.slotconv.impl.NoopPropertyConverter\" />");
			//System.err.println();
		}
	}
	
	public void genAnnotSnippets()
	{
		/*
		 * <slot_conv_class slot_name="definition" 
			conv_cls_name="edu.uw.sig.frames2owl.slotconv.impl.AnnotationPropertyConverter" />
		 */
		for(String slotName : slotNameList)
		{
			System.err.println("<slot_conv_class slot_name=\""+slotName+"\"");
			System.err.println("	conv_cls_name=\"edu.uw.sig.frames2owl.slotconv.impl.AnnotationPropertyConverter\" />");
			//System.err.println();
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args)
	{
		String filePath = args[0];
		String configType = args[1];
		ConfigSnippetGenerator snipGen = new ConfigSnippetGenerator();
		snipGen.readSlotNames(filePath);
		//System.err.println(snipGen.slotNameList);
		
		if(configType.equals("NOOP"))
			snipGen.genNoopSnippets();
		else if(configType.equals("ANNOT"))
			snipGen.genAnnotSnippets();
		
		
	}

}
