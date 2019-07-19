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

public class Quote extends Base implements Comparable<Quote> {	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Quote.class);

	public static final int    DATA_WEIGHT = 1; // 1 per return records
	
//	  {
//		    "symbol": "IBM",
//		    "companyName": "International Business Machines Corp.",
//		    "primaryExchange": "New York Stock Exchange",
//		    "calculationPrice": "close",
//		    "open": 142.5,
//		    "openTime": 1563456600588,
//		    "close": 149.63,
//		    "closeTime": 1563480110809,
//		    "high": 150.31,
//		    "low": 142.2,
//		    "latestPrice": 149.63,
//		    "latestSource": "Close",
//		    "latestTime": "July 18, 2019",
//		    "latestUpdate": 1563480110809,
//		    "latestVolume": 12634038,
//		    "iexRealtimePrice": 149.64,
//		    "iexRealtimeSize": 100,
//		    "iexLastUpdated": 1563479998261,
//		    "delayedPrice": 149.63,
//		    "delayedPriceTime": 1563480110830,
//		    "extendedPrice": 149.9,
//		    "extendedChange": 0.27,
//		    "extendedChangePercent": 0.0018,
//		    "extendedPriceTime": 1563523614300,
//		    "previousClose": 143.07,
//		    "previousVolume": 6064265,
//		    "change": 6.56,
//		    "changePercent": 0.04585,
//		    "volume": 0,
//		    "iexMarketPercent": 0.04551363546634892,
//		    "iexVolume": 575021,
//		    "avgTotalVolume": 3255347,
//		    "iexBidPrice": 0,
//		    "iexBidSize": 0,
//		    "iexAskPrice": 0,
//		    "iexAskSize": 0,
//		    "marketCap": 132668392090,
//		    "peRatio": 15.39,
//		    "week52High": 154.36,
//		    "week52Low": 105.94,
//		    "ytdChange": 0.287669,
//		    "lastTradeTime": 1563480110830
//		  },

	    public String        symbol;
	    public String        companyName;
	    public String        primaryExchange;
	    public String        calculationPrice;
	    public double        open;
		@UseTimeZone(TimeZone.NEW_YORK)
		public LocalDateTime openTime;
	    public double close;
		@UseTimeZone(TimeZone.NEW_YORK)
	    public LocalDateTime closeTime;
	    public double        high;
	    public double        low;
	    public double        latestPrice;
	    public String        latestSource;
	    public String        latestTime;
		@UseTimeZone(TimeZone.NEW_YORK)
	    public LocalDateTime latestUpdate;
	    public long          latestVolume;
	    public double        iexRealtimePrice;
	    public long          iexRealtimeSize;
		@UseTimeZone(TimeZone.NEW_YORK)
	    public LocalDateTime iexLastUpdated;
	    public double        delayedPrice;
		@UseTimeZone(TimeZone.NEW_YORK)
	    public LocalDateTime delayedPriceTime;
	    public double        extendedPrice;
	    public double        extendedChange;
	    public double        extendedChangePercent;
		@UseTimeZone(TimeZone.NEW_YORK)
	    public LocalDateTime extendedPriceTime;
	    public double        previousClose;
	    public long          previousVolume;
	    public double        change;
	    public double        changePercent;
	    public long          volume;
	    public double        iexMarketPercent;
	    public long          iexVolume;
	    public long          avgTotalVolume;
	    public double        iexBid;
	    public long          iexBidSize;
	    public double        iexAsk;
	    public long          iexAskSize;
	    public long          marketCap;
	    public double        peRatio;
	    public double        week52High;
	    public double        week52Low;
	    public double        ytdChange;
		@UseTimeZone(TimeZone.NEW_YORK)
	    public LocalDateTime lastTradeTime;

	
	public Quote() {
	    symbol                = null;
	    companyName           = null;
	    primaryExchange       = null;
	    calculationPrice      = null;
	    open                  = 0;
		openTime              = null;
	    close                 = 0;
	    closeTime             = null;
	    high                  = 0;
	    low                   = 0;
	    latestPrice           = 0;
	    latestSource          = null;
	    latestTime            = null;
	    latestUpdate          = null;
	    latestVolume          = 0;
	    iexRealtimePrice      = 0;
	    iexRealtimeSize       = 0;
	    iexLastUpdated        = null;
	    delayedPrice          = 0;
	    delayedPriceTime      = null;
	    extendedPrice         = 0;
	    extendedChange        = 0;
	    extendedChangePercent = 0;
	    extendedPriceTime     = null;
	    previousClose         = 0;
	    previousVolume        = 0;
	    change                = 0;
	    changePercent         = 0;
	    volume                = 0;
	    iexMarketPercent      = 0;
	    iexVolume             = 0;
	    avgTotalVolume        = 0;
	    iexBid                = 0;
	    iexBidSize            = 0;
	    iexAsk                = 0;
	    iexAskSize            = 0;
	    marketCap             = 0;
	    peRatio               = 0;
	    week52High            = 0;
	    week52Low             = 0;
	    ytdChange             = 0;
	    lastTradeTime         = null;
	}
	
	public Quote(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Quote that) {
		return this.symbol.compareTo(that.symbol);
	}
	
	public static final String METHOD      = "/stock/%s/quote";
	public static Quote getInstance(Context context, String symbol) {
		String base = context.getBaseURL(String.format(METHOD, encodeString(symbol)));
		String url  = context.getURL(base);

		Quote ret = getObject(context, url, Quote.class);
		
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
	
	public static final String METHOD_MARKET = "/stock/market/quote";
	public static List<Quote> getInstance(Context context, String... symbols) {
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

		List<Quote> ret = getArray(context, url, Quote.class);
		
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