package yokwe.iex.cloud.data.reference;

import java.util.Collections;
import java.util.List;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Tags extends Base implements Comparable<Tags> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Tags.class);

	public static final int    DATA_WEIGHT = 1; // 1 per call
	public static final String METHOD      = "/ref-data/tags";

/*
[{"name":"Electronic Technology"}, ... ,{"name":"Government"}]
 */
	public static List<Tags> getInstance(Context context) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(base);
		
		List<Tags> ret = getArray(context, url, Tags.class);
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

	public Tags() {
		name = null;
	}
	
	public Tags(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Tags that) {
		return this.name.compareTo(that.name);
	}
}
