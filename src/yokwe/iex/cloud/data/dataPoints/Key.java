package yokwe.iex.cloud.data.dataPoints;

import java.util.List;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Key extends Base implements Comparable<Key>{
	public static final int    DATA_WEIGHT = 0; // FREE
	public static final String METHOD      = "/data-points";

	// key,weight,description,lastUpdated
	public String key;
	public int    weight;
	public String description;
	public String lastUpdated;
	
	public Key() {
		key         = null;
		weight      = 0;
		description = null;
		lastUpdated = null;
	}
	
	public Key(JsonObject jsonObject) {
		super(jsonObject);
	}
	
	@Override
	public int compareTo(Key that) {
		return this.key.compareTo(that.key);
	}
	
	public static List<Key> getInstance(Context context, String symbol) {
		List<Key> ret = getCSV(context, Key.class, symbol);
		return ret;
	}

}
