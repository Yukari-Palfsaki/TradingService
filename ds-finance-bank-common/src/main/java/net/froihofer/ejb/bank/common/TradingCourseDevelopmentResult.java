package net.froihofer.ejb.bank.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TradingCourseDevelopmentResult implements Serializable {
  public List<PublicStockQuoteDTO> shares = new ArrayList<>();
  public String msg = "";
  public boolean succeeded = false;
}
