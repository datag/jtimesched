package de.dominik_geyer.jtimesched.project;

import java.text.ParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTime {
	private ProjectTime() {}
	
	public static String formatSeconds(int s) {
		return String.format("%d:%02d:%02d", s/3600, (s%3600)/60, (s%60));
	}
	
	public static int parseSeconds(String strTime) throws ParseException {
		 Pattern p = Pattern.compile("(\\d+):([0-5]?\\d):([0-5]?\\d)");	// 0:00:00
		 Matcher m = p.matcher(strTime);
		 
		 if (!m.matches())
			 throw new ParseException("Invalid seconds-string", 0);
		 
		 int hours = Integer.parseInt(m.group(1));
		 int minutes = Integer.parseInt(m.group(2));
		 int seconds = Integer.parseInt(m.group(3));
		 
		 return (hours * 3600 + minutes * 60 + seconds);
	}
}
