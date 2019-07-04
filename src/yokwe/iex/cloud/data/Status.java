package yokwe.iex.cloud.data;

import java.time.OffsetDateTime;

import javax.json.JsonObject;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.IEXCloud.UseLocalTimeZone;

public class Status extends Base {
	public static final String METHOD = "/status";
	
	public String 		  status;
	public String 		  version;
	@UseLocalTimeZone
	public OffsetDateTime time;
	
	public Status() {
		status  = null;
		version = null;
		time    = null;
	}
	
	public Status(JsonObject jsonObject) {
		super(jsonObject);
	}
}
