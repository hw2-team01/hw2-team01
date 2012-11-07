package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;
/*
 *  Wittren by Guanyu Wang, andrew ID: guanyuw
 *  For homework 1 of 11791
 */
import java.util.Iterator;
import java.util.Map;
import org.apache.uima.resource.ResourceInitializationException;
import java.util.ArrayList;
import java.util.List;

import org.apache.uima.UimaContext;
import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

/**
 * The Name Entity Recognizer. It use the PosTagNamedEntityRecognizer first to tokinize all texts,
 * then adopts the GeneRuler to judge all these tokens are gene mentions or not.
 */

public class GuanyuwKeyTermExtractor extends AbstractKeytermExtractor {
  private PosTagNamedEntityRecognizer mPosTagNER;

  private GeneRuler mRuler;

  Map<Integer, Integer> begin2end;
  
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
  }

  public List<Keyterm> getKeyterms(String docText) {
    List<Keyterm> keyterms = new ArrayList<Keyterm>();
    String[] lines;
    String word;
    String lineindex;
    String linewoindex;
    int firstblank;
    mRuler = new GeneRuler();

    try {
      mPosTagNER = new PosTagNamedEntityRecognizer();
    } catch (ResourceInitializationException e) {
      e.printStackTrace();
    }

    lines = docText.split("\n");
    for (String s : lines) {
      firstblank = s.indexOf(" ");
      lineindex = s.substring(0, firstblank);
      linewoindex = s.substring(firstblank + 1, s.length());

      begin2end = mPosTagNER.getGeneSpans(linewoindex);
      Iterator it = begin2end.entrySet().iterator();

      while (it.hasNext()) {
        Map.Entry pairs = (Map.Entry) it.next();
        // System.out.println(pairs.getKey() + " <-> " + pairs.getValue());
        word = linewoindex.substring((Integer) pairs.getKey(), (Integer) pairs.getValue());
        
        if (mRuler.GeneTest(word)) {
          keyterms.add(new Keyterm(word));
        }
      }
    }
    return keyterms;
  }
  
}
