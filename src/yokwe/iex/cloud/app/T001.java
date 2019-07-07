package yokwe.iex.cloud.app;

import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.iex.cloud.Base;
import yokwe.iex.cloud.Context;
import yokwe.iex.cloud.Type;
import yokwe.iex.cloud.Version;
import yokwe.iex.cloud.data.Status;
import yokwe.iex.cloud.data.account.Metadata;
import yokwe.iex.cloud.data.account.Usage;

public class T001 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T001.class);

	public static void main(String[] args) {
		logger.info("START");
		
		Context context = new Context(Type.PRODUCTION, Version.V1);
		logger.info("context = {}", context);
		
		{
			Status status = Status.getInstance(context);
			logger.info("status = {}", status);
		}
		
		{
			Metadata metadata = Metadata.getInstance(context);
			logger.info("metadata = {}", metadata);
		}
		
		{
			Usage usage = Usage.getInstance(context);
			logger.info("usage = {}", usage);
		}
		
//		{
//			List<yokwe.iex.cloud.data.reference.iex.Symbols> symbols = Base.getArray(context, yokwe.iex.cloud.data.reference.iex.Symbols.class);
//			logger.info("iex arr symbols = {}", symbols.size());
//			
////			Base.saveCSV(symbols);
//		}
		{
			List<yokwe.iex.cloud.data.reference.iex.Symbols> iexSymbols = yokwe.iex.cloud.data.reference.iex.Symbols.getInstance(context);
			logger.info("iex csv symbols = {}", iexSymbols.size());
			
			Base.saveCSV(iexSymbols);
		}

//		{
//          // 100 per call
//			List<yokwe.iex.cloud.data.reference.Symbols> symbols = Base.getCSV(context, yokwe.iex.cloud.data.reference.Symbols.class);
//			logger.info("symbols = {}", symbols.size());
//			
//			Base.saveCSV(symbols);
//		}

//		{
//			// 100 per call
//			List<yokwe.iex.cloud.data.reference.otc.Symbols> otcSymbols = Base.getCSV(context, yokwe.iex.cloud.data.reference.otc.Symbols.class);
//			logger.info("otcSymbols = {}", otcSymbols.size());
//			
//			Base.saveCSV(otcSymbols);
//		}

//		{
//			// 1 per call
//			List<yokwe.iex.cloud.data.reference.Exchanges> exchanges = Base.getCSV(context, yokwe.iex.cloud.data.reference.Exchanges.class);
//			logger.info("exchanges = {}", exchanges.size());
//			
//			Base.saveCSV(exchanges);
//		}

//		{
//			// 1 per call
//			List<yokwe.iex.cloud.data.reference.market.us.Exchanges> marketUSExchanges = Base.getCSV(context, yokwe.iex.cloud.data.reference.market.us.Exchanges.class);
//			logger.info("marketUSExchanges = {}", marketUSExchanges.size());
//			
//			Base.saveCSV(marketUSExchanges);
//		}

//		{
//			// 100 per call
//			List<yokwe.iex.cloud.data.reference.mutualFunds.Symbols> mfSymbols = Base.getCSV(context, yokwe.iex.cloud.data.reference.mutualFunds.Symbols.class);
//			logger.info("mSymbols = {}", mfSymbols.size());
//			
//			Base.saveCSV(mfSymbols);
//		}

				
		{
			String symbol = "IBKR";
			List<yokwe.iex.cloud.data.dataPoints.Key> keyList = yokwe.iex.cloud.data.dataPoints.Key.getInstance(context, symbol);
			logger.info("keyList {} = {}", symbol, keyList.size());
			for(int i = 0; i < keyList.size(); i++) {
				yokwe.iex.cloud.data.dataPoints.Key key = keyList.get(i); 
				logger.info("{} {}", symbol, String.format("%3d  %-40s  %4d  %s", i, key.key, key.weight, key.description));
			}
		}
		
		{
			List<yokwe.iex.cloud.data.timeSeries.Key> keyList = yokwe.iex.cloud.data.timeSeries.Key.getInstance(context);
			logger.info("keyList = {}", keyList.size());
			for(int i = 0; i < keyList.size(); i++) {
				yokwe.iex.cloud.data.timeSeries.Key key = keyList.get(i); 
				logger.info("{}", key);
			}
		}
		
		logger.info("STOP");
	}
}
