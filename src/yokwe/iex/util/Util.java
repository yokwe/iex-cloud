package yokwe.iex.util;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;

public final class Util {

	// Use America/New_York as default time zone
	public static final ZoneId     ZONE_ID     = ZoneId.of("America/New_York");
	public static final ZoneOffset ZONE_OFFSET = ZONE_ID.getRules().getOffset(Instant.now());

	//
	// Methods about long to OffsetDateTime
	//
	public static OffsetDateTime getOffsetDateTimeFromSecond(int value) {
		return getOffsetDateTimeFromSecond(value, ZONE_OFFSET);
	}
	public static OffsetDateTime getOffsetDateTimeFromMilli(long value) {
		return getOffsetDateTimeFromMilli(value, ZONE_OFFSET);
	}
	public static OffsetDateTime getOffsetDateTimeFromMicro(long value) {
		return getOffsetDateTimeFromMicro(value, ZONE_OFFSET);
	}
	public static OffsetDateTime getOffsetDateTimeFromNano(long value) {
		return getOffsetDateTimeFromNano(value, ZONE_OFFSET);
	}
	public static OffsetDateTime getOffsetDateTimeFromSecond(int value, ZoneOffset zoneOffset) {
		return OffsetDateTime.ofInstant(Instant.ofEpochSecond((long)value), zoneOffset);
	}
	public static OffsetDateTime getOffsetDateTimeFromMilli(long value, ZoneOffset zoneOffset) {
		return OffsetDateTime.ofInstant(Instant.ofEpochMilli(value), zoneOffset);
	}
	public static OffsetDateTime getOffsetDateTimeFromMicro(long value, ZoneOffset zoneOffset) {
		return getOffsetDateTimeFromNano(value * 1000, zoneOffset);
	}
	public static OffsetDateTime getOffsetDateTimeFromNano(long value, ZoneOffset zoneOffset) {
		long nano   = value % 1_000_000_000;
		long second = value / 1_000_000_000;
		
		return OffsetDateTime.ofInstant(Instant.ofEpochSecond(second, nano), zoneOffset);
	}
}
