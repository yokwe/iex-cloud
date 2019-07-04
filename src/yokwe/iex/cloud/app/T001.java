package yokwe.iex.cloud.app;

import java.util.List;

import org.slf4j.LoggerFactory;

import yokwe.iex.cloud.Context;
import yokwe.iex.cloud.Type;
import yokwe.iex.cloud.Version;
import yokwe.iex.cloud.data.Account;
import yokwe.iex.cloud.data.RefData;
import yokwe.iex.cloud.data.Status;

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
			Account.Metadata metadata = Account.Metadata.getObject(context, Account.Metadata.class);
			logger.info("metadata = {}", metadata);
		}
		
		{
			Account.Usage usage = Account.Usage.getObject(context, Account.Usage.class);
			logger.info("usage = {}", usage);
		}
		
//		{
//			List<RefData.IEX.Symbols> symbols = RefData.IEX.Symbols.getArray(context, RefData.IEX.Symbols.class);
//			logger.info("array symbols = {}", symbols.size());
//			
//			RefData.IEX.Symbols.saveCSV(symbols);
//		}
		{
			List<RefData.IEX.Symbols> iexSymbols = RefData.IEX.Symbols.getCSV(context, RefData.IEX.Symbols.class);
			logger.info("iexSymbols = {}", iexSymbols.size());
			
			RefData.IEX.Symbols.saveCSV(iexSymbols);
		}

//		{
//			List<RefData.Symbols> symbols = RefData.Symbols.getCSV(context, RefData.Symbols.class);
//			logger.info("symbols = {}", symbols.size());
//			
//			RefData.Symbols.saveCSV(symbols);
//		}

//		{
//			List<RefData.OTC.Symbols> otcSymbols = RefData.OTC.Symbols.getCSV(context, RefData.OTC.Symbols.class);
//			logger.info("otcSymbols = {}", otcSymbols.size());
//			
//			RefData.OTC.Symbols.saveCSV(otcSymbols);
//		}

//		{
//			List<RefData.Exchanges> exchanges = RefData.Exchanges.getCSV(context, RefData.Exchanges.class);
//			logger.info("exchanges = {}", exchanges.size());
//			
//			RefData.Exchanges.saveCSV(exchanges);
//		}

//		{
//			List<RefData.Market.US.Exchanges> usExchanges = RefData.Market.US.Exchanges.getCSV(context, RefData.Market.US.Exchanges.class);
//			logger.info("usExchanges = {}", usExchanges.size());
//			
//			RefData.Market.US.Exchanges.saveCSV(usExchanges);
//		}

//		{
//			List<RefData.MutualFunds.Symbols> mfSymbols = RefData.MutualFunds.Symbols.getCSV(context, RefData.MutualFunds.Symbols.class);
//			logger.info("mfSymbols = {}", mfSymbols.size());
//			
//			RefData.MutualFunds.Symbols.saveCSV(mfSymbols);
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
		
		logger.info("STOP");
	}
}
