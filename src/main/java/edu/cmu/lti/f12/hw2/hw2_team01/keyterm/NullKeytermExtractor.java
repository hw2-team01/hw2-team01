package edu.cmu.lti.f12.hw2.hw2_team01.keyterm;

import java.util.LinkedList;
import java.util.List;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermExtractor;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class NullKeytermExtractor extends AbstractKeytermExtractor {

  @Override
  protected List<Keyterm> getKeyterms(String question) {
    return new LinkedList<Keyterm>();
  }
  
}
