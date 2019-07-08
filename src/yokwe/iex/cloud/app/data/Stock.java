package yokwe.iex.cloud.app.data;

import yokwe.iex.cloud.Base;

public class Stock extends Base implements Comparable<Stock> {
	public static String PATH = "tmp/data/stock.csv";
	
	/*
	public enum Type {
		ADR("ad"),     // American Depository Receipt
		CEF("cef"),    // Closed End Fund
		COM("cs"),     // Common Share
		ETF("et"),     // Exchange Traded Fund
        PRF("ps"),     // Preferred Share
        RIG("rt"),     // Rights
        STR("struct"), // Structured Bond
        UNI("ut"),     // Units
        WAR("wt");     // Warrant
		
		public final String value;
		Type(String value) {
			this.value = value;
		}
	}
	*/
	
	public String symbol;
	public String type;
	public String name;
	
	public Stock(String symbol, String type, String name) {
		this.symbol   = symbol;
		this.type     = type;
		this.name     = name;
	}
	
	public Stock() {
		this(null, null, null);
	}
	
	@Override
	public int compareTo(Stock that) {
		return this.symbol.compareTo(that.symbol);
	}
}
