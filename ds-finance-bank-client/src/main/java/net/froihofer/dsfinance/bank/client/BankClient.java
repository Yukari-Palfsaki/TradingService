package net.froihofer.dsfinance.bank.client;

import java.math.BigDecimal;
import java.util.Properties;
import java.util.Scanner;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import net.froihofer.ejb.bank.common.Bank;
import net.froihofer.util.AuthCallbackHandler;
import net.froihofer.util.WildflyJndiLookupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for starting the bank client.
 *
 */
public class BankClient {
  private static Logger log = LoggerFactory.getLogger(BankClient.class);

  /**
   * Skeleton method for performing an RMI lookup
   */
  private Bank getRmiProxy() {
    AuthCallbackHandler.setUsername("employee1");
    AuthCallbackHandler.setPassword("employee1pass");
    Properties props = new Properties();
    props.put(Context.SECURITY_PRINCIPAL,AuthCallbackHandler.getUsername());
    props.put(Context.SECURITY_CREDENTIALS,AuthCallbackHandler.getPassword());
    try {
      WildflyJndiLookupHelper jndiHelper = new WildflyJndiLookupHelper(new InitialContext(props), "ds-finance-bank-ear", "ds-finance-bank-ejb", "");
      //TODO: Lookup the proxy and assign it to some variable or return it by changing the
      //      return type of this method
      var bank =  jndiHelper.lookup("BankService", Bank.class);
      return bank;
    }
    catch (NamingException e) {
      log.error("Failed to initialize InitialContext.",e);
    }
    return null;
  }

  private void run() {
    Bank bank = getRmiProxy();
    System.out.println("Client Test");
    Scanner scanner = new Scanner(System.in);

    while(true) {
      System.out.println("Search or buy?");
      var option = scanner.nextLine().toLowerCase();
      if(option.equals("search")) {
        System.out.println("Enter name to search: ");
        var name = scanner.nextLine();
        var test = bank.findStockByName(name);
        System.out.println("Search result");
        for(int i = 0; i < test.size(); i++) {
          System.out.printf("%s: %s\n", test.get(i).getCompanyName(), test.get(i).getSymbol());
        }
      } else if(option.equals("buy")) {
        System.out.println("Enter symbol: ");
        var symbol = scanner.nextLine();
        System.out.println("Enter amount: ");
        var amount = scanner.nextLine();
        try {
          int shares = Integer.parseInt(amount);
          var result = bank.buyStockByName(symbol, shares);
          System.out.println("Cost per share: " + result);
          System.out.println("Overall cost: " + result.multiply(BigDecimal.valueOf(shares)));

        } catch (Exception e) {
          e.printStackTrace();
        }
      }
    }


    //TODO implement the client part
  }

  public static void main(String[] args) {
    BankClient client = new BankClient();
    client.run();
  }
}
