package sig.biostr.washington.edu.protege;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import edu.stanford.smi.protege.model.KnowledgeBase;
import edu.stanford.smi.protege.model.Project;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: UW SIG</p>
 * @author Todd Detwiler
 * @version 1.0
 */

public class KnowledgeBaseLoader
{
  /**
   * This function initializes the Protege knowledge base
   * @return true if successful
   */
  public static KnowledgeBase loadKB(String pprj)
  {
    KnowledgeBase kb = null;
    Collection errors = new ArrayList();
    File pprjFile = new File(pprj);
    Project project = new Project(pprjFile.getAbsolutePath(),errors);
    if(project==null&&!errors.isEmpty())
    {
      System.out.println("create project failed for: "+pprj);
      displayErrors(errors);
      return kb;
    }
    //runTest(project);
    kb = project.getKnowledgeBase();
    System.out.println("knowledge base loaded");
    return kb;
  }
  
  /*
  public static void runTest(Project project)
  {
	  KnowledgeBase kb1 = project.getInternalProjectKnowledgeBase();
	  KnowledgeBase kb2 = project.getKnowledgeBase();
	  System.err.println("kb1 size = "+kb1.getClsCount());
	  System.err.println("kb2 size = "+kb2.getClsCount());
  }
  */

  /**
   * Method for displaying an error collection
   * @param errors collection of errors to display
   */
  public static void displayErrors(Collection errors)
  {
    Iterator i = errors.iterator();
    while(i.hasNext()){
      System.out.println("Error: "+i.next());
    }
  }
}