package edu.cmu.lti.f12.hw2.hw2_team01.keyterm;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.UimaContext;
import org.apache.uima.resource.ResourceInitializationException;

import edu.cmu.lti.oaqa.cse.basephase.keyterm.AbstractKeytermUpdater;
import edu.cmu.lti.oaqa.framework.data.Keyterm;

public class RuleBasedKeytermUpdater extends AbstractKeytermUpdater {

  private Pattern rule;

  @Override
  public void initialize(UimaContext context) throws ResourceInitializationException {
    super.initialize(context);
    rule = Pattern.compile((String) context.getConfigParameterValue("rule"));
  }

  @Override
  protected List<Keyterm> updateKeyterms(String question, List<Keyterm> keyterms) {
    Matcher m = rule.matcher(question);
    while(m.find()) {
      Keyterm keyterm = new Keyterm(m.group());
      keyterm.setComponentId("RULE");
      keyterms.add(keyterm);
    }
    return keyterms;
  }

}
