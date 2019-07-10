package yokwe.iex.cloud.data.reference;

import java.util.List;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Exchanges extends Base implements Comparable<Exchanges> {
	public static final int    DATA_WEIGHT = 1; // 1 per call
	public static final String METHOD      = "/ref-data/exchanges";
	public static final String PATH        = "/exchanges.csv";

	// exchange,region,description,mic,exchangeSuffix
	public String exchange;
	public String region;
	public String description;
	public String mic;
	public String exchangeSuffix;
	
	public Exchanges() {
		exchange       = null;
		region         = null;
		description    = null;
		mic            = null;
		exchangeSuffix = null;
	}
	
	public Exchanges(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Exchanges that) {
		int ret = this.region.compareTo(that.region);
		if (ret == 0) ret = this.exchange.compareTo(that.exchange);
		return ret;
	}
	
	public static List<Exchanges> getInstance(Context context) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(base, Format.CSV);
		
		List<Exchanges> ret = getCSV(context, url, Exchanges.class);
		return ret;
	}
}