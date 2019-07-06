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
			Status status = Status.getObject(context, Status.class);
			logger.info("status = {}", status);
		}
		
		{
			Metadata metadata = Metadata.getObject(context, Metadata.class);
			logger.info("metadata = {}", metadata);
		}
		
		{
			Usage usage = Usage.getObject(context, Usage.class);
			logger.info("usage = {}", usage);
		}
		
//		{
//			List<yokwe.iex.cloud.data.reference.iex.Symbols> symbols = yokwe.iex.cloud.data.reference.iex.Symbols.getArray(context, yokwe.iex.cloud.data.reference.iex.Symbols.class);
//			logger.info("array symbols = {}", symbols.size());
//			
////			yokwe.iex.cloud.data.reference.iex.Symbols.saveCSV(symbols);
//		}
//		{
//			List<yokwe.iex.cloud.data.reference.iex.Symbols> iexSymbols = yokwe.iex.cloud.data.reference.iex.Symbols.getCSV(context, Symbols.class);
//			logger.info("iexSymbols = {}", iexSymbols.size());
//			
//			yokwe.iex.cloud.data.reference.iex.Symbols.saveCSV(iexSymbols);
//		}

//		{
//          // 100 per call
//			List<yokwe.iex.cloud.data.reference.Symbols> symbols = yokwe.iex.cloud.data.reference.Symbols.getCSV(context, yokwe.iex.cloud.data.reference.Symbols.class);
//			logger.info("symbols = {}", symbols.size());
//			
//			yokwe.iex.cloud.data.reference.Symbols.saveCSV(symbols);
//		}

//		{
//			// 100 per call
//			List<yokwe.iex.cloud.data.reference.otc.Symbols> otcSymbols = yokwe.iex.cloud.data.reference.otc.Symbols.getCSV(context, yokwe.iex.cloud.data.reference.otc.Symbols.class);
//			logger.info("otcSymbols = {}", otcSymbols.size());
//			
//			yokwe.iex.cloud.data.reference.otc.Symbols.saveCSV(otcSymbols);
//		}

//		{
//			// 1 per call
//			List<yokwe.iex.cloud.data.reference.Exchanges> exchanges = yokwe.iex.cloud.data.reference.Exchanges.getCSV(context, yokwe.iex.cloud.data.reference.Exchanges.class);
//			logger.info("exchanges = {}", exchanges.size());
//			
//			yokwe.iex.cloud.data.reference.Exchanges.saveCSV(exchanges);
//		}

//		{
//			// 1 per call
//			List<yokwe.iex.cloud.data.reference.market.us.Exchanges> marketUSExchanges = yokwe.iex.cloud.data.reference.market.us.Exchanges.getCSV(context, yokwe.iex.cloud.data.reference.market.us.Exchanges.class);
//			logger.info("marketUSExchanges = {}", marketUSExchanges.size());
//			
//			yokwe.iex.cloud.data.reference.market.us.Exchanges.saveCSV(marketUSExchanges);
//		}

//		{
//			// 100 per call
//			List<yokwe.iex.cloud.data.reference.mutualFunds.Symbols> mfSymbols = yokwe.iex.cloud.data.reference.mutualFunds.Symbols.getCSV(context, yokwe.iex.cloud.data.reference.mutualFunds.Symbols.class);
//			logger.info("mSymbols = {}", mfSymbols.size());
//			
//			yokwe.iex.cloud.data.reference.mutualFunds.Symbols.saveCSV(mfSymbols);
//		}

		
		
//		{
//			List<Tops> tops = Tops.getCSV(context, Tops.class);
//			logger.info("top = {}", tops.size());
//			
//			Tops.saveCSV(tops);
//		}

//		{
//			List<Tops.Last> topsLast = Tops.Last.getCSV(context, Tops.Last.class);
//			logger.info("topsLast = {}", topsLast.size());
//			
//			Tops.Last.saveCSV(topsLast);
//		}
		
		{
			String symbol = "IBKR";
			List<yokwe.iex.cloud.data.dataPoints.Key> keyList = yokwe.iex.cloud.data.dataPoints.Key.getKeyList(context, symbol);
			logger.info("keyList {} = {}", symbol, keyList.size());
			for(int i = 0; i < keyList.size(); i++) {
				yokwe.iex.cloud.data.dataPoints.Key key = keyList.get(i); 
				logger.info("{} {}", symbol, String.format("%3d  %-40s  %4d  %s", i, key.key, key.weight, key.description));
			}
		}
		
		logger.info("STOP");
	}
}
