package yokwe.iex.cloud.data.stock;

import java.util.List;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Chart extends Base implements Comparable<Chart> {	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Chart.class);

	public static final int    DATA_WEIGHT = 10; // 10 per return records
	public static final String METHOD      = "/stock/%s/chart/%s";
	
//  https://cloud.iexapis.com/v1/stock/TRTN/chart/5d&token=sk_bb977734bffe47ef8dca20cd4cfad878&format=csv

//	date,open,close,high,low,volume,uOpen,uClose,uHigh,uLow,uVolume,change,changePercent,label,changeOverTime
//	2019-06-28,32.38,32.76,33.11,32.28,1133049,32.38,32.76,33.11,32.28,1133049,0,0,Jun 28,0
//	2019-07-01,33.15,33.24,33.48,32.94,429509,33.15,33.24,33.48,32.94,429509,0.48,1.4652,Jul 1,0.014652
//	2019-07-02,33.28,32.78,33.28,32.47,263862,33.28,32.78,33.28,32.47,263862,-0.46,-1.3839,Jul 2,0.000611
//	2019-07-03,32.88,32.92,33.1,32.82,155552,32.88,32.92,33.1,32.82,155552,0.14,0.4271,Jul 3,0.004884
//	2019-07-05,32.72,33.01,33.15,32.62,223015,32.72,33.01,33.15,32.62,223015,0.09,0.2734,Jul 5,0.007631h
	
	public String date;
	
	public double open;
	public double close;
	public double high;
	public double low;
	public long   volume;
	//
	public double uOpen;
	public double uClose;
	public double uHigh;
	public double uLow;
	public long   uVolume;
	//
	public double change;
	public double changePercent;
	public String label;
	public double changeOverTime;
	
	public Chart() {
		date           = null;
		
		open           = 0;
		close          = 0;
		high           = 0;
		low            = 0;
		volume         = 0;
		
		uOpen          = 0;
		uClose         = 0;
		uHigh          = 0;
		uLow           = 0;
		uVolume        = 0;
		
		change         = 0;
		changePercent  = 0;
		label          = null;
		changeOverTime = 0;
	}
	
	public Chart(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Chart that) {
		return this.date.compareTo(that.date);
	}
		
	public static List<Chart> getInstance(Context context, String symbol, Range range) {
		String base = context.getBaseURL(String.format(METHOD, encodeString(symbol), range.value));
		String url  = context.getURL(base, Format.CSV);

		List<Chart> ret = getCSV(context, url, Chart.class);
		
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