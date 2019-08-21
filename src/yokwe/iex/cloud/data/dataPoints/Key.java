package yokwe.iex.cloud.data.dataPoints;

import java.util.List;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Key extends Base implements Comparable<Key>{
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Key.class);

	public static final int    DATA_WEIGHT = 0; // FREE
	public static final String METHOD      = "/data-points";

	// key,weight,description,lastUpdated
	public String key;
	public int    weight;
	public String description;
	public String lastUpdated;
	
	public Key() {
		key         = null;
		weight      = 0;
		description = null;
		lastUpdated = null;
	}
	
	public Key(JsonObject jsonObject) {
		super(jsonObject);
	}
	
	@Override
	public int compareTo(Key that) {
		return this.key.compareTo(that.key);
	}
	
	public static List<Key> getInstance(Context context, String symbol) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(String.format("%s/%s", base, encodeString(symbol)), Format.CSV);
		
		List<Key> ret = getCSV(context, url, Key.class);
		
		// Check token usage
		{
			int actual = context.getTokenUsed();
			int expect = DATA_WEIGHT;
			if (actual != expect) {
				logger.error("Unexpected token usage {}  {}", actual, expect);
				throw new UnexpectedException("Unexpected token usage");
			}
		}
		return ret;
	}
}
