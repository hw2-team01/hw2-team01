package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.List;

import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class SimpleQueryFormulator {
  public String formulateQuery(String question, List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for(Keyterm term: keyterms)
      query.append(term+" ");
    return query.toString();
  }

}