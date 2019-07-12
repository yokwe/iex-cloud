package yokwe.iex.cloud;

import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;

public enum Type {
	PRODUCTION(Token.PRODUCTION, "https://cloud.iexapis.com"),
	SANDBOX   (Token.SANDBOX,    "https://sandbox.iexapis.com");
	
	public final Token  token;
	public final String url;
	
	Type(Token token, String newURL) {
		this.token = token;
		this.url   = newURL;
	}
	
	@Override
	public String toString() {
		return String.format("%s", this.name());
	}
	
	
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Type.class);

	private static Map<String, Type> typeMap = new TreeMap<>();
	static {
		for(Type type: Type.class.getEnumConstants()) {
			typeMap.put(type.name(), type);
		}
	}
	public static Type toType(String value) {
		if (typeMap.containsKey(value)) {
			return typeMap.get(value);
		} else {
			logger.error("Unexpected value  {}", value);
			throw new UnexpectedException("Unexpected value");
		}
	}
	public static Type getFromProperty(String propertyName) {
		String stringValue = System.getProperty(propertyName);
		if (stringValue != null) {
			return toType(stringValue);
		} else {
			logger.error("No property  {}", propertyName);
			throw new UnexpectedException("No property");
		}
	}
	public static Type getFromProperty(String propertyName, String defaultValue) {
		String stringValue = System.getProperty(propertyName, defaultValue);
		return toType(stringValue);
	}
}