package yokwe.iex.cloud;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;

public enum Version {
	V1    ("v1"),
	BETA  ("beta"),
	STABLE("stable"),
	LATEST("latest");
	
	public final String url;
	
	Version(String newValue) {
		url = newValue;
	}
	
	@Override
	public String toString() {
		return String.format("%s", name());
	}
	
	
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Version.class);

	private static Map<String, Version> versionMap = new TreeMap<>();
	static {
		for(Version type: Version.class.getEnumConstants()) {
			versionMap.put(type.name(), type);
		}
	}
	public static Version toVersion(String value) {
		if (versionMap.containsKey(value)) {
			return versionMap.get(value);
		} else {
			logger.error("Unexpected value  {}", value);
			throw new UnexpectedException("Unexpected value");
		}
	}
	public static Version getFromProperty(String propertyNam) {
		String stringValue = System.getProperty(propertyNam);
		if (stringValue != null) {
			return toVersion(stringValue);
		} else {
			logger.error("No property  {}", propertyNam);
			throw new UnexpectedException("No property");
		}
	}
	public static Version getFromProperty(String propertyNam, String defaultValue) {
		String stringValue = System.getProperty(propertyNam, defaultValue);
		return toVersion(stringValue);
	}
}