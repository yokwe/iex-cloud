package yokwe.iex.cloud.data.reference;

import java.util.Collections;
import java.util.List;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Sectors extends Base implements Comparable<Sectors> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Sectors.class);

	public static final int    DATA_WEIGHT = 1; // 1 per call
	public static final String METHOD      = "/ref-data/sectors";

/*
[{"name":"Electronic Technology"}, ... ,{"name":"Government"}]
 */
	public static List<Sectors> getInstance(Context context) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(base);
		
		List<Sectors> ret = getArray(context, url, Sectors.class);
		Collections.sort(ret);

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
	
	public String name;
	
	public Sectors() {
		name = null;
	}
	
	public Sectors(JsonObject jsonObject) {
		super(jsonObject);
	}
	
	@Override
	public int compareTo(Sectors that) {
		return this.name.compareTo(that.name);
	}
}
