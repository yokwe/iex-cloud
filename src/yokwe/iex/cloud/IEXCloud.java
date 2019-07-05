package yokwe.iex.cloud;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.ZoneId;
import java.time.ZoneOffset;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IEXCloud {
	static final Logger logger = LoggerFactory.getLogger(IEXCloud.class);
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface JSONName {
		String value();
	}
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface IgnoreField {
	}
	
	public enum TimeZone {
		UTC,
		LOCAL,
		NEW_YORK,
	}
	
	public static final ZoneId UTC      = ZoneOffset.UTC;
	public static final ZoneId LOCAL    = ZoneId.systemDefault();
	public static final ZoneId NEW_YORK = ZoneId.of("America/New_York");
	
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.FIELD)
	public @interface UseTimeZone {
		TimeZone value();
	}
}
