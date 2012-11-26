package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class QueryExpandingRetrievalStrategist extends SimpleBioSolrRetrievalStrategist {
  
  MeSHQueryExpander meshExpander;

  public void initialize(UimaContext aContext) throws ResourceInitializationException {
    super.initialize(aContext);
    String meshDatabasePath = (String) aContext.getConfigParameterValue("MeSH-database");
    log("Loading MeSH query expander with database: "+meshDatabasePath);
    meshExpander = new MeSHQueryExpander(meshDatabasePath);
  }
  
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
}
