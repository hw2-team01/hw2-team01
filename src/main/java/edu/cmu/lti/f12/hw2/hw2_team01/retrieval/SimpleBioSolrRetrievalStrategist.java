package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.retrieval.AbstractRetrievalStrategist;
import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

/**
 * Original version by @author Zi Yang <ziy@cs.cmu.edu>
 * Modified by @author Victor Chahuneau <vchahune@cs.cmu.edu>
 */
public class SimpleBioSolrRetrievalStrategist extends AbstractRetrievalStrategist {

  protected Integer hitListSize;
  protected SimpleQueryFormulator formulator;
  protected SimpleDocumentRetriever retriever;

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
      retriever = new SimpleDocumentRetriever(serverUrl, serverPort, embedded, core, hitListSize);
    } catch (Exception e) {
      throw new ResourceInitializationException(e);
    }
    formulator = new SimpleQueryFormulator();
  }

  @Override
  protected List<RetrievalResult> retrieveDocuments(String questionText, List<Keyterm> keyterms) {
    String query = formulator.formulateQuery(questionText, keyterms);
    System.out.println(" QUERY: " + query);
    return retriever.retrieveDocuments(query);
  }

  @Override
  public void collectionProcessComplete() throws AnalysisEngineProcessException {
    super.collectionProcessComplete();
    retriever.close();
  }
}