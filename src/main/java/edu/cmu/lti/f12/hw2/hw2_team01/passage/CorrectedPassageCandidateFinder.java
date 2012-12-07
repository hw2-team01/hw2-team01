package edu.cmu.lti.f12.hw2.hw2_team01.passage;

/*
 *  Copyright 2012 Carnegie Mellon University
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import com.google.common.collect.Sets;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerProduct;

public class CorrectedPassageCandidateFinder {
  private String text;

  private String docId;

  private int textSize; // values for the entire text

  private int totalMatches;

  private Map<PassageCandidate, Integer> pcMap;

  // private int totalKeyterms;

  private double threshold;

  private KeytermWindowScorer scorer;

  public CorrectedPassageCandidateFinder(String docId, String text, KeytermWindowScorer scorer) {
    super();
    this.text = text;
    this.docId = docId;
    this.textSize = text.length();
    this.scorer = scorer;
    // this.threshold = 0.6; //threshold;
    pcMap = new HashMap<PassageCandidate, Integer>();
    
  }

  public List<PassageCandidate> extractPassages(String[] keyterms) {
    String[] paragraphs = text.split("<p>");
    int start = 0, end = 0;

    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<PassageCandidate> passageList = new ArrayList<PassageCandidate>();
   // System.out.println("starting paragraph spans");
    for (String paragraph : paragraphs) {
      start = end + 3;
      end = start + paragraph.length();
     // System.out.println("cleaning text...");
      String cleanText = Jsoup.parse(paragraph).text().replaceAll("([\177-\377\0-\32]*)", "");
      int totalKeyterms = 0;
      for (String keyterm : keyterms) {
   //     System.out.println("matching keyterms...");
        Pattern p = Pattern.compile(keyterm);
        Matcher mClean = p.matcher(cleanText);
        while (mClean.find()) {
          PassageSpan match = new PassageSpan(mClean.start(), mClean.end());
          matchedSpans.add(match);
          totalMatches++;
        }
        if (!matchedSpans.isEmpty()) {
          // matchingSpans.add(matchedSpans);
          totalKeyterms++;
        }
        try {

          BioPassageCandidate pc = new BioPassageCandidate(docId, start, end, null);
          pc.keytermMatches = totalKeyterms;
          pc.addSpans(matchedSpans);
          pc.setText(paragraph);
          pcMap.put(pc,totalKeyterms);
          // pc.setProbablity(getScore(pc,totalmatches));
          passageList.add(pc);

        } catch (AnalysisEngineProcessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }

    }
    //System.out.println("ranking passages...");
    // rank passage candidate
    for (PassageCandidate pc : passageList) {
      pc.setProbablity((float) getScore(pc, totalMatches));
    }
   // System.out.println("ranking results");
    Collections.sort(passageList, new PassageCandidateComparator());
   // System.out.println("returning results");

    return passageList;
  }

  // Very simple method, rank passages by the normalized keyterm matches
  public float getScore(PassageCandidate pc, int totalMatches) {
    return (float)pcMap.get(pc) / (float)totalMatches;
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
    private int begin, end, cleanBegin, cleanEnd;

    public PassageSpan(int begin, int end) {
      this.begin = begin;
      this.end = end;
    }

    public PassageSpan(int begin, int end, int cleanBegin, int cleanEnd) {
      this.begin = begin;
      this.end = end;
      this.cleanBegin = cleanBegin;
      this.cleanEnd = cleanEnd;
    }

    public boolean containedIn(int begin, int end) {
      if (begin <= this.begin && end >= this.end) {
        return true;
      } else {
        return false;
      }
    }
  }

  public static void main(String[] args) {
    CorrectedPassageCandidateFinder passageFinder1 = new CorrectedPassageCandidateFinder("1",
            "The quick brown fox jumped over the quick brown fox.",
            new KeytermWindowScorerProduct());
    CorrectedPassageCandidateFinder passageFinder2 = new CorrectedPassageCandidateFinder("1",
            "The quick brown fox jumped over the quick brown fox.", new KeytermWindowScorerSum());
    String[] keyterms = { "quick", "jumped" };
    List<PassageCandidate> windows1 = passageFinder1.extractPassages(keyterms);
    System.out.println("Windows (product scoring): " + windows1);
    List<PassageCandidate> windows2 = passageFinder2.extractPassages(keyterms);
    System.out.println("Windows (sum scoring): " + windows2);
  }

}