package yokwe.iex.cloud;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;

public class Context {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Context.class);
	
	public final Type   type;
	public final Version version;
	public final String  url;
	
	public Context(Type type, Version version) {
		this.type    = type;
		this.version = version;
		this.url = String.format("%s/%s", type.url, version.url);
	}
	
	@Override
	public String toString() {
		return String.format("[%s %s %s]", type.toString(), version.toString(), url);
	}
	
	public static String encodeSymbol(String symbol) {
		try {
			String ret = URLEncoder.encode(symbol, "UTF-8");
			return ret;
		} catch (UnsupportedEncodingException e) {
			String exceptionName = e.getClass().getSimpleName();
			logger.error("{} {}", exceptionName, e);
			throw new UnexpectedException(exceptionName, e);
		}
	}
	public static String[] encodeSymbol(String[] symbols) {
		String[] ret = new String[symbols.length];
		for(int i = 0; i < symbols.length; i++) {
			ret[i] = encodeSymbol(symbols[i]);
		}
		return ret;
	}


	public String getURL(String method) {
		return String.format("%s%s?token=%s", url, method, type.token.secret);
	}
	public String getURLAsCSV(String method) {
		return String.format("%s%s?token=%s&format=csv", url, method, type.token.secret);
	}
	public String getURLAsCSV(String method, String symbol) {
		return String.format("%s%s/%s?token=%s&format=csv", url, method, encodeSymbol(symbol), type.token.secret);
	}
}