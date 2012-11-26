package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.ArrayList;
import java.util.List;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;


public class BoostingRetrievalStrategist extends SimpleBioSolrRetrievalStrategist {
  

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
//    String meshDatabasePath = (String) aContext.getConfigParameterValue("MeSH-database");
//    log("Loading MeSH query expander with database: "+meshDatabasePath);
 }
  
  @Override
  protected List<RetrievalResult> retrieveDocuments(String query) {
    List<RetrievalResult> result = new ArrayList<RetrievalResult>();
    try {
      SolrDocumentList docs = wrapper.runQuery(query, hitListSize, "dismax", "3");
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
  
  @Override
  protected String formulateQuery(String questionText, List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for(Keyterm term: keyterms){
      if(term.getComponentId().equals("GENE")){
        query.append(term+":GENE");
      }
      else if(term.getComponentId().equals("VERB")){
        query.append(term+":VERB");
      }
      else if(term.getComponentId().equals("DISE")){
        query.append(term+":DISE");
      }
      query.append(term+" ");
    }
    return query.toString();
  }
}
