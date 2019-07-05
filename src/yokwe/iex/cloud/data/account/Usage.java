package yokwe.iex.cloud.data.account;

import java.util.Map;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;

public class Usage extends Base {
	public static final int    DATA_WEIGHT = 0; // FREE
	public static final String METHOD      = "/account/usage";
	
	public static class Messages extends Base {
		public Map<String, Long> dailyUsage;
		public long              monthlyUsage;
		public long              monthlyPayAsYouGo;
		public Map<String, Long> tokenUsage;
		public Map<String, Long> keyUsage;
		
		public Messages() {
			dailyUsage        = null;
			monthlyUsage      = 0;
			monthlyPayAsYouGo = 0;
			keyUsage          = null;
		}
		
		public Messages(JsonObject jsonObject) {
			super(jsonObject);
		}
	}
	
	// {"messages":{"dailyUsage":{"20190616":"0","20190617":"0"},"monthlyUsage":0,"monthlyPayAsYouGo":0,"tokenUsage":{},"keyUsage":{"ACCOUNT_USAGE":"0","IEX_STATS":"0","REF_DATA_IEX_SYMBOLS":"0","IEX_TOPS":"0","IEX_DEEP":"0"}},"rules":[]}
	public Usage.Messages messages;
//		public Rules    rules;
	
	public Usage() {
		messages = null;
	}
	public Usage(JsonObject jsonObject) {
		super(jsonObject);
	}
}