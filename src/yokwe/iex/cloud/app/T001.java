package yokwe.iex.cloud.app;

import java.util.List;

import org.slf4j.LoggerFactory;

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
//			List<yokwe.iex.cloud.data.reference.iex.Symbols> iexSymbols = yokwe.iex.cloud.data.reference.iex.Symbols.getInstance(context);
//			logger.info("iex csv symbols = {}", iexSymbols.size());
//			
//			Base.saveCSV(iexSymbols);
//		}

//		{
//          // 100 per call
//			List<yokwe.iex.cloud.data.reference.Symbols> symbols = yokwe.iex.cloud.data.reference.Symbols.getInstance(context);
//			logger.info("symbols = {}", symbols.size());
//			
//			Base.saveCSV(symbols);
//		}

//		{
//			// 100 per call
//			List<yokwe.iex.cloud.data.reference.otc.Symbols> otcSymbols = yokwe.iex.cloud.data.reference.otc.Symbols.getInstance(context);
//			logger.info("otcSymbols = {}", otcSymbols.size());
//			
//			Base.saveCSV(otcSymbols);
//		}

//		{
//			// 1 per call
//			List<yokwe.iex.cloud.data.reference.Exchanges> exchanges = yokwe.iex.cloud.data.reference.Exchanges.getInstance(context);
//			logger.info("exchanges = {}", exchanges.size());
//			
//			Base.saveCSV(exchanges);
//		}

//		{
//			// 1 per call
//			List<yokwe.iex.cloud.data.reference.market.us.Exchanges> marketUSExchanges = yokwe.iex.cloud.data.reference.market.us.Exchanges.getInstance(context);
//			logger.info("marketUSExchanges = {}", marketUSExchanges.size());
//			
//			Base.saveCSV(marketUSExchanges);
//		}

//		{
//			// 100 per call
//			List<yokwe.iex.cloud.data.reference.mutualFunds.Symbols> mfSymbols = yokwe.iex.cloud.data.reference.mutualFunds.Symbols.getInstance(context);
//			logger.info("mfSymbols = {}", mfSymbols.size());
//			
//			Base.saveCSV(mfSymbols);
//		}

				
		{
			String symbol = "IBKR";
			List<yokwe.iex.cloud.data.dataPoints.Key> keyList = yokwe.iex.cloud.data.dataPoints.Key.getInstance(context, symbol);
			logger.info("keyList {} = {}", symbol, keyList.size());
//			for(int i = 0; i < keyList.size(); i++) {
//				yokwe.iex.cloud.data.dataPoints.Key key = keyList.get(i); 
//				logger.info("{} {}", symbol, String.format("%3d  %-40s  %4d  %s", i, key.key, key.weight, key.description));
//			}
		}
		
		{
			List<yokwe.iex.cloud.data.timeSeries.Key> keyList = yokwe.iex.cloud.data.timeSeries.Key.getInstance(context);
			logger.info("keyList = {}", keyList.size());
//			for(int i = 0; i < keyList.size(); i++) {
//				yokwe.iex.cloud.data.timeSeries.Key key = keyList.get(i); 
//				logger.info("{}", key);
//			}
		}

//		{
//		// 10 per return record
//			List<yokwe.iex.cloud.data.stock.Dividends> dividendList = yokwe.iex.cloud.data.stock.Dividends.getInstance(context, "trtn", Range.Y1);
//			logger.info("dividendList = {}", dividendList.size());
//			for(int i = 0; i < dividendList.size(); i++) {
//				yokwe.iex.cloud.data.stock.Dividends dividend = dividendList.get(i); 
//				logger.info("{}", dividend);
//			}
//		}

//		{
//		// 2 per call
//			yokwe.iex.cloud.data.stock.Previous previous = yokwe.iex.cloud.data.stock.Previous.getInstance(context, "trtn");
//			logger.info("previous = {}", previous);
//		}

//		{
//		// 2 per call
//			yokwe.iex.cloud.data.stock.ChartDate chartDate = yokwe.iex.cloud.data.stock.ChartDate.getInstance(context, "trtn", "20190705");
//			logger.info("chartDate = {}", chartDate);
//		}
		
//		{
//		// 10 per returned record
//			List<yokwe.iex.cloud.data.stock.Chart> chartList = yokwe.iex.cloud.data.stock.Chart.getInstance(context, "trtn", Range.D5);
//			logger.info("chartList = {}", chartList.size());
//			for(int i = 0; i < chartList.size(); i++) {
//				yokwe.iex.cloud.data.stock.Chart chart = chartList.get(i); 
//				logger.info("{}", chart);
//			}
//		}

		logger.info("STOP");
	}
}
