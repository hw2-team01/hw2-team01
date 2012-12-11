package edu.cmu.lti.f12.hw2.hw2_team01.passage;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.uima.analysis_engine.AnalysisEngineProcessException;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.stanford.nlp.io.EncodingPrintWriter.out;

public class MyPassageCandidateFinder {
  // private String text;

  private List<Passage> passages;

  private String docId;

  private int textSize; // values for the entire text

  private int totalMatches;

  private int totalKeyterms;

  private KeytermWindowScorer scorer;

  public MyPassageCandidateFinder(String docId, List<Passage> passages, KeytermWindowScorer scorer,
          int textsize) {
    super();
    this.passages = passages;
    this.docId = docId;
    this.textSize = textsize;
    this.scorer = scorer;
  }

  public List<PassageCandidate> extractPassages(String[] keyterms) {
    List<List<PassageSpan>> matchingSpans = new ArrayList<List<PassageSpan>>();
    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();

    List<Integer> leftEdges = new ArrayList<Integer>();
    List<Integer> rightEdges = new ArrayList<Integer>();

    String text = "";
    int internalStart = 0;
    int internalEnd = 0;
    for (Passage item : passages) {
      internalStart = internalEnd;
      leftEdges.add(internalStart);
      text += item.text;
      internalEnd = text.length();
      rightEdges.add(internalEnd);
    }

    // Find all keyterm matches.
    for (String keyterm : keyterms) {
      Pattern p = Pattern.compile(keyterm);
      Matcher m = p.matcher(text);
      while (m.find()) {
        PassageSpan match = new PassageSpan(m.start(), m.end());
        matchedSpans.add(match);
        totalMatches++;
      }
      if (!matchedSpans.isEmpty()) {
        matchingSpans.add(matchedSpans);
        totalKeyterms++;
      }
    }

    // create set of left edges and right edges which define possible windows.
    // List<Integer> leftEdges = new ArrayList<Integer>();
    // List<Integer> rightEdges = new ArrayList<Integer>();

    // For every possible window, calculate keyterms found, matches found; score window, and create
    // passage candidate.
    List<PassageCandidate> result = new ArrayList<PassageCandidate>();

    for (int i = 0; i < leftEdges.size(); i++) {
      int begin = leftEdges.get(i);
      int end = rightEdges.get(i);
      // This code runs for each window.
      int keytermsFound = 0;
      int matchesFound = 0;
      for (List<PassageSpan> keytermMatches : matchingSpans) {
        boolean thisKeytermFound = false;
        for (PassageSpan keytermMatch : keytermMatches) {
          if (keytermMatch.containedIn(begin, end)) {
            matchesFound++;
            thisKeytermFound = true;
          }
        }
        if (thisKeytermFound)
          keytermsFound++;
      }
      if (keytermsFound > 0) {
        double score = scorer.scoreWindow(begin, end, matchesFound, totalMatches, keytermsFound,
                totalKeyterms, textSize);
        PassageCandidate window = null;
        try {
          int realBegin = passages.get(i).start;
          int realEnd = passages.get(i).end;
          window = new PassageCandidate(docId, realBegin, realEnd, (float) score, null);
        } catch (AnalysisEngineProcessException e) {
          e.printStackTrace();
        }
        result.add(window);
      }
    }

    // Sort the result in order of decreasing score.
    // Collections.sort(result, new PassageCandidateComparator());
    return result;
  }

  private class PassageCandidateComparator implements Comparator {
    // Ranks by score, decreasing.
    public int compare(Object o1, Object o2) {
      PassageCandidate s1 = (PassageCandidate) o1;
      PassageCandidate s2 = (PassageCandidate) o2;
      if (s1.getProbability() < s2.getProbability()) {
        return 1;
      } else if (s1.getProbability() > s2.getProbability()) {
        return -1;
      }
      return 0;
    }
  }

  class PassageSpan {
    private int begin, end;

    public PassageSpan(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    public boolean containedIn(int begin, int end) {
      if (begin <= this.begin && end >= this.end) {
        return true;
      } else {
        return false;
      }
    }
  }

}

class Passage {
  public String text;

  public int start, end;

  public Passage(String text, int start, int end) {
    this.text = text;
    this.start = start;
    this.end = end;
  }

}
