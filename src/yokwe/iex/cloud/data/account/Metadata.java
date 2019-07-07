package yokwe.iex.cloud.data.account;

import java.time.LocalDateTime;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;
import yokwe.iex.cloud.IEXCloud.TimeZone;
import yokwe.iex.cloud.IEXCloud.UseTimeZone;

public class Metadata extends Base {
	public static final int    DATA_WEIGHT = 0; // FREE
	public static final String METHOD      = "/account/metadata";

	// {"payAsYouGoEnabled":false,"effectiveDate":1551225868000,"subscriptionTermType":"annual","tierName":"start","messageLimit":500000,"messagesUsed":0,"circuitBreaker":null}
	public boolean        payAsYouGoEnabled;
	@UseTimeZone(TimeZone.LOCAL)
	public LocalDateTime  effectiveDate;
	public String         subscriptionTermType;
	public String         tierName;
	public long           messageLimit;
	public long           messagesUsed;
	public long           circuitBreaker;
	
	public Metadata() {
		payAsYouGoEnabled    = false;
		effectiveDate        = null;
		subscriptionTermType = null;
		tierName             = null;
		messageLimit         = 0;
		messagesUsed         = 0;
		circuitBreaker       = 0;
	}

	public Metadata(JsonObject jsonObject) {
		super(jsonObject);
	}

	public static Metadata getInstance(Context context) {
		String base = context.getBaseURL(METHOD);
		String url  = context.getURL(base);
		
		Metadata ret = getObject(url, Metadata.class);
		return ret;
	}
}