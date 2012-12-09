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

import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.jsoup.Jsoup;

import edu.cmu.lti.oaqa.framework.data.PassageCandidate;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorer;
import edu.cmu.lti.oaqa.openqa.hello.passage.KeytermWindowScorerProduct;

public class SentenceBasedPassageCandidateFinder {
  private String text;

  private String docId;

  private int textSize; // values for the entire text

  private int totalMatches;

  private int totalKeyterms;

  private Map<PassageCandidate, Integer> pcMap;

  // private int totalKeyterms;


  private KeytermWindowScorer scorer;

  public SentenceBasedPassageCandidateFinder(String docId, String text, KeytermWindowScorer scorer) {
    super();
    this.totalMatches = 0;
    this.totalKeyterms = 0;
    this.text = text;
    this.docId = docId;
    this.textSize = text.length();
    this.scorer = scorer;
    // this.threshold = 0.6; //threshold;
    pcMap = new HashMap<PassageCandidate, Integer>();

  }

  public List<PassageCandidate> extractPassages(String[] keyterms) {
    // String[] paragraphs = text.split("<P>");
    List<PassageSpan> paragraphs = splitParagraph(text);
    int start = 0, end = 0;

    List<PassageSpan> matchedSpans = new ArrayList<PassageSpan>();
    List<PassageCandidate> passageList = new ArrayList<PassageCandidate>();
    // System.out.println("starting paragraph spans");
    Iterator<PassageSpan> iparagprah = paragraphs.iterator();
    String paragraph = new String();
    PassageSpan ps;
    
    
    for ( String keyterm : keyterms ) {
      Pattern p = Pattern.compile( keyterm.toLowerCase() );
      Matcher m = p.matcher( text.toLowerCase() );
      while ( m.find() ) {
        PassageSpan match = new PassageSpan( m.start() , m.end() ) ;
        matchedSpans.add( match );
        totalMatches++;
      }
      if (! matchedSpans.isEmpty() ) {
        totalKeyterms++;
      }
    }
    
    
    while (iparagprah.hasNext()) {
      ps = iparagprah.next();
      paragraph = text.substring(ps.begin, ps.end);
      start = ps.begin;
      end = ps.end;
      // System.out.println("cleaning text...");
      String cleanText = Jsoup.parse(paragraph).text().replaceAll("([\177-\377\0-\32]*)", "");
      int keytermsFound = 0;
      int matchednum = 0;
      for (String keyterm : keyterms) {
        // System.out.println("matching keyterms...");
        Pattern p = Pattern.compile(keyterm.toLowerCase());
        Matcher mClean = p.matcher(cleanText.toLowerCase());
        while (mClean.find()) {
          PassageSpan match = new PassageSpan(mClean.start(), mClean.end());
          matchedSpans.add(match);
          matchednum++;
        }
        if (!matchedSpans.isEmpty()) {
          // matchingSpans.add(matchedSpans);
          keytermsFound++;
        }
      }
      if (matchednum != 0) {
        double score = scorer.scoreWindow(start, end, matchednum, totalMatches, keytermsFound,
                totalKeyterms, textSize);

        try {
          BioPassageCandidate pc = new BioPassageCandidate(docId, start, end, (float) score, null);
          pc.keytermMatches = keytermsFound;
          pc.setText(paragraph);
          passageList.add(pc);

        } catch (AnalysisEngineProcessException e) {
          // TODO Auto-generated catch block
          e.printStackTrace();
        }
      }
    }

    return passageList;
  }

