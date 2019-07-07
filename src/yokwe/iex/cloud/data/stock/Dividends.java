package yokwe.iex.cloud.data.stock;

import java.util.List;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Dividends extends Base implements Comparable<Dividends> {	
	public static final int    DATA_WEIGHT = 10; // 10 per return records
	public static final String METHOD      = "/stock/%s/dividends/%s";
	
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
	
	public static List<Dividends> getInstance(Context context, String symbol, Range range) {
		String base = context.getBaseURL(String.format(METHOD, encodeString(symbol), range.value));
		String url  = context.getURL(base, Format.CSV);

		List<Dividends> ret = getCSV(url, Dividends.class);
		return ret;
	}
}