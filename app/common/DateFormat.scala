package common

import java.util.Locale

import org.joda.time.{DateTime, YearMonth}

/**
  * Created by angebagui on 12/03/2016.
  */
object DateFormat {

  def futureContext(millis: Long): String = {
    val dateTime = new DateTime(millis)
    val builder = new StringBuilder

    val yearMonth= YearMonth.fromDateFields(dateTime.toDate)
    builder.append(get(dateTime.getDayOfMonth))
    builder.append(" ")
    builder.append(yearMonth.monthOfYear().getAsShortText(Locale.US))
    builder.append(",")
    builder.append(" ")
    builder.append(get(dateTime.getHourOfDay))
    builder.append(":")
    builder.append(get(dateTime.getMinuteOfHour))

    builder.toString
  }
  private def get(number: Int): String =
    if (number<10)
      "0"+number
    else
       String.valueOf(number)


}
