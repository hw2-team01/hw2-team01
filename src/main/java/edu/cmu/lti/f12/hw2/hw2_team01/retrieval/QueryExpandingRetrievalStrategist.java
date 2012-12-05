package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;
import edu.cmu.lti.oaqa.framework.data.RetrievalResult;

public class QueryExpandingRetrievalStrategist extends SimpleBioSolrRetrievalStrategist {
  
  MeSHQueryExpander meshExpander;
  int extendflag;

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String meshDatabasePath = (String) aContext.getConfigParameterValue("MeSH-database");
    log("Loading MeSH query expander with database: "+meshDatabasePath);
    meshExpander = new MeSHQueryExpander(meshDatabasePath);
  }
  
  
  /*
  @Override
  protected String formulateQuery(String questionText, List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for(Keyterm term: keyterms) {
      query.append("(\""+term+"\"");
      if(term.getComponentId().equals("DISE")) {
        for(String synonym: meshExpander.getSynomyms(term.getText())) {
          log("Expanded "+term+" -> "+synonym);
          query.append(" OR ("+synonym.replace(" ", " AND ")+")");
        }
      }
      query.append(") ");
    }
    return query.toString();
  }
  */
  
  protected String boostkey(Keyterm term, String Type){
    StringTokenizer tokenizer = new StringTokenizer(term.toString());
    StringBuffer query = new StringBuffer();
    while(tokenizer.hasMoreTokens()){
      query.append(tokenizer.nextToken() + ":" + Type + " ");
    }
    return query.toString();
  }
  
  
  @Override
  protected String formulateQuery(String questionText, List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for(Keyterm term: keyterms) {
      if(term.getComponentId().equals("DISE")) {
        query.append("(" + term + "):DISE_KEY ");
        extendflag = 0;
//        query.append("(");
        for(String synonym: meshExpander.getSynomyms(term.getText())) {
          log("Expanded "+term+" -> "+synonym);
          query.append("(" + synonym + "):DISE_SYN ");
          extendflag = 1;
        }
        query.delete(query.length()-1, query.length());
//        if(extendflag == 1){
//          query.append("):DISE_SYN ");
//        }
//        else {
//          query.append(" ");
//        }
        
      }
      else if(term.getComponentId().equals("GENE"))
      {
        query.append("(" + term + "):GENE ");
      }
      else if(term.getComponentId().equals("VERB"))
      {
        query.append("(" + term + "):VERB ");
      }
    }
    query.delete(query.length() - 1, query.length());
    
    return query.toString();
  }
  
  
  @Override
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
  
}