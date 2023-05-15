package net.froihofer.dsfinance.bank.client;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Properties;
import java.util.Scanner;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import net.froihofer.ejb.bank.common.Credentials;
import net.froihofer.ejb.bank.common.TradingInterface;
//import net.froihofer.ejb.bank.common.BankException;
import net.froihofer.ejb.bank.common.JaxRsAuthenticator;
import net.froihofer.ejb.bank.common.PublicStockQuoteDTO;
import net.froihofer.util.AuthCallbackHandler;
import net.froihofer.util.WildflyJndiLookupHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Main class for starting the bank client.
 *
 */
public class TradingClient {
  private static Logger log = LoggerFactory.getLogger(TradingClient.class);
  private Client client;
  private WebTarget baseTarget;
  /* ?? */
  public void setupRest (){
    client = ClientBuilder.newClient()
        .register(new JaxRsAuthenticator("customer1", "customer1Pass"))
        .register(JacksonJsonProvider.class);
    baseTarget = client.target ("http://localhost:8080/ds-finance-bank-web/rs/bank");
  }

  /**
   * Skeleton method for performing an RMI lookup
   */
  private TradingInterface getRmiProxy() {
    Credentials crd = new Credentials();
    AuthCallbackHandler.setUsername(crd.employeeUsername());
    AuthCallbackHandler.setPassword(crd.employeePassword());
    Properties props = new Properties();
    props.put(Context.SECURITY_PRINCIPAL,AuthCallbackHandler.getUsername());
    props.put(Context.SECURITY_CREDENTIALS,AuthCallbackHandler.getPassword());
    try {
      WildflyJndiLookupHelper jndiHelper = new WildflyJndiLookupHelper(new InitialContext(props), "ds-finance-bank-ear", "ds-finance-bank-ejb", "");
      //TODO: Lookup the proxy and assign it to some variable or return it by changing the
      //      return type of this method
      var bank =  jndiHelper.lookup("BankService", TradingInterface.class);
      return bank;
    }
    catch (NamingException e) {
      int index = e.getMessage().lastIndexOf(':'); // find the index number of ":"
      String message = (index == -1) ? e.getMessage() : e.getMessage().substring(index + 1);
      System.out.println(message);
      log.error("Failed to initialize InitialContext.",e);
    }
    return null;
  }

  private void run() {
    TradingInterface bank = getRmiProxy();
    setupRest();// TODO
    System.out.println("Trading Client");
    if (bank == null){
      return;
    }

    System.out.println("\nWELCOME TO TRADING SERVICE!\n");

    Scanner scanner = new Scanner(System.in);

    while(true) {
      System.out.println("Please select\n");
      System.out.println("(1) Search\n");
      System.out.println("(2) Buy\n");
      System.out.println("(3) Sell\n");
      System.out.println("(4) See history\n");
      System.out.println("(5) Exit");

      var option = scanner.nextLine().toLowerCase();
      // Options for search, sell, buy
      switch (option) {
        case "1": { // Search
          System.out.println("Enter share name to search: ");
          var name = scanner.nextLine();
          var searchResult = bank.findStockByName(name);
          if (searchResult.succeeded) {
            if(searchResult.shares.isEmpty()) {
              System.out.println("No item with your input " + name + " is found");
            }
            else {
              System.out.println("Search result: ");
              for (int i = 0; i < searchResult.shares.size();i++){
                System.out.println(searchResult.shares.get(i).getCompanyName() + ": "+ searchResult.shares.get(i).getSymbol());
              } // end for
            } // end else
          }
          break;
        }

        case "2": {  //Buy
          System.out.println("Enter the share name: ");
          var symbol = scanner.nextLine();
          System.out.println("Enter amount: ");
          var amount = scanner.nextLine();
          try {
            int shares = Integer.parseInt(amount);
            var buyResult = bank.buyStockByName(symbol, shares);
            if (buyResult.succeeded) {
              System.out.println("Cost per share: " + buyResult);
              System.out.println("Overall cost: " + buyResult.count.multiply(BigDecimal.valueOf(shares)));
            } else {
              System.out.println("Failed to buy " + shares + " of " + symbol + ": " + buyResult.msg);
            }
          }  catch (Exception e) {
            log.error("Something did not work, see stack trace.", e);
            e.printStackTrace();
          }
          break;
        }
        case "3": { // Sell
          System.out.println("Enter the share name to sell: ");
          var symbol = scanner.nextLine();
          System.out.println("Enter amount: ");
          var amount = scanner.nextLine();
          try {
            int shares = Integer.parseInt(amount);
            var sellResult = bank.sellStockByName(symbol, shares);

            if (sellResult.succeeded){
              System.out.println("Cost per share: " + sellResult);
              System.out.println("Overall cost" + sellResult.count.multiply(BigDecimal.valueOf(shares)));
            } else {
              System.out.println("Failed to sell "+ shares + " shares of " + symbol + ": "+sellResult.msg);
            }
          } catch (Exception e) {
            log.error("Something did not work, see stack trace." + e);
            e.printStackTrace();
          }
          break;
        }
        case "4": {
          System.out.println("Enter share name for history: ");
          var symbol = scanner.nextLine();
          try {
            var historyResult = bank.courseDevelopmentByName (symbol);
            if (historyResult.succeeded){
              System.out.println("Cost per share: " + historyResult);
            } else {
              System.out.println("Cannot show the history.");
            }
          }catch (Exception e){
            log.error( ("Something did not work, see stack trace."));
            e.printStackTrace();
          }
          break;
        }
      }
    }
    //TODO implement the client part
  }

  public static void main(String[] args) {
    TradingClient client = new TradingClient();
    client.run();
  }
}
