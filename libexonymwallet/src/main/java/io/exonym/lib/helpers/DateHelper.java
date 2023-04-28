package io.exonym.lib.helpers;

import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.temporal.TemporalUnit;
import java.util.GregorianCalendar;

public class DateHelper {

	public static GregorianCalendar getCurrentUtcTime(){
		Instant now = Instant.now();
		ZonedDateTime zdt = ZonedDateTime.ofInstant(now, ZoneId.of("UTC"));
		return GregorianCalendar.from(zdt);
		
	}

	public static long getCurrentUtcMillis() {
		return Instant.now().toEpochMilli();

	}

	public static String currentIsoUtcDateTime(){
		return isoUtcDateTime(Instant.now());
		
	}
	
	public static String currentIsoUtcDate(){
		return isoUtcDate(Instant.now());

	}	

	public static String currentBareIsoUtcDate(){
		return bareIsoUtcDate(Instant.now());
		
	}

	public static String bareIsoUtcDate(Instant target){
		return isoUtcDate(target).replaceAll("-", "");

	}

	public static String yesterdayBareIsoUtcDate(){
		Instant now = Instant.now();
		Period period = Period.ofDays(1);
		String yesterday = now.minus(period).toString();
		yesterday = yesterday.split("T")[0];
		return yesterday.replaceAll("-", "");

	}

	public static String isoUtcDateTime(Instant dt){
		String date = (dt.toString()).split("\\.")[0] + "Z";
		return date;

	}

	public static String isoUtcDateTime(long msSince){
		Instant instant = Instant.ofEpochMilli(msSince);
		return isoUtcDateTime(instant);

	}

	public static String isoUtcDate(Instant dt){
		String date = dt.toString().split("T")[0];
		return date;

	}

	public static long getUtcMillisInDays(int days) {
		return Instant.now().plus(Period.ofDays(days)).toEpochMilli();

	}



	public static boolean isTargetInFutureWithinPeriod(Instant target, Period period){
		if (target==null || period == null){
			throw new RuntimeException("Target= " + target + " Period=" + period);
		}
		Instant now = Instant.now();
		Instant validUntil = now.plus(period);
		boolean isInRange = now.isBefore(target);
		boolean isAllowed = target.isBefore(validUntil);
		if (isAllowed && isInRange){
			return true;

		} else{
			throw new RuntimeException("allowed=" + isAllowed + " isInRange" + isInRange);


		}
	}


}
