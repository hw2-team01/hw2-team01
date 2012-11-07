package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.io.*;

import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class YipeiwKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    // TODO Auto-generated method stub
    List<Keyterm> keyterms = new ArrayList<Keyterm>();

    try {
      RuleTagNamedEntityRecognizer tagger = new RuleTagNamedEntityRecognizer();
      Map<Integer, Integer> rangeList = tagger.getGeneSpans(question);

      Iterator iter = rangeList.keySet().iterator();

      while (iter.hasNext()) {
        int Begin = (Integer) iter.next();
        int End = rangeList.get(Begin);

        String NE = question.substring(Begin, End);
        
        System.out.printf("from %d to %d: %s\n", Begin, End, NE);

        keyterms.add(new Keyterm(NE));
      }
      return keyterms;
      
    } catch (ResourceInitializationException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return null;
  }

}
