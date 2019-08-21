package yokwe.iex.cloud.data.reference.market.us;

import java.util.List;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Exchanges extends Base implements Comparable<Exchanges> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Exchanges.class);

	public static final int    DATA_WEIGHT = 1; // 1 per call
	public static final String METHOD      = "/ref-data/market/us/exchanges";

	// mic,name,longName,tapeId,oatsId,type
	public String mic;
	public String name;
	public String longName;
	public String tapeId;
	public String oatsId;
	public String refId;
	public String type;
	
	public Exchanges() {
		mic      = null;
		name     = null;
		longName = null;
		tapeId   = null;
		oatsId   = null;
		refId    = null;
		type     = null;
	}
	
	public Exchanges(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Exchanges that) {
		return this.mic.compareTo(that.mic);
	}
	
	public static List<Exchanges> getInstance(Context context) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(base, Format.CSV);
		
		List<Exchanges> ret = getCSV(context, url, Exchanges.class);
		
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