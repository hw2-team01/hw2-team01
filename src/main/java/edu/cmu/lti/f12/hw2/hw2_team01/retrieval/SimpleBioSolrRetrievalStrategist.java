package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Original version by @author Zi Yang <ziy@cs.cmu.edu>
 * Modified by @author Victor Chahuneau <vchahune@cs.cmu.edu>
 */
public class SimpleBioSolrRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;
  protected SolrWrapper wrapper;

  @Override
  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    try {
      hitListSize = (Integer) aContext.getConfigParameterValue("hit-list-size");
    } catch (ClassCastException e) { // all cross-opts are strings?
      hitListSize = Integer.parseInt((String) aContext
              .getConfigParameterValue("hit-list-size"));
    }
    String serverUrl = (String) aContext.getConfigParameterValue("server");
    Integer serverPort = (Integer) aContext.getConfigParameterValue("port");
    Boolean embedded = (Boolean) aContext.getConfigParameterValue("embedded");
    String core = (String) aContext.getConfigParameterValue("core");
    try {
      wrapper = new SolrWrapper(serverUrl, serverPort, embedded, core);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
  } 
  
  @Override
  protected List<RetrievalResult> retrieveDocuments(String questionText, List<Keyterm> keyterms) {
    String query = formulateQuery(questionText, keyterms);
    List<RetrievalResult> results = retrieveDocuments(query);
    System.out.println("Question: "+questionText+" -> Keyterms: "+keyterms+" -> Query: " + query+"-> #Results: "+results.size()+"");
    return results;
  }

  protected List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize);
      for (SolrDocument doc : docs) {
        RetrievalResult r = new RetrievalResult((String) doc.getFieldValue("id"),
                (Float) doc.getFieldValue("score"), query);
        result.add(r);
        System.out.println(doc.getFieldValue("id"));
      }
    } catch (Exception e) {
      System.err.println("Error retrieving documents from Solr: " + e);
    }
    return result;
  }
  
  protected String formulateQuery(String questionText, List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for(Keyterm term: keyterms)
      query.append(term+" ");
    return query.toString();
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }
}