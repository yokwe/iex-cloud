package yokwe.iex.cloud.data.reference.iex;

import java.util.List;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Symbols extends Base implements Comparable<Symbols> {
	public static final int    DATA_WEIGHT = 0; // FREE
	public static final String METHOD      = "/ref-data/iex/symbols";
	public static final String PATH        = "/iex-symbols.csv";

	// {"symbol":"A","date":"2019-06-17","isEnabled":true}
	public String  symbol;
	public String  date;
	public boolean isEnabled;
	
	public Symbols() {
		symbol    = null;
		date      = null;
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
		
		List<Symbols> ret  = getCSV(url, Symbols.class);
		return ret;
	}
}