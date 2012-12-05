package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

//import edu.cmu.lti.oaqa.core.provider.solr.SolrWrapper;
import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Original version by @author Zi Yang <ziy@cs.cmu.edu>
 * Modified by @author Victor Chahuneau <vchahune@cs.cmu.edu>
 */
public class BoostedSimpleBioSolrRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;
  protected SimpleSolrWrapper wrapper;
  protected String geneboost, diseboost, verbboost;
  
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
    geneboost = String.valueOf((Integer) aContext.getConfigParameterValue("GENEkey"));
    System.out.println("the geneboost: " + geneboost);
    verbboost = String.valueOf((Integer) aContext.getConfigParameterValue("VERB"));
    System.out.println("the verbboost: " + verbboost);
    diseboost = String.valueOf((Integer) aContext.getConfigParameterValue("DISEkey"));
    System.out.println("the diseboost:" + diseboost);
    try {
      wrapper = new SimpleSolrWrapper(serverUrl, serverPort, embedded, core);
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
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize, "dismax", geneboost, verbboost, diseboost);
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
  
  protected String getLabel(Keyterm term){
    String label = term.getComponentId();
    StringTokenizer tokenizer = new StringTokenizer(term.toString());
    StringBuffer query = new StringBuffer();
    while (tokenizer.hasMoreTokens()) {
      query.append(tokenizer.nextToken() + ":" + label + " ");
    }
    query.delete(query.length() - 1, query.length());
    return query.toString();
  }
  
  protected String formulateQuery(String questionText, List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for(Keyterm term: keyterms){
      query.append(getLabel(term)+ " ");
    }
    return query.toString();
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    wrapper.close();
  }
}