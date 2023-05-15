package net.froihofer.ejb.bank.common;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/* This small class to help the client option of search the share name*/
public class TradingSearchResult implements Serializable {
  public List<PublicStockQuoteDTO> shares = new ArrayList<>();
  public String msg = "";
  public boolean succeeded = false;
}
