package net.froihofer.ejb.bank.common;

import javax.ejb.Remote;
import java.math.BigDecimal;

@Remote
public interface TradingInterface {
  public TradingSearchResult findStockByName(String name);
  public TradingBuyResult buyStockByName(String symbol, int shares);
  public TradingSellResult sellStockByName (String symbol, int shares);
  public TradingCourseDevelopmentResult courseDevelopmentByName (String symbol);
}
