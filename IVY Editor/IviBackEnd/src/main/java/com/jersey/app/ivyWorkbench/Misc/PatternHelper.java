
package ivyWorkbench.Misc;

import java.util.*;
import java.util.regex.*;

/**
 *
 * <p>Title: Trace Visualizer Component</p>
 *
 * <p>Description: </p>
 *
 * Contains string manipulation methods which are used throughout the program.
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author Nuno Sousa
 * @version 2.0
 */
public class PatternHelper {

  /**
   * Replace a string (defined by the pattern) in a line by a string.
   *
   * @param line String
   * @param replaceWith String
   * @param replace Pattern
   * @return String
   */
  public static String replacePattern(String line, String replaceWith,
                                      Pattern replace) {
    Matcher matcher = replace.matcher(line);
    StringBuffer stringBuffer = new StringBuffer(); //Deprecated collection, but required by matcher

    boolean result;

    // Loop through and create a new String
    // with the replacements
    while (result = matcher.find()) {
      matcher.appendReplacement(stringBuffer,
                                (matcher.group(1)) + replaceWith +
                                (matcher.group(2)));
    }
    // Add the last segment of input to the new String
    matcher.appendTail(stringBuffer);
    return stringBuffer.toString();
  }

  /**
   * Returns a ArrayList with the strings which were seperated by dots.
   *
   * @param splitString String
   * @return ArrayList
   */
  public static ArrayList<String> splitString(String splitString) {
    String[] s = splitString.split("\\.");
    ArrayList<String> v = new ArrayList<String> ();
    for (int i = 0; i < s.length; i++) {
      v.add(s[i]);
    }
    return v;
  }

  /**
   * Search a line for a pattern.
   *
   * @param line String
   * @param pattern String
   * @return boolean
   */
  public static boolean searchLineForPattern(String line, String pattern) {
    return searchLineForPattern(line, Pattern.compile(pattern));
  }

  /**
   * Search a line for a pattern.
   *
   * @param line String
   * @param searchPattern Pattern
   * @return boolean
   */
  public static boolean searchLineForPattern(String line, Pattern searchPattern) {
    Matcher matcher = searchPattern.matcher(line);
    return (matcher.lookingAt());
  }

  /**
   * Method which applies a pattern to a ArrayList of strings and
   * returns a ArrayList of ArrayLists with the results.
   *
   * @param pattern String
   * @param applyTo ArrayList
   * @param groups int
   * @return ArrayList
   */
  public static ArrayList<ArrayList<String>> filter(String pattern, ArrayList<String>
      applyTo, int groups) {
    Pattern searchPattern = Pattern.compile(pattern);
    Matcher matcher;
    ArrayList<String> tmp;
    ArrayList<ArrayList<String>> results = new ArrayList<ArrayList<String>> ();

    // Create a ArrayList for each group
    for (int i = 0; i < groups; i++) {
      results.add(new ArrayList<String> ());
    }

    for (int i = 0; i < applyTo.size(); i++) {
      matcher = searchPattern.matcher( (String) applyTo.get(i));
      if (matcher.lookingAt()) {
        for (int j = 0; j < groups; j++) {
          tmp = (ArrayList<String>) results.get(j);
          String s = matcher.group(j + 1);
          tmp.add(s);
        }
      }
    }
    return results;
  }
}

