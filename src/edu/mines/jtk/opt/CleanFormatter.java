package edu.mines.jtk.opt;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;
import java.util.logging.*;

/** Format log messages without any extras.
    @author W.S. Harlan, Landmark Graphics
 */
public class CleanFormatter extends Formatter {
  private static String s_prefix = "";

  /** The format used by getTimeStamp() methods,
      like 20050621-153318 */
  private static final DateFormat TIMESTAMP_FORMAT =
    new SimpleDateFormat("yyyyMMdd-HHmmss");

  /** Line separator */
  private static final String NL = System.getProperty("line.separator");

  /** Prefix a string to all warnings or severe errors
      We use this method only for unit tests,
      to distinguish the source of warnings.
      @param prefix new prefix for all lines
   */
  public static void setWarningPrefix(String prefix) {
    s_prefix = prefix;
  }

  private Level lastLevel = Level.INFO;
  // from Formatter
  @Override public synchronized String format(LogRecord record) {
    // First try default localization of entire string
    String message = formatMessage(record);
    if (message == null) return null;
    if (message.length() == 0) return message;

    // More advanced localization of substrings
    message = Localize.filter(message, record.getResourceBundle());

    if (message.endsWith("\\")) {
      message = message.substring(0,message.length()-1);
    } else if (message.matches("^\\s*\n?$")) {
    } else {
      message = message +"\n";
    }
    Level level = record.getLevel();
    if (level.equals(Level.INFO)) {
      // do nothing
    } else if (level.equals(Level.WARNING)) {
      if (message.indexOf("WARNING") == -1) {
        message = prependToLines(s_prefix+level+": ", message);
      } else {
        message = s_prefix+message;
      }
    } else if (level.equals(Level.SEVERE)) {
      message = prependToLines(level+": ", message);
      if (!lastLevel.equals(Level.SEVERE)) {
        message =
          s_prefix + "**** SEVERE WARNING **** ("+
          record.getSourceClassName()+ "." + record.getSourceMethodName()+" "+
          getTimeStamp(record.getMillis())+" "+
          "#" + record.getThreadID() + ")\n" + message;
      }
    } else if (level.equals(Level.FINE)
               || level.equals(Level.FINER)
               || level.equals(Level.FINEST)) {
      String shortPackage = record.getLoggerName();
      int index = shortPackage.lastIndexOf(".");
      if (index>0) shortPackage = shortPackage.substring(index+1);
      message = prependToLines (level+" "+shortPackage+": ", message);
    } else {
      message = prependToLines
        (level+ " " + s_time_formatter.format(new Date())
         + " "+ record.getLoggerName()+": ",
         message);
    }
    lastLevel = level;
    return message;
  }
  private static DateFormat s_time_formatter
    = new SimpleDateFormat("HH:mm:ss.SSS");

  /** Run tests.
      @param argv command line
  */
  public static void main(String[] argv) {
    CleanHandler.setDefaultHandler();
    Logger logger = Logger.getLogger("edu.mines.jtk.opt.CleanFormatter");
    CleanFormatter cf = new CleanFormatter();
    String[] messages = new String[] {"one", "two", "three"};
    Level[] levels = new Level[] {Level.INFO, Level.WARNING, Level.SEVERE};
    String[] s = new String[3];
    for (int i=0; i<messages.length; ++i) {
      LogRecord lr = new LogRecord(levels[i], messages[i]);
      lr.setSourceClassName("Class");
      lr.setSourceMethodName("method");
      s[i] = cf.format(lr);
      assert s[i].endsWith(messages[i]+"\n");
      logger.info("|"+s[i]+"|");
    }
    assert s[0].equals("one\n"): s[0];
    assert s[1].equals("WARNING: two\n") : s[1];
    assert s[2].matches("^\\*\\*\\*\\* SEVERE WARNING \\*\\*\\*\\* "+
                        "\\(Class.method \\d+-\\d+ #.*\\)\n"+
                        "SEVERE: three\n$") :s[2];
  }

  /** Prepend a string to every line of text in a String
      @param prepend String to be prepended
      @param lines Lines separated by newline character, from
      System.getProperty("line.separator");
      @return Modified lines
  */
  private static String prependToLines(String prepend, String lines) {
    if (lines == null) return null;
    if (prepend == null) return lines;
    StringBuilder result = new StringBuilder();
    boolean hasFinalNL = lines.endsWith(NL);
    StringTokenizer divided = new StringTokenizer(lines, NL);
    while (divided.hasMoreTokens()) {
      result.append(prepend + divided.nextToken());
      if (divided.hasMoreTokens() || hasFinalNL) result.append(NL);
    }
    return result.toString();
  }

  /** Return a concise string that can be added as a timestamp to
      filenames
      @param date Date for timestamp
      @return String in format TIMESTAMP_FORMAT.
  */
  private static String getTimeStamp(Date date) {
    synchronized (TIMESTAMP_FORMAT) { // format is not thread-safe
      return TIMESTAMP_FORMAT.format(date);
    }
  }

  /** Return a concise string that can be added as a timestamp to
      filenames
      @param date Time in milliseconds since 1970
      @return String in format TIMESTAMP_FORMAT.
  */
  private static String getTimeStamp(long date) {
    return getTimeStamp(new Date(date));
  }

}
