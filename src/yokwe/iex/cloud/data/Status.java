package yokwe.iex.cloud.data;

import java.time.LocalDateTime;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.IEXCloud.TimeZone;
import yokwe.iex.cloud.IEXCloud.UseTimeZone;

public class Status extends Base {
	public static final int    DATA_WEIGHT = 0; // FREE
	public static final String METHOD      = "/status";
	
	public String 		 status;
	public String 		 version;
	@UseTimeZone(TimeZone.LOCAL)
	public LocalDateTime time;
	
	public Status() {
		status  = null;
		version = null;
		time    = null;
	}
	
	public Status(JsonObject jsonObject) {
		super(jsonObject);
	}
}
