package yokwe.iex.cloud.data.stock;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;
import yokwe.iex.cloud.IEXCloud.TimeZone;
import yokwe.iex.cloud.IEXCloud.UseTimeZone;

public class OHLC extends Base implements Comparable<OHLC> {	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(OHLC.class);

	public static final int    DATA_WEIGHT = 1; // 1 per return records
		
//	{
//	    "open": {
//	        "price": 32.72,
//	        "time": 1562333400753
//	    },
//	    "close": {
//	        "price": 33.01,
//	        "time": 1562356922682
//	    },
//	    "high": 33.15,
//	    "low": 32.62,
//	    "volume": 223015,
//	    "symbol": "TRTN"
//	}
	
	public static class PriceTime extends Base {
		public double price;
		@UseTimeZone(TimeZone.NEW_YORK)
		public LocalDateTime time;
		
		public PriceTime() {
			price = 0;
			time  = null;
		}
		
		public PriceTime(JsonObject jsonObject) {
			super(jsonObject);
		}
	}
	
	public PriceTime open;
	public PriceTime close;
	public double    high;
	public double    low;
	public long      volume;
	public String    symbol;
	
	public OHLC() {
		open   = null;
		close  = null;
		high   = 0;
		low    = 0;
		volume = 0;
		symbol = null;
	}
	
	public OHLC(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(OHLC that) {
		return this.symbol.compareTo(that.symbol);
	}
	
	public static final String METHOD      = "/stock/%s/ohlc";
	public static OHLC getInstance(Context context, String symbol) {
		String base = context.getBaseURL(String.format(METHOD, encodeString(symbol)));
		String url  = context.getURL(base, Format.JSON);

		OHLC ret = getObject(context, url, OHLC.class);
		
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
	
	public static final String METHOD_MARKET = "/stock/market/ohlc";
	public static List<OHLC> getInstance(Context context, String... symbols) {
		// Sanity check
		if (symbols.length == 0) {
			logger.error("symbols.length == 0");
			throw new UnexpectedException("symbols.length == 0");
		}
		
		String[] symbolArray = new String[symbols.length];
		for(int i = 0; i < symbols.length; i++) {
			symbolArray[i] = Base.encodeString(symbols[i]);
		}
		
		Map<String, String> paramMap = new TreeMap<>();
		paramMap.put("symbols", String.join(",", symbolArray));
		
		String base = context.getBaseURL(METHOD_MARKET);
		String url  = context.getURL(base, Format.JSON, paramMap);

		List<OHLC> ret = getArray(context, url, OHLC.class);
		
		// Check token usage
		{
			int actual = context.getTokenUsed();
			int expect = DATA_WEIGHT * ret.size();
			if (actual != expect) {
				logger.error("Unexpected token usage {}  {}", actual, expect);
				throw new UnexpectedException("Unexpected token usage");
			}
		}
		return ret;
	}
}