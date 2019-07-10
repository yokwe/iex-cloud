package yokwe.iex.cloud.data.reference.otc;

import java.util.List;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Symbols extends Base implements Comparable<Symbols> {
	public static final int    DATA_WEIGHT = 100; // 100 per call
	public static final String METHOD      = "/ref-data/otc/symbols";

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
		return ret;
	}
}