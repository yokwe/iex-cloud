package yokwe.iex.cloud.data.stock;

import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.json.JsonObject;

import org.slf4j.LoggerFactory;

import yokwe.iex.UnexpectedException;
import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;
import yokwe.iex.cloud.IEXCloud.JSONName;

public class Company extends Base implements Comparable<Company> {	
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(Company.class);

	public static final int    DATA_WEIGHT = 1; // 2 per return records
	
//	{
//		  "symbol": "TRTN",
//		  "companyName": "Triton International Ltd.",
//		  "exchange": "New York Stock Exchange",
//		  "industry": "Finance/Rental/Leasing",
//		  "website": "http://www.trtn.com",
//		  "description": "Triton International Ltd. is engaged in the operation and management of fleet of intermodal marine dry, refrigerated, and cargo containers. It operates through the Equipment Leasing and Equipment Trading segments. The Equipment Leasing segment involves in owning, leasing, and disposing containers and chassis from lease fleet, as well as managing containers owned by third parties. The Equipment Trading segment focuses on the purchase containers from shipping line customers, and other sellers of containers, and resells containers to container retailers and users of containers for storage or one-way shipment. The company was founded on September 29, 2015 and is headquartered in Hamilton, Bermuda.",
//		  "CEO": "Brian Mead Sondey",
//		  "securityName": "Triton International Ltd. Class A",
//		  "issueType": "cs",
//		  "sector": "Finance",
//		  "employees": 243,
//		  "tags": [
//		    "Finance",
//		    "Finance/Rental/Leasing"
//		  ]
//		}

	public String   symbol;
	public String   companyName;
	public String   exchange;
	public String   industry;
	public String   website;
	public String   description;
	@JSONName("CEO")
	public String   ceo;
	public String   securityName;
	public String   issueType;
	public String   sector;
	public int      employees;
	public String[] tags;
	
	public Company() {
		symbol       = null;
		companyName  = null;
		exchange     = null;
		industry     = null;
		website      = null;
		description  = null;
		ceo          = null;
		securityName = null;
		issueType    = null;
		sector       = null;
		employees    = 0;
		tags         = null;
	}
	
	public Company(JsonObject jsonObject) {
		super(jsonObject);
	}

	@Override
	public int compareTo(Company that) {
		return this.symbol.compareTo(that.symbol);
	}
	
	public static final String METHOD      = "/stock/%s/company";
	public static Company getInstance(Context context, String symbol) {
		String base = context.getBaseURL(String.format(METHOD, encodeString(symbol)));
		String url  = context.getURL(base);

		Company ret = getObject(context, url, Company.class);
		
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
	
	public static final String METHOD_MARKET = "/stock/market/company";
	public static List<Company> getInstance(Context context, String... symbols) {
		// Sanity check
		if (symbols.length == 0) {
			logger.error("symbols.length == 0");
			throw new UnexpectedException("symbols.length == 0");
		}
		
		String[] symbolArray = new String[symbols.length];
		for(int i = 0; i < symbols.length; i++) {
			symbolArray[i] = Base.encodeString(symbols[i]);
		}
		
		Map<String, String> paramMap = new TreeMap<>();
		paramMap.put("symbols", String.join(",", symbolArray));
		
		String base = context.getBaseURL(METHOD_MARKET);
		String url  = context.getURL(base, Format.JSON, paramMap);

		List<Company> ret = getArray(context, url, Company.class);
		
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