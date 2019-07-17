package yokwe.iex.cloud.app;

import java.util.List;
import java.util.Map;

import org.slf4j.LoggerFactory;

import yokwe.iex.cloud.Base.Range;
import yokwe.iex.cloud.Context;
import yokwe.iex.cloud.Type;
import yokwe.iex.cloud.Version;
import yokwe.iex.cloud.data.Status;
import yokwe.iex.cloud.data.account.Metadata;
import yokwe.iex.cloud.data.account.Usage;
import yokwe.iex.util.HttpUtil;

public class T001 {
	static final org.slf4j.Logger logger = LoggerFactory.getLogger(T001.class);

	public static void main(String[] args) {
		logger.info("START");
		
		HttpUtil.enableTrace(true);
		
		Context context = new Context(Type.SANDBOX, Version.V1);
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
		
		{
			List<yokwe.iex.cloud.data.reference.iex.Symbols> iexSymbols = yokwe.iex.cloud.data.reference.iex.Symbols.getInstance(context);
			logger.info("iex csv symbols = {}", iexSymbols.size());
		}

		{
          // 100 per call
			List<yokwe.iex.cloud.data.reference.Symbols> symbols = yokwe.iex.cloud.data.reference.Symbols.getInstance(context);
			logger.info("symbols = {}", symbols.size());
		}

		{
			// 100 per call
			List<yokwe.iex.cloud.data.reference.otc.Symbols> otcSymbols = yokwe.iex.cloud.data.reference.otc.Symbols.getInstance(context);
			logger.info("otcSymbols = {}", otcSymbols.size());
		}

		{
			// 1 per call
			List<yokwe.iex.cloud.data.reference.Exchanges> exchanges = yokwe.iex.cloud.data.reference.Exchanges.getInstance(context);
			logger.info("exchanges = {}", exchanges.size());
		}

		{
			// 1 per call
			List<yokwe.iex.cloud.data.reference.market.us.Exchanges> marketUSExchanges = yokwe.iex.cloud.data.reference.market.us.Exchanges.getInstance(context);
			logger.info("marketUSExchanges = {}", marketUSExchanges.size());
		}

		{
			// 100 per call
			List<yokwe.iex.cloud.data.reference.mutualFunds.Symbols> mfSymbols = yokwe.iex.cloud.data.reference.mutualFunds.Symbols.getInstance(context);
			logger.info("mfSymbols = {}", mfSymbols.size());
		}

				
		{
			String symbol = "IBKR";
			List<yokwe.iex.cloud.data.dataPoints.Key> keyList = yokwe.iex.cloud.data.dataPoints.Key.getInstance(context, symbol);
			logger.info("keyList {} = {}", symbol, keyList.size());
			for(int i = 0; i < keyList.size(); i++) {
//				yokwe.iex.cloud.data.dataPoints.Key key = keyList.get(i); 
//				logger.info("{} {}", symbol, String.format("%3d  %-40s  %4d  %s", i, key.key, key.weight, key.description));
			}
		}
		
		{
			List<yokwe.iex.cloud.data.timeSeries.Key> keyList = yokwe.iex.cloud.data.timeSeries.Key.getInstance(context);
			logger.info("keyList = {}", keyList.size());
			for(int i = 0; i < keyList.size(); i++) {
//				yokwe.iex.cloud.data.timeSeries.Key key = keyList.get(i); 
//				logger.info("{}", key);
			}
		}

		{
		// 10 per return record
			List<yokwe.iex.cloud.data.stock.Dividends> dividendList = yokwe.iex.cloud.data.stock.Dividends.getInstance(context, Range.Y1, "trtn");
			logger.info("dividendList = {}", dividendList.size());
			for(int i = 0; i < dividendList.size(); i++) {
				yokwe.iex.cloud.data.stock.Dividends dividend = dividendList.get(i); 
				logger.info("{}", dividend);
			}
		}
		{
		// 10 per return record
			Map<String, List<yokwe.iex.cloud.data.stock.Dividends>> mapList = yokwe.iex.cloud.data.stock.Dividends.getInstance(context, Range.Y1, "ibm", "amzn");
			logger.info("mapList = {}", mapList.size());
			for(Map.Entry<String, List<yokwe.iex.cloud.data.stock.Dividends>> entry: mapList.entrySet()) {
				logger.info("{}  {}", entry.getKey(), entry.getValue().size());
				for(yokwe.iex.cloud.data.stock.Dividends dividends: entry.getValue()) {
					logger.info("  {}", dividends);
				}
			}
		}

		{
		// 2 per call
			yokwe.iex.cloud.data.stock.Previous previous = yokwe.iex.cloud.data.stock.Previous.getInstance(context, "trtn");
			logger.info("previous = {}", previous);
		}
		
		{
		// 2 per call
			List<yokwe.iex.cloud.data.stock.Previous> previousList = yokwe.iex.cloud.data.stock.Previous.getInstance(context, "trtn", "ibm");
			logger.info("previousList = {}", previousList.size());
			for(int i = 0; i < previousList.size(); i++) {
				yokwe.iex.cloud.data.stock.Previous previous = previousList.get(i); 
				logger.info("{}", previous);
			}
		}

		
		{
		// 2 per call
			List<yokwe.iex.cloud.data.stock.ChartDate> chartDateList = yokwe.iex.cloud.data.stock.ChartDate.getInstance(context, "20190705", "trtn");
			logger.info("chartDateList = {}", chartDateList);
		}

		{
		// 2 per return record
			Map<String, List<yokwe.iex.cloud.data.stock.ChartDate>> mapList = yokwe.iex.cloud.data.stock.ChartDate.getInstance(context, "20190705", "ibm", "amzn");
			logger.info("mapList = {}", mapList.size());
			for(Map.Entry<String, List<yokwe.iex.cloud.data.stock.ChartDate>> entry: mapList.entrySet()) {
				logger.info("{}  {}", entry.getKey(), entry.getValue().size());
				for(yokwe.iex.cloud.data.stock.ChartDate dividends: entry.getValue()) {
					logger.info("  {}", dividends);
				}
			}
		}

		{
		// 10 per returned record
			List<yokwe.iex.cloud.data.stock.Chart> chartList = yokwe.iex.cloud.data.stock.Chart.getInstance(context, "trtn", Range.D5);
			logger.info("chartList = {}", chartList.size());
			for(int i = 0; i < chartList.size(); i++) {
				yokwe.iex.cloud.data.stock.Chart chart = chartList.get(i); 
				logger.info("{}", chart);
			}
		}

		{
		// 1 per call
			yokwe.iex.cloud.data.stock.OHLC ohlc = yokwe.iex.cloud.data.stock.OHLC.getInstance(context, "trtn");
			logger.info("ohlc = {}", ohlc);
		}
		{
		// 1 per call
			List<yokwe.iex.cloud.data.stock.OHLC> ohlcList = yokwe.iex.cloud.data.stock.OHLC.getInstance(context, "trtn", "ibm");
			logger.info("ohlcList = {}", ohlcList.size());
			for(int i = 0; i < ohlcList.size(); i++) {
				yokwe.iex.cloud.data.stock.OHLC ohlc = ohlcList.get(i); 
				logger.info("{}", ohlc);
			}
		}

		{
		// 2 per call
			yokwe.iex.cloud.data.stock.Previous previous = yokwe.iex.cloud.data.stock.Previous.getInstance(context, "trtn");
			logger.info("previous = {}", previous);
		}
		{
		// 2 per call
			List<yokwe.iex.cloud.data.stock.Previous> previousList = yokwe.iex.cloud.data.stock.Previous.getInstance(context, "trtn", "ibm");
			logger.info("previousList = {}", previousList.size());
			for(int i = 0; i < previousList.size(); i++) {
				yokwe.iex.cloud.data.stock.Previous previous = previousList.get(i); 
				logger.info("{}", previous);
			}
		}

		{
		// 1 per call
			List<yokwe.iex.cloud.data.reference.Sectors> sectorsList = yokwe.iex.cloud.data.reference.Sectors.getInstance(context);
			logger.info("sectorsList = {}", sectorsList.size());
			for(int i = 0; i < sectorsList.size(); i++) {
//				yokwe.iex.cloud.data.reference.Sectors sectors = sectorsList.get(i); 
//				logger.info("{}", sectors);
			}
		}

		{
		// 1 per call
			List<yokwe.iex.cloud.data.reference.Tags> tagList = yokwe.iex.cloud.data.reference.Tags.getInstance(context);
			logger.info("tagList = {}", tagList.size());
			for(int i = 0; i < tagList.size(); i++) {
//				yokwe.iex.cloud.data.reference.Tags tags = tagList.get(i); 
//				logger.info("{}", tags);
			}
		}

		{
		// 1 per record
			yokwe.iex.cloud.data.stock.Company company = yokwe.iex.cloud.data.stock.Company.getInstance(context, "IBM");
			logger.info("company {}", company);
		}

		{
		// 1 per record
			List<yokwe.iex.cloud.data.stock.Company> companyList = yokwe.iex.cloud.data.stock.Company.getInstance(context, "IBM", "TRTN");
			logger.info("tagList = {}", companyList.size());
			for(int i = 0; i < companyList.size(); i++) {
				yokwe.iex.cloud.data.stock.Company company = companyList.get(i); 
				logger.info("{}", company);
			}
		}

		logger.info("STOP");
	}
}
