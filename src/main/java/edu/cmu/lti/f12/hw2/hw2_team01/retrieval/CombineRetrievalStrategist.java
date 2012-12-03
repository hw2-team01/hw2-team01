package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class CombineRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;

  protected SimpleSolrWrapper wrapper;
  
  protected String CombineQuery1;
  protected String CombineQuery2;
  protected String CombineRetrieval1;
  protected String CombineRetrieval2;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      hitListSize = Integer.parseInt((String) aContext.getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    CombineQuery1 = (String) aContext.getConfigParameterValue("combineQ1");
    CombineQuery2 = (String) aContext.getConfigParameterValue("combineQ2");
    CombineRetrieval1 = (String) aContext.getConfigParameterValue("combineR1");
    CombineRetrieval2 = (String) aContext.getConfigParameterValue("combineR2");
    try {
      wrapper = new SimpleSolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String questionText, List<Keyterm> keyterms) {
    String queryPrimary = QueryFormulater.fomulate(questionText, keyterms, "AND");
    List<RetrievalResult> resultsPrimary = retrieveDocuments(queryPrimary, "base");
    System.out.println("Question: " + questionText + " -> Keyterms: " + keyterms
            + " -> PrimaryQuery: " + queryPrimary + "-> #Results: " + resultsPrimary.size() + "");

    String querySencondary = QueryFormulater.fomulate(questionText, keyterms, "OR");
    List<RetrievalResult> resultsSecondary = retrieveDocuments(querySencondary, "base");
    System.out.println("Question: " + questionText + " -> Keyterms: " + keyterms
            + " -> SecondaryQuery: " + querySencondary + "-> #Results: " + resultsSecondary.size()
            + "");

    return MergeResult(resultsPrimary, resultsSecondary, "base");
  }

  protected List<RetrievalResult> retrieveDocuments(String query, String choice) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs;
      if (choice == "base") {
        docs = wrapper.runQuery(query, hitListSize);
      } else {
        docs = wrapper.runQuery(query, hitListSize, "dismax", "4");
      }
      for (SolrDocument doc : docs) {
        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        System.out.println(doc.getFieldValue("id") + "  " + doc.getFieldValue("score"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }

  protected List<RetrievalResult> MergeResult(List<RetrievalResult> R1, List<RetrievalResult> R2,
          String choice) {
    List<RetrievalResult> R = new ArrayList<RetrievalResult>();
    R.addAll(R1);

    for (int i = 0; i < R2.size(); i++) {
      boolean exist = false;
      String doc = R2.get(i).getDocID();

      for (int j = 0; j < R1.size(); j++) {
        if (R1.get(j).getDocID().toString().trim().equals(doc.trim())) {
          exist = true;
          break;
        }
      }
      if (exist == false) {
        System.out.println("merging from 2:" + doc);
        R.add(R2.get(i));
      }
    }

    System.out.printf("number of merged retreive documents %s", R.size());

    for (int i = 0; i < R.size(); i++) {
      System.out.println((R.get(i).getDocID()));
    }
    return R;
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }
}