  private double CheckContain(PassageCandidate pc, List<PassageCandidate> result) {
    int oldstart, oldend;
    int newstart = pc.getStart();
    int newend = pc.getEnd();
    double containratio = 0.0;
    for (PassageCandidate oldpc : result) {
      oldstart = oldpc.getStart();
      oldend = oldpc.getEnd();
      if (newend <= oldstart || oldend <= newstart)
        containratio = Math.max(0.0, containratio);
      else if (newstart >= oldstart && newend <= oldend)
        containratio = Math.max(1.0, containratio);
      else if (oldstart >= newstart && oldend <= newend)
        containratio = Math.max(1.0, containratio);
      else if (newstart > oldstart && newstart < oldend && newend > oldend) {
        containratio = Math.max(containratio, (oldend - newstart) / (double) (oldend - oldstart));
      } else if (oldstart > newstart && oldstart < newend && oldend > newend) {
        containratio = Math.max(containratio, (newend - oldstart) / (double) (oldend - oldstart));
      }
      if (containratio >= 0.9)
        return containratio;
    }
    return containratio;
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

  private List<PassageSpan> splitParagraph(String text) {


    StringBuffer temptext = new StringBuffer(text);
    List<PassageSpan> rawSpans = new ArrayList<PassageSpan>();
    List<PassageSpan> splitedSpans = new ArrayList<PassageSpan>();
    List<PassageSpan> sentences = new ArrayList<PassageSpan>();
    int start = 0;
    int startindice = -1;
    int pstart, pend, textend;
    while ((startindice = temptext.toString().toLowerCase().indexOf("<p>")) != -1) {

      temptext.delete(0, startindice + 3);
      start += (startindice + 3);


      pstart = temptext.toString().toLowerCase().indexOf("<p>");
      pend = temptext.toString().toLowerCase().indexOf("</p>");
      textend = temptext.toString().toLowerCase().indexOf("</txt>");

      if (textend != -1 && textend * pend < pend * pend && textend * pstart < pstart * pstart) {
        PassageSpan ps = new PassageSpan(start, start + textend);

        rawSpans.add(ps);
        temptext = temptext.delete(0, textend + 6);
        start += (textend + 6);

      }

      else if (pend != -1 && pstart > pend) {
        PassageSpan ps = new PassageSpan(start, start + pend);
        rawSpans.add(ps);
        temptext = temptext.delete(0, pend + 4);
        start += (pend + 4);


      }

      else if (pstart != -1) {
        PassageSpan ps = new PassageSpan(start, start + pstart);
        rawSpans.add(ps);
        temptext = temptext.delete(0, pstart + 3);
        start += pstart + 3;

      } else {
      }
    }

    String substring = new String();
    String subtoken = new String();
    String cleanText = new String();
    PassageSpan rawps;
    int substart, subend, psstart, psend, offset;
    Iterator<PassageSpan> ips = rawSpans.iterator();
    while (ips.hasNext()) {
      sentences.clear();
      rawps = ips.next();
      //REFINE THE PARAGRAPH
      //rawps = RefinePassage(rawps);
      //if(rawps == null)continue;
      substart = rawps.begin;
      subend = rawps.end;
      substring = text.substring(substart, subend);
      int count = StringUtils.countMatches(substring, " ");
      if (substring.length() > 20 && count / (double) substring.length() <= 0.4) {
        cleanText = Jsoup.parse(substring).text().replaceAll("([\177-\377\0-\32]*)", "");
        if ((cleanText.length() / (double) substring.length()) >= 0.6) {
          StringTokenizer tokenizer = new StringTokenizer(substring, ",.?!;");
          psstart = substart;
          psend = psstart;
          offset = 0;
          while (tokenizer.hasMoreTokens()) {
            psstart = psend;
            subtoken = tokenizer.nextToken();
            // offset = noneblankindex(subtoken);
            // psstart += offset;

            psend += subtoken.length() + 1;
            int totallength = subtoken.length();
            while (totallength <= 50) {
              if (tokenizer.hasMoreElements()) {
                subtoken = tokenizer.nextToken();
                totallength += subtoken.length();
                psend += subtoken.length() + 1;
              } else
                break;
            }
            PassageSpan sentence = new PassageSpan(psstart, psend);
            sentences.add(sentence);
          }
        }
      }
      
      Iterator<PassageSpan> isentence = sentences.iterator();
      PassageSpan ps1;
      while (isentence.hasNext()) {
        ps1 = isentence.next();
        splitedSpans.add(ps1);

      }
      
      MergeSentences(substart, subend, sentences, splitedSpans, 2);
      MergeSentences(substart, subend, sentences, splitedSpans, 3);
      MergeSentences(substart, subend, sentences, splitedSpans, 4);
      MergeSentences(substart, subend, sentences, splitedSpans, 5);
      MergeSentences(substart, subend, sentences, splitedSpans, 6);
      MergeSentences(substart, subend, sentences, splitedSpans, 7);
      MergeSentences(substart, subend, sentences, splitedSpans, 8);
      MergeSentences(substart, subend, sentences, splitedSpans, 9);
      MergeSentences(substart, subend, sentences, splitedSpans, 10);
    }

    return splitedSpans;
  }

  private void MergeSentences(int substart, int subend, List<PassageSpan> sentences,  List<PassageSpan> splitedSpans, int number){
    int mergestart, mergeend, count;
    PassageSpan ps1;
    Iterator<PassageSpan> isentence = sentences.iterator();
    if (sentences.size() == number) {
      PassageSpan mergedspan = new PassageSpan(substart, subend);
      splitedSpans.add(mergedspan);
    } else if (sentences.size() > number && isentence.hasNext()) {
      ps1 = isentence.next();
      mergestart = ps1.begin;

      while (isentence.hasNext()) {
        ps1 = isentence.next();
        count = 2;
        while (isentence.hasNext() && count < number){
          ps1 = isentence.next();
          count ++;
        }
        mergeend = ps1.end;
        PassageSpan mergedspan = new PassageSpan(mergestart, mergeend);
        splitedSpans.add(mergedspan);
        mergestart = ps1.begin;
      }
    }
  }
  
  private PassageSpan RefinePassage(PassageSpan oldps){
    int oldstart = oldps.begin;
    int oldend = oldps.end;
    int newstart = 0;
    int newend = 0;
    String cleanText = new String();
    String substring = text.substring(oldstart, oldend);
    cleanText = Jsoup.parse(substring).text().replaceAll("([\177-\377\0-\32]*)", "");
    if(cleanText.length() < 5) return null;
    
    int startindex = 0;
    int endindex = cleanText.length() - 1;
    while((cleanText.toCharArray()[startindex] == ' ' || cleanText.toCharArray()[startindex] == '.' || 
            cleanText.toCharArray()[startindex] == '*' || cleanText.toCharArray()[startindex] == '\n'||
            cleanText.toCharArray()[startindex] == ',' || cleanText.toCharArray()[startindex] == '?'||
            cleanText.toCharArray()[startindex] == '!' || cleanText.toCharArray()[startindex] <= 57) 
            && startindex < endindex - 1){
      startindex ++;
      }
    while((cleanText.toCharArray()[endindex] == ' ' || cleanText.toCharArray()[endindex] == '.' || 
            cleanText.toCharArray()[endindex] == '*' || cleanText.toCharArray()[endindex] == '\n'||
            cleanText.toCharArray()[endindex] == ',' || cleanText.toCharArray()[endindex] == '?'||
            cleanText.toCharArray()[endindex] == '!' || cleanText.toCharArray()[endindex] <= 57) 
            && endindex > startindex + 1){
      endindex --;
      }
    newstart = oldstart + substring.indexOf(cleanText.substring(startindex, startindex + 2));
    if(newstart == oldstart - 1)newstart = oldstart;
    newend = oldstart + substring.lastIndexOf(cleanText.substring(endindex-1, endindex + 1)) + 1;
    if(newend == oldstart)newend = oldend;
    else newend = newend + 1;
    
    PassageSpan newps = new PassageSpan(newstart, newend);
    return newps;
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
    SentenceBasedPassageCandidateFinder passageFinder1 = new SentenceBasedPassageCandidateFinder("1",
            "The quick brown fox jumped over the quick brown fox.",
            new KeytermWindowScorerProduct());
    SentenceBasedPassageCandidateFinder passageFinder2 = new SentenceBasedPassageCandidateFinder("1",
            "The quick brown fox jumped over the quick brown fox.", new KeytermWindowScorerSum());
    String[] keyterms = { "quick", "jumped" };
    List<PassageCandidate> windows1 = passageFinder1.extractPassages(keyterms);
    System.out.println("Windows (product scoring): " + windows1);
    List<PassageCandidate> windows2 = passageFinder2.extractPassages(keyterms);
    System.out.println("Windows (sum scoring): " + windows2);
  }

}