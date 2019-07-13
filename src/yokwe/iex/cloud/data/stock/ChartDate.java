package yokwe.iex.cloud.data.stock;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class ChartDate extends Base implements Comparable<ChartDate> {	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(ChartDate.class);

	public static final int    DATA_WEIGHT = 2; // 2 per return records
	
//	[
//	    {
//	        "date": "2019-07-05",
//	        "uClose": 33.01,
//	        "uOpen": 32.72,
//	        "uHigh": 33.15,
//	        "uLow": 32.62,
//	        "uVolume": 223015,
//	        "close": 33.01,
//	        "open": 32.72,
//	        "high": 33.15,
//	        "low": 32.62,
//	        "volume": 223015,
//	        "change": 0,
//	        "changePercent": 0,
//	        "label": "Jul 5",
//	        "changeOverTime": 0
//	    }
//	]
	
//	date,uClose,uOpen,uHigh,uLow,uVolume,close,open,high,low,volume,change,changePercent,label,changeOverTime
//	2019-07-05,33.01,32.72,33.15,32.62,223015,33.01,32.72,33.15,32.62,223015,0,0,Jul 5,0
		
	public String date;
	
	public double uClose;
	public double uOpen;
	public double uHigh;
	public double uLow;
	public long   uVolume;
	//
	public double close;
	public double open;
	public double high;
	public double low;
	public long   volume;
	//
	public double change;
	public double changePercent;
	public String label;
	public double changeOverTime;
	
	public ChartDate() {
		date           = null;
		
		uClose         = 0;
		uOpen          = 0;
		uHigh          = 0;
		uLow           = 0;
		uVolume        = 0;
		
		close          = 0;
		open           = 0;
		high           = 0;
		low            = 0;
		volume         = 0;
		
		change         = 0;
		changePercent  = 0;
		label          = null;
		changeOverTime = 0;
	}
	
	public ChartDate(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(ChartDate that) {
		return this.date.compareTo(that.date);
	}
		
	
	public static List<ChartDate> getInstance(Context context, LocalDate date, String symbol) {
		int y = date.getYear();
		int m = date.getMonthValue();
		int d = date.getDayOfMonth();
		
		String dateString = String.format("%d%02d%02d", y, m, d);
		return getInstance(context, dateString, symbol);
	}
	private static Map<String, String> paramMap = new TreeMap<>();
	static {
		paramMap.put("chartByDay", "true");
	}
	// https://cloud.iexapis.com/v1/stock/TRTN/chart/date/20190705?chartByDay=true&token=XX&format=csv
	public static final String METHOD = "/stock/%s/chart/date/%s";
	public static List<ChartDate> getInstance(Context context, String date, String symbol) {
		String base = context.getBaseURL(String.format(METHOD, encodeString(symbol), date));
		String url  = context.getURL(base, Format.CSV, paramMap);

		List<ChartDate> ret = getCSV(context, url, ChartDate.class);
		
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
	
	// https://sandbox.iexapis.com/v1/stock/market/chart/date/20190705?chartByDay=true&symbols=ibm,trtn&token=XX&format=json
	public static final String METHOD_MARKET = "/stock/market/chart/date/%s";
	public static Map<String, List<ChartDate>> getInstance(Context context, LocalDate date, String... symbols) {
		int y = date.getYear();
		int m = date.getMonthValue();
		int d = date.getDayOfMonth();
		
		String dateString = String.format("%d%02d%02d", y, m, d);
		return getInstance(context, dateString, symbols);
	}
	public static Map<String, List<ChartDate>> getInstance(Context context, String date, String... symbols) {
		// Sanity check
		if (symbols.length == 0) {
			logger.error("symbols.length == 0");
			throw new UnexpectedException("symbols.length == 0");
		}
				
		Map<String, String> paramMap = new TreeMap<>();
		paramMap.put("symbols",    String.join(",", symbols));
		paramMap.put("chartByDay", "true");
		
		String base = context.getBaseURL(String.format(METHOD_MARKET, date));
		String url  = context.getURL(base, Format.JSON, paramMap);

		List<List<ChartDate>> result = getArrayArray(context, url, ChartDate.class);
		if (result.size() != symbols.length) {
			logger.error("result.size() != symbols.length  {}  {}", result.size(), symbols.length);
			throw new UnexpectedException("result.size() != symbols.length");
		}
		
		Map<String, List<ChartDate>> ret = new TreeMap<>();
		for(int i = 0; i < symbols.length; i++) {
			ret.put(symbols[i], result.get(i));
		}
		
		// Check token usage
		{
			int actual = context.getTokenUsed();
			int expect = DATA_WEIGHT * ret.values().stream().mapToInt(o -> o.size()).sum();
			if (actual != expect) {
				logger.error("Unexpected token usage {}  {}", actual, expect);
				throw new UnexpectedException("Unexpected token usage");
			}
		}
		return ret;
	}
}