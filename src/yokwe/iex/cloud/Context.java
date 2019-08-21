package yokwe.iex.cloud;

import java.util.Map;

import org.slf4j.LoggerFactory;

import yokwe.UnexpectedException;
import yokwe.iex.cloud.Base.Format;

public class Context {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(Context.class);
	
	public final Type    type;
	public final Version version;
	public final boolean httpTrace;
	public final String  url;
	private      int     tokenUsed;
	private      int     tokenUsedTotal;
	
	public Context(Type type, Version version, boolean httpTrace) {
		this.type           = type;
		this.version        = version;
		this.httpTrace      = httpTrace;
		this.url            = String.format("%s/%s", type.url, version.url);
		this.tokenUsed      = 0;
		this.tokenUsedTotal = 0;
	}
	
	@Override
	public String toString() {
		return String.format("[%s %s %s %s]", type.toString(), version.toString(), httpTrace, url);
	}
	
	public void setTokenUsed(int value) {
		tokenUsed       = value;
		tokenUsedTotal += value;
	}
	public int getTokenUsed() {
		return tokenUsed;
	}
	public int getTokenUsedTotal() {
		return tokenUsedTotal;
	}
	
	public String getBaseURL(String endPoint) {
		return String.format("%s%s", url, endPoint);
	}
	
	public String getURL(String base) {
		return String.format("%s?token=%s", base, type.token.secret);
	}
	public String getURL(String base, Format format) {
		return String.format("%s?token=%s&format=%s", base, type.token.secret, format.value);
	}
	public String getURL(String base, Format format, Map<String, String> paramMap) {
		// Sanity check
		if (paramMap == null) {
			logger.error("paramMap == null");
			throw new UnexpectedException("paramMap == null");
		}
		
		StringBuilder ret = new StringBuilder(getURL(base, format));
		
		for(Map.Entry<String, String> entry: paramMap.entrySet()) {
			ret.append(String.format("&%s=%s", Base.encodeString(entry.getKey()), Base.encodeString(entry.getValue())));
		}
		return ret.toString();
	}
}