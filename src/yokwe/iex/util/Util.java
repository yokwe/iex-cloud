package yokwe.iex.util;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

public final class Util {
	//
	// Methods about long to LocalDateTime
	//
	public static LocalDateTime getLocalDateTimeFromSecond(int value) {
		return LocalDateTime.ofInstant(Instant.ofEpochSecond((long)value), ZoneOffset.UTC);
	}
	public static LocalDateTime getLocalDateTimeFromMilli(long value) {
		return LocalDateTime.ofInstant(Instant.ofEpochMilli(value), ZoneOffset.UTC);
	}
	public static LocalDateTime getLocalDateTimeFromMicro(long value) {
		return getLocalDateTimeFromNano(value * 1000);
	}
	public static LocalDateTime getLocalDateTimeFromNano(long value) {
		long nano   = value % 1_000_000_000;
		long second = value / 1_000_000_000;
		
		return LocalDateTime.ofInstant(Instant.ofEpochSecond(second, nano), ZoneOffset.UTC);
	}
}
