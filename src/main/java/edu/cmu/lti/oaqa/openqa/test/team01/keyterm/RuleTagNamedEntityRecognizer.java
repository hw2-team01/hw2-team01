package edu.cmu.lti.oaqa.openqa.test.team01.keyterm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.uima.resource.ResourceInitializationException;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.ling.CoreAnnotations.PartOfSpeechAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreAnnotations.TokensAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

public class RuleTagNamedEntityRecognizer {

  private StanfordCoreNLP pipeline;

  public RuleTagNamedEntityRecognizer() throws ResourceInitializationException {
    Properties props = new Properties();
    props.put("annotators", "tokenize, ssplit, pos");
    pipeline = new StanfordCoreNLP(props);
  }

  public Map<Integer, Integer> getGeneSpans(String text) {
    Map<Integer, Integer> begin2end = new HashMap<Integer, Integer>();
    Annotation document = new Annotation(text);
    pipeline.annotate(document);
    List<CoreMap> sentences = document.get(SentencesAnnotation.class);
    for (CoreMap sentence : sentences) {
      List<CoreLabel> candidate = new ArrayList<CoreLabel>();
      for (CoreLabel token : sentence.get(TokensAnnotation.class)) {
        String phrase = token.originalText();

        // System.out.printf("token: %s\n", phrase);

        if (IsGenByRuleSet(phrase)) {
          candidate.add(token);

        } else if (candidate.size() > 0) {
          int begin = candidate.get(0).beginPosition();
          int end = candidate.get(candidate.size() - 1).endPosition();
          begin2end.put(begin, end);
          candidate.clear();
        }
      }
      if (candidate.size() > 0) {
        int begin = candidate.get(0).beginPosition();
        int end = candidate.get(candidate.size() - 1).endPosition();
        begin2end.put(begin, end);
        candidate.clear();
      }
    }
    return begin2end;
  }

  public boolean IsGenByRuleSet(String text) {
    String[] words = text.split("[ ]");

    // length of single word
    int HighThreshold = 10;
    boolean rule1 = IsSpecialLenSingleWord(HighThreshold, words);

    // othology characteristic
    // --capital char besides the first char
    // --whether exist some special symbol, like -,.,(,)
    // --whether exist digits
    boolean rule2 = SearchCapitalCharInWord(words);

    boolean rule3 = SearchDigitWord(words);

    char[] SpecialList = { '\\', ')', '(', '-', '#' };
    boolean rule4 = SearchSpecialCharWord(words, SpecialList);

    return (rule1 || rule2 || rule3 || rule4);
  }

  public boolean IsSpecialLenSingleWord( int HighThreshold, String[] words) {
    if (words.length == 1) {
      if (words[0].length() > HighThreshold) {
        return true;
      }
    }
    return false;
  }

  public boolean SearchCapitalCharInWord(String[] words) {
    for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
      String currentWord = words[wordIndex];
      for (int CharIndex = 1; CharIndex < currentWord.length(); CharIndex++) {
        if (Character.isUpperCase(currentWord.charAt(CharIndex))) {
          return true;
        }
      }
    }
    return false;
  }

  public boolean SearchDigitWord(String[] words) {
    boolean HasChar = false;
    boolean HasDigit = false;
    for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
      String currentWord = words[wordIndex];
      for (int CharIndex = 0; CharIndex < currentWord.length(); CharIndex++) {
        if (Character.isDigit(currentWord.charAt(CharIndex))) {
          HasDigit = true;
          if(HasChar)
          {
            return true;
          }
        }
        if (Character.isLetter(currentWord.charAt(CharIndex)))
        {
          HasChar = true;
        }
      }
    }

    return (HasChar&HasDigit);
  }

  public boolean SearchSpecialCharWord(String[] words, char[] specials) {
    for (int wordIndex = 0; wordIndex < words.length; wordIndex++) {
      String currentWord = words[wordIndex];

      if (currentWord.length() == 1)
        continue;

      for (int CharIndex = 0; CharIndex < currentWord.length(); CharIndex++) {
        for (int specialN = 0; specialN < specials.length; specialN++) {
          if (currentWord.charAt(CharIndex) == specials[specialN]) {
            return true;
          }
        }
      }
    }
    return false;

  }

}
