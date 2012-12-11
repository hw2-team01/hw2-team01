package edu.cmu.lti.f12.hw2.hw2_team01.passage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;
import org.jsoup.Jsoup;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import edu.cmu.lti.f12.hw2.hw2_team01.passage.Passage;
import edu.cmu.lti.f12.hw2.hw2_team01.retrieval.SimpleSolrWrapper;
import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerSum;
import edu.cmu.lti.oaqa.openqa.hello.passage.PassageCandidateFinder;
import edu.cmu.lti.oaqa.openqa.hello.passage.SimplePassageExtractor;

public class YipeiBioPassageExtractor extends SimplePassageExtractor {

  private String WindowKind; //merge, sentence, para

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    this.WindowKind = (String) aContext.getConfigParameterValue("window");
    try {
      this.wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<PassageCandidate> extractPassages(String question, List<Keyterm> keyterms,
          List<RetrievalResult> documents) {
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();
    for (RetrievalResult document : documents) {
      System.out.println("RetrievalResult: " + document.toString());
      String id = document.getDocID();
      try {
        String htmlText = wrapper.getDocText(id);

        String text = htmlText;

        System.out.println(text);
        WindowsExtractor select = new WindowsExtractor(WindowKind);
        List<Passage> passages = select.ExtractWindow(text);

        System.out.println("passage extraction complete");

        MyPassageCandidateFinder finder = new MyPassageCandidateFinder(id, passages,
                new WeightedScorer(), htmlText.length());

        List<String> keytermStrings = Lists.transform(keyterms, new Function<Keyterm, String>() {
          public String apply(Keyterm keyterm) {
            return keyterm.getText();
          }
        });
        List<PassageCandidate> passageSpans = finder.extractPassages(keytermStrings
                .toArray(new String[0]));

        for (PassageCandidate passageSpan : passageSpans)
          result.add(passageSpan);
      } catch (SolrServerException e) {
        e.printStackTrace();
      }
    }
    return result;
  }
}
