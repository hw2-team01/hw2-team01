package edu.cmu.lti.f12.hw2.hw2_team01.passage;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;

public class WindowsExtractor {
  private String WindowKind;

  public WindowsExtractor(String WindowKind) {
    this.WindowKind = WindowKind;
  }

  public List<Passage> ExtractWindow(String htmlText) {
    if (this.WindowKind == "para") {
      return extractParagraph(htmlText);
    } else if(this.WindowKind == "sentence")
    {
      return extractSentence(htmlText);
    }else //merge
    {
      List<Passage> initial = extractSentence(htmlText);
      return Merging(initial, 50);
    }
  }

  private List<Passage> extractSentence(String htmlText) {
    List<Passage> result = new ArrayList<Passage>();

    String[] paragraph = htmlText.split("<p>");
    System.out.println("line num:" + paragraph.length);

    int start = 0, end = 0;
    for (String para : paragraph) {
      start = end + 3;
      String[] sentence = para.split("[\\.,]");
      for (String sent : sentence) {
        start = end + 1;
        end = start + sent.length();

        String text = Jsoup.parse(sent).text().replaceAll("([\177-\377\0-\32]*)", "");

        System.out.println("clean text:" + text);
        result.add(new Passage(text, start, end));
      }
    }
    System.out.println("sentence num:" + result.size());
    return result;
  }

  private List<Passage> extractParagraph(String htmlText) {
    List<Passage> result = new ArrayList<Passage>();

    String[] paragraph = htmlText.split("<p>");
    System.out.println("line num:" + paragraph.length);

    int start = 0, end = 0;
    for (String para : paragraph) {
      start = end + 3;
      end = start + para.length();

      String text = Jsoup.parse(para).text().replaceAll("([\177-\377\0-\32]*)", "");
      System.out.println("clean text:" + text);
      result.add(new Passage(text, start, end));
    }
    System.out.println("paragraph num:" + result.size());
    return result;
  }

  private List<Passage> Merging(List<Passage> original, int threshold) {
    List<Passage> result = new ArrayList<Passage>();

    int length = 0;
    int start=0, end=start;
    String text = "";

    boolean beginMerge = true;
    
    for (Passage item : original) {
      if (beginMerge) {
        start = item.start;
        beginMerge = false;
      }

      length += item.text.length();
      text += item.text;
      end = item.end;
      
      if (length >= threshold) {
        Passage additem = new Passage(text, start, end);
        result.add(additem);
        length = 0;
        text = "";
        beginMerge = true;
      }
    }
    
    System.out.println("num of merging parts:" + result.size());
    
    return result;

  }
}
