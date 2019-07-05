package yokwe.iex.cloud.data.reference.market.us;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;

public class Exchanges extends Base implements Comparable<Exchanges> {
	public static final int    DATA_WEIGHT = 1; // 1 per call
	public static final String METHOD      = "/ref-data/market/us/exchanges";
	public static final String PATH        = "/market-us-exchanges.csv";

	// mic,name,longName,tapeId,oatsId,type
	public String mic;
	public String name;
	public String longName;
	public String tapeId;
	public String oatsId;
	public String refId;
	public String type;
	
	public Exchanges() {
		mic      = null;
		name     = null;
		longName = null;
		tapeId   = null;
		oatsId   = null;
		refId    = null;
		type     = null;
	}
	
	public Exchanges(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Exchanges that) {
		return this.mic.compareTo(that.mic);
	}
}