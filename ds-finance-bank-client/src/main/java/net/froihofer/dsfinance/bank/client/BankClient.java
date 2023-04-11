package net.froihofer.dsfinance.bank.client;

import java.util.Properties;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

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
  private void getRmiProxy() {
    AuthCallbackHandler.setUsername("customer");
    AuthCallbackHandler.setPassword("customerpass");
    Properties props = new Properties();
    props.put(Context.SECURITY_PRINCIPAL,AuthCallbackHandler.getUsername());
    props.put(Context.SECURITY_CREDENTIALS,AuthCallbackHandler.getPassword());
    try {
      WildflyJndiLookupHelper jndiHelper = new WildflyJndiLookupHelper(new InitialContext(props), "ds-finance-bank-ear", "ds-finance-bank-ejb", "");
      //TODO: Lookup the proxy and assign it to some variable or return it by changing the
      //      return type of this method
    }
    catch (NamingException e) {
      log.error("Failed to initialize InitialContext.",e);
    }
  }

  private void run() {
    //TODO implement the client part
  }

  public static void main(String[] args) {
    BankClient client = new BankClient();
    client.run();
  }
}
