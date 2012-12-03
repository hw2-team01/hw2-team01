package edu.cmu.lti.f12.hw2.hw2_team01.retrieval;

import java.util.List;

import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class QueryFormulater {
  public static String fomulate(String questionText, List<Keyterm> keyterms, String choice) {
    if (choice == "base")
      return BaseFormulator(keyterms);
    else if (choice == "NoVerb")
      return ExcludeVerbFormulator(keyterms);
    else if (choice == "OR")
      return ORFormulator(keyterms);
    else if (choice == "AND")
      return ANDFormulator(keyterms);
    else if (choice == "original")
      return originFormulator(questionText);
    else {
      System.out.println("wrong choice of query fomulator:" + choice);
      return null;
    }
  }

  private static String BaseFormulator(List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for (Keyterm term : keyterms) {
      query.append(term + " ");
    }
    return query.toString();
  }

  private static String ExcludeVerbFormulator(List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    for (Keyterm term : keyterms) {
      if (!term.getComponentId().equals("VERB")) {
        query.append(term + " ");
      }
    }
    return query.toString();
  }

  private static String ORFormulator(List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    int i;
    for (i = 0; i < keyterms.size() - 1; i++) {
      query.append(keyterms.get(i) + " OR ");
    }
    query.append(keyterms.get(i));
    return query.toString();
  }

  private static String ANDFormulator(List<Keyterm> keyterms) {
    StringBuffer query = new StringBuffer();
    int i;
    for (i = 0; i < keyterms.size() - 1; i++) {
      query.append(keyterms.get(i) + " AND ");
    }
    query.append(keyterms.get(i));
    return query.toString();
  }

  private static String originFormulator(String question) {
    return question;
  }

}
