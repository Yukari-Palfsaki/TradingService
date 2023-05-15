package net.froihofer.ejb.bank.common;

import java.io.Serializable;
import java.math.BigDecimal;

public class TradingSellResult implements Serializable {
  public String msg = "";
  public BigDecimal count = BigDecimal.valueOf(0);
  public boolean succeeded = false;
}
