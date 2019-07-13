package yokwe.iex.cloud.data.stock;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Dividends extends Base implements Comparable<Dividends> {	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Dividends.class);
	
	public static final int    DATA_WEIGHT = 10; // 10 per return records
	
//	[
//	    {
//	        "exDate": "2018-11-30",
//	        "paymentDate": "2018-12-20",
//	        "recordDate": "2018-12-03",
//	        "declaredDate": "2018-11-02",
//	        "amount": 0.52,
//	        "flag": "No Change QoQ",
//	        "currency": "USD",
//	        "description": "Triton International Declares Quarterly Dividend of $0.52 Per Share",
//	        "frequency": "Quarterly",
//	        "date": "2019-07-07"
//	    },
//	    {
//	        "exDate": "2018-08-31",
//	        "paymentDate": "2018-09-25",
//	        "recordDate": "2018-09-04",
//	        "declaredDate": "2018-08-03",
//	        "amount": 0.52,
//	        "flag": "No Change QoQ",
//	        "currency": "USD",
//	        "description": "Triton International Declares Quarterly Dividend of $0.52 Per Share",
//	        "frequency": "Quarterly",
//	        "date": "2019-07-07"
//	    }
//	]
	
//	exDate,paymentDate,recordDate,declaredDate,amount,flag,currency,description,frequency,date
//	2018-11-30,2018-12-20,2018-12-03,2018-11-02,0.52,No Change QoQ,USD,Triton International Declares Quarterly Dividend of $0.52 Per Share,Quarterly,2019-07-07
//	2018-08-31,2018-09-25,2018-09-04,2018-08-03,0.52,No Change QoQ,USD,Triton International Declares Quarterly Dividend of $0.52 Per Share,Quarterly,2019-07-07
	
	public String exDate;
	public String paymentDate;
	public String recordDate;
	public String declaredDate;
	public double amount;
	public String flag;
	public String currency;
	public String description;
	public String frequency;
	public String date;
	
	public Dividends() {
		exDate       = null;
		paymentDate  = null;
		recordDate   = null;
		declaredDate = null;
		amount       = 0;
		flag         = null;
		currency     = null;
		description  = null;
		frequency    = null;
		date         = null;
	}
	
	public Dividends(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Dividends that) {
		return this.exDate.compareTo(that.exDate);
	}
	
	public static final String METHOD = "/stock/%s/dividends/%s";
	public static List<Dividends> getInstance(Context context, Range range, String symbol) {
		String base = context.getBaseURL(String.format(METHOD, encodeString(symbol), range.value));
		String url  = context.getURL(base, Format.CSV);

		List<Dividends> ret = getCSV(context, url, Dividends.class);
		
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
	
	public static final String METHOD_MARKET = "/stock/market/dividends";
	public static Map<String, List<Dividends>> getInstance(Context context, Range range, String... symbols) {
		// Sanity check
		if (symbols.length == 0) {
			logger.error("symbols.length == 0");
			throw new UnexpectedException("symbols.length == 0");
		}
				
		Map<String, String> paramMap = new TreeMap<>();
		paramMap.put("symbols", String.join(",", symbols));
		paramMap.put("range",   range.value);
		
		String base = context.getBaseURL(METHOD_MARKET);
		String url  = context.getURL(base, Format.JSON, paramMap);

		List<List<Dividends>> result = getArrayArray(context, url, Dividends.class);
		if (result.size() != symbols.length) {
			logger.error("result.size() != symbols.length  {}  {}", result.size(), symbols.length);
			throw new UnexpectedException("result.size() != symbols.length");
		}
		
		Map<String, List<Dividends>> ret = new TreeMap<>();
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