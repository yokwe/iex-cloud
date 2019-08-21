package yokwe.iex.cloud.data.timeSeries;

import java.util.List;
import java.util.Map;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;

public class Key extends Base {
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Key.class);

	public static final int    DATA_WEIGHT = 0; // FREE
	public static final String METHOD      = "/time-series";

//	[
//	  {
//	    "id": "REPORTED_FINANCIALS",
//	    "description": "Reported financials",
//	    "schema": {
//	      "type": "object",
//	      "properties": {
//	        "formFiscalYear": {
//	          "type": "number"
//	        },
//	        "formFiscalQuarter": {
//	          "type": "number"
//	        },
//	        "version": {
//	          "type": "string"
//	        },
//	        "periodStart": {
//	          "type": "string"
//	        },
//	        "periodEnd": {
//	          "type": "string"
//	        },
//	        "dateFiled": {
//	          "type": "string"
//	        },
//	        "reportLink": {
//	          "type": "string"
//	        },
//	        "adsh": {
//	          "type": "string"
//	        },
//	        "stat": {
//	          "type": "object"
//	        }
//	      }
//	    },
//	    "weight": 5000,
//	    "created": "2019-06-04 21:32:20",
//	    "lastUpdated": "2019-06-04 21:32:20"
//	  }
//	]
	
	public static class Property extends Base {
		public String type;
		
		public Property() {
			type = null;
		}
		
		public Property(JsonObject jsonObject) {
			super(jsonObject);
		}
	}
	
	public static class Schema extends Base {
		public String                type;
		public Map<String, Property> properties;
		
		public Schema() {
			type                 = null;
			properties           = null;
		}
		
		public Schema(JsonObject jsonObject) {
			super(jsonObject);
		}
	}

	public String id;
	public String description;
	public Schema schema;
	public int    weight;
	public String created;
	public String lastUpdated;
	
	public Key() {
		id          = null;
		description = null;
		schema      = null;
		weight      = 0;
		created     = null;
		lastUpdated = null;
	}
	
	public Key(JsonObject jsonObject) {
		super(jsonObject);
	}
	
	public static List<Key> getInstance(Context context) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(base);

		List<Key> ret = getArray(context, url, Key.class);
		
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
}
