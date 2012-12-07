package edu.cmu.lti.f12.hw2.hw2_team01.passage;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.client.solrj.SolrServerException;
import org.jsoup.Jsoup;

import utils.HtmlCleaner;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;

public class CorrectedBioPassageExtractor extends SimplePassageExtractor {

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (RetrievalResult document : documents) {
      System.out.println("RetrievalResult: " + document.toString());
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);
       
        // cleaning HTML text
      // String text = Jsoup.parse(htmlText).text().replaceAll("([\177-\377\0-\32]*)", "")/* .trim() */;
        // for now, making sure the text isn't too long
      String text = htmlText.substring(0, Math.min(50000, htmlText.length()));
        //System.out.println(text);
       // HtmlCleaner cleaner = new HtmlCleaner.ByteCleaner();
      //  text=  cleaner.cleanString(text);
      //  System.out.println("clean html: " + text);
        CorrectedPassageCandidateFinder finder = new CorrectedPassageCandidateFinder(id, text,
                new KeytermWindowScorerSum());
        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
      
        List<PassageCandidate> passageSpans = finder.extractPassages(keytermStrings
                .toArray(new String[0]));
      System.out.println("extracted passages... ->" + passageSpans );
        for (PassageCandidate passageSpan : passageSpans){
          System.out.println("Score: " + passageSpan.getProbability() + " DocId:" + passageSpan.getDocID());
         
          result.add(passageSpan);}
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    System.out.println("returning result");
    return result;
  }

}
