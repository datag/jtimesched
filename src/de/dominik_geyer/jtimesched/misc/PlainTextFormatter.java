package de.dominik_geyer.jtimesched.misc;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.LogRecord;
import java.util.logging.SimpleFormatter;

public class PlainTextFormatter extends SimpleFormatter {
	@Override
	public synchronized String format(LogRecord record) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd (E) HH:mm:ss");
		
		return String.format("%s [%s]: %s%n",
				sdf.format(new Date(record.getMillis())),
				record.getLevel(),
				record.getMessage());
	}
	
}
