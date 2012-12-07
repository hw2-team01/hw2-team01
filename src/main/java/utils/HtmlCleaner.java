package utils;



import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Scanner;

/**
 * HtmlCleaner removes the tags from an HTML document preserving the character positions.
 */
public abstract class HtmlCleaner {

    final static String TAG_SCRIPT = "script";
    final static String TAG_STYLE = "style";

    /**
     * HTML entities that are converted to whitespace.
     * 
     * We remove only the most common entities that correspond to punctuation
     * symbols, since they would not affect token boundaries.
     * 
     */
    public static final Set<String> removedEntNames = new HashSet<String>();
    
    
    static {
        // Entities that are converted to whitespace
        String[] entities = new String[] {
                "nbsp", "amp", "quot", "gt", "raquo", "lt", "ldquo", "rdquo", 
                "bull", "hellip", "laquo", "middot", "rsquo", "ndash", "mdash",
                "curren", "bdquo", "lsquo", "copy", "rarr", "apos", "reg"
        };
        for (String entName : entities) {
            removedEntNames.add(entName);
        }
    }

    public String cleanString(String content) {
        char[] output = cleanContent(content);        
        return new String(output);
    }

    public abstract char[] cleanContent(String content);

    /**
     * Replaces the characters by whitespace, but keeps new lines.
     * 
     * @param content
     * @param fromIndex
     * @param toIndex
     */
    public static void eraseString(char[] content, int fromIndex, int toIndex) {
        for (int i = fromIndex; i < toIndex; i++) {
            if (content[i] != '\n' && content[i] != '\r' && content[i] != 12) {
                content[i] = ' ';
            }
        }
    }
    
    public static class ByteCleaner extends HtmlCleaner {

        private static void cleanEntity(char[] content, int start){
            char item;
            String entity = null;
            int entEnd = 0;
            for (int j=start+1; 
                    entity == null &&
                    j < content.length && j < (start + 8); j++){
                item = content[j];
                if (item == ';'){
                    entity = new String(Arrays.copyOfRange(content, start + 1, j));
                    entEnd = j + 1;
                }
            }

            
            if (entity != null && removedEntNames.contains(entity)){
                eraseString(content, start, entEnd);
            }
        }
        
        public static boolean matchString(char[] content, int start, String str) {
            char item;
            char[] chars = str.toCharArray();
            int i=0;

            for (i=0;
                    i < chars.length &&
                    i + start < content.length; i++){
                item = content[i+start];
                item = Character.toLowerCase(item);
                if (item != chars[i]){
                    return false;
                }
            }
            return i == chars.length;
        }
        

        @Override
        public char[] cleanContent(String content) {
            char[] data = content.toCharArray();

            int deleteFrom = -1;
            char item = 0;
            char nextItem = 0;
            
            int startScriptTag = -1;
            int startStyleTag = -1;
            
            for (int i=0; i < data.length; i++){
                item = data[i];
                if (i+1 < data.length) {
                    nextItem = data[i+1];
                }

                if (item == '<'){
                    
                    // Delete the headers
                    if (deleteFrom == -1) {
                        eraseString(data, 0, i);
                    }
                    
                    if (matchString(data, i+1, "style")){
                        startStyleTag = i;
                    }
                    
                    if (startStyleTag > 0 && matchString(data, i+1, "/style")){
                        eraseString(data, startStyleTag, i+1);
                        startStyleTag = -1;
                    }
                    
                    if (matchString(data, i+1, "script")){
                        startScriptTag = i;
                    }
                    
                    if (startScriptTag > 0 && matchString(data, i+1, "/script")){
                        eraseString(data, startScriptTag, i+1);
                        startScriptTag = -1;
                    }

                    if (nextItem == '!' && i + 3 < data.length){
                        String lookAhead = new String(Arrays.copyOfRange(data, i, i+4));
                        if (lookAhead.equals("<!--")){
                            eraseString(data, i, i+4+1);
                        }
                    }

                    deleteFrom = i;
                }
                if (item == '>' && i > deleteFrom && deleteFrom > -1){
                    eraseString(data, deleteFrom, i+1);
                }
                
                if (item == '&'){
                    cleanEntity(data, i);
                }

                nextItem = 0;
            }
            return data;
        }
    }
    

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: HtmlCleaner input.html output.txt");
            System.exit(1);
        }

        try {
            File fInput = new File(args[0]);
            File fOutput = new File(args[1]);

            String content = new Scanner(fInput).useDelimiter("\\A").next();
            
            HtmlCleaner cleaner = new ByteCleaner();
            
            String output = cleaner.cleanString(content);

            PrintWriter out = new PrintWriter(fOutput);
            out.print(output);
            out.close();
        } catch (Exception e) {
            System.err.println("Something went wrong:");
            e.printStackTrace();
        }
    }
}