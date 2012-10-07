/* jTimeSched - A simple and lightweight time tracking tool
 * Copyright (C) 2010-2012 Dominik D. Geyer <dominik.geyer@gmail.com>
 * See LICENSE.txt for details.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.dominik_geyer.jtimesched.project;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProjectTime {
	private static final String fmtDate = "yyyy-MM-dd";
	
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
	
	public static String formatDate(Date d) {
		SimpleDateFormat sdf = new SimpleDateFormat(ProjectTime.fmtDate);
		return sdf.format(d);
	}
	
	public static Date parseDate(String strDate) throws ParseException {
		SimpleDateFormat sdf = new SimpleDateFormat(ProjectTime.fmtDate);
		return sdf.parse(strDate);
	}
}
