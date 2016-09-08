package ru.skinnyweb.server.utils;

import java.util.Date;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

public class DateUtils {
  private final static DateTimeFormatter fmtHM = DateTimeFormat.forPattern("HH:mm");
  private final static DateTimeFormatter fmtMD = DateTimeFormat.forPattern("MMM d");
  private final static DateTimeFormatter fmtYMD = DateTimeFormat.forPattern("MMM d YYYY");
  private final static DateTimeFormatter fmt = DateTimeFormat.forPattern("HH:mm, MMM d YYYY");
  private final static DateTime now = DateTime.now();

  public static String printDate(Date date) {
    DateTime dt = new DateTime(date);

    if (now.getYear() == dt.getYear() 
        && now.getDayOfYear() == dt.getDayOfYear()) {
      return fmtHM.print(dt);
    } else if (now.getYear() == dt.getYear()) {
      return fmtMD.print(dt);
    } else {
      return fmtYMD.print(dt);
    }
  }

  public static String printDateFull(Date date) {
    DateTime dt = new DateTime(date);
    return fmt.print(dt);
  }
}
