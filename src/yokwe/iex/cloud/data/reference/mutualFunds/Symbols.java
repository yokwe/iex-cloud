package yokwe.iex.cloud.data.reference.mutualFunds;

import java.util.List;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Symbols extends Base implements Comparable<Symbols> {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Symbols.class);

	public static final int    DATA_WEIGHT = 100; // 100 per call
	public static final String METHOD      = "/ref-data/mutual-funds/symbols";

	// symbol,exchange,name,date,type,iexId,region,currency,isEnabled
	public String  symbol;
	public String  exchange;
	public String  name;
	public String  date;
	public String  type;
	public String  iexId;
	public String  region;
	public String  currency;
	public boolean isEnabled;
	
	public Symbols() {
		symbol    = null;
		exchange  = null;
		name      = null;
		date      = null;
		type      = null;
		iexId     = null;
		region    = null;
		currency  = null;
		isEnabled = false;
	}
	
	public Symbols(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Symbols that) {
		return this.symbol.compareTo(that.symbol);
	}
	
	public static List<Symbols> getInstance(Context context) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(base, Format.CSV);
		
		List<Symbols> ret = getCSV(context, url, Symbols.class);
		
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