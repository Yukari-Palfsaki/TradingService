package net.froihofer.util.jboss.TradingServiceImplementation;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import net.froihofer.dsfinance.ws.trading.TradingWebService;
import net.froihofer.dsfinance.ws.trading.TradingWebServiceService;
import net.froihofer.ejb.bank.common.*;
import net.froihofer.util.jboss.entity.PublicStockQuoteTranslator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.security.PermitAll;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.ws.Binding;
import javax.xml.ws.BindingProvider;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Stateless(name="TradingSevice")
@PermitAll
@Path("/trading")
@Consumes("application/json")
@Produces("application/json")
public class TradingInterfaceImplementation implements TradingInterface {
  // Logger for SRV
  private static final Logger log = LoggerFactory.getLogger(TradingInterfaceImplementation.class);
  private Credentials crd = new Credentials();
  private Client restClient = null;
  @Inject
  PublicStockQuoteTranslator publicStockQuoteTranslator;
  private TradingWebService tradingWebService;

  @Override
  @GET
  @Path("/findStock")
  public TradingSearchResult searchResult (@QueryParam("symbol") String symbol) {
    binding();
    log.info("Trying to search a stock by symbol: " + symbol);

    TradingSearchResult result = new TradingSearchResult();
    try {
      var quotes = tradingWebService.findStockQuotesByCompanyName(symbol);
      log.info("Stock size: " + quotes.size());
      for (int i = 0; i < quotes.size(); i++){
        result.shares.add (publicStockQuoteTranslator.toPublicStockQuoteDTO(quotes.get(i)));
      }
      result.succeeded = true;
    } catch (Exception e){
      log.error(e.toString(), e);
      int index = e.getMessage().indexOf(':');
      result.msg = (index == -1) ? e.getMessage() : e.getMessage().substring(index + 1);
    }
    return result;
  }
  @Override
  @Path("/buyStock")
  @POST
  public TradingBuyResult buyStockByName(@QueryParam("symbol") String symbol, @QueryParam("shares") int shares) {
    binding();
    log.info("try buy Stock by symbol: " + symbol + " Amount: " + shares);

    TradingBuyResult result = new TradingBuyResult();
    try {
      result.count = tradingWebService.buy(symbol, shares);
      result.succeeded = true;

    } catch (Exception e) {
      log.error(e.toString(), e);
      result.count = BigDecimal.valueOf(0);
      int index = e.getMessage().indexOf(':');
      result.msg = index == -1 ? e.getMessage() : e.getMessage().substring(index + 1);
    }
    return result;
  }
  @Override
  @Path("/sellStock")
  @POST
  public TradingSellResult sellStockByName(@QueryParam("symbol") String symbol, @QueryParam("shares") int shares) {
    if (restClient == null) {
      restClient = ClientBuilder.newClient().register(new JaxRsAuthenticator(crd.clientUsername(), crd.clientPassword())).register(JacksonJsonProvider.class);
    }

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule()); //TODO

    String url = String.format("https://edu.dedisys.org/ds-finance/ws/rs/trading/stock/%s/sell", symbol);
    WebTarget target = restClient.target(url);

    Response response = target
        .request(MediaType.APPLICATION_JSON)
        .post(Entity.json(String.format("%d", shares)));

    int status = response.getStatus();
    String json = response.readEntity(String.class);
    TradingSellResult result = new TradingSellResult();

    try {
      double value = Double.parseDouble(json);
      result.count = BigDecimal.valueOf(value);
      result.succeeded = true;
    }
    catch (Exception e) {
      int index = e.getMessage().lastIndexOf(':');
      result.msg = index == -1 ? e.getMessage() : e.getMessage().substring(index + 1);
      result.succeeded = false;
    }

    return result;
  }// end sellStockByName
  public TradingCourseDevelopmentResult courseDevelopmentResult(@QueryParam("symbol") String symbol) {
    if (restClient == null) {
      restClient = ClientBuilder.newClient().register(new JaxRsAuthenticator (crd.clientUsername(), crd.clientPassword())).register(JacksonJsonProvider.class);
    }

    ObjectMapper mapper = new ObjectMapper();
    mapper.registerModule(new JavaTimeModule());
    String url = String.format("https://edu.dedisys.org/ds-finance/ws/rs/trading/stock/%s/history", symbol);
    WebTarget target = restClient.target(url);

    Response response = target
        .request(MediaType.APPLICATION_JSON)
        .get();

    int status = response.getStatus();
    String json = response.readEntity(String.class);
    TradingCourseDevelopmentResult result = new TradingCourseDevelopmentResult();
    try {
      result.shares = mapper.readValue(json, new TypeReference<ArrayList<PublicStockQuoteDTO>>() {});
      result.succeeded = true;
    } catch (JsonProcessingException e) {
      int index = e.getMessage().lastIndexOf(':');
      result.msg = index == -1 ? e.getMessage() : e.getMessage().substring(index + 1);
      result.succeeded = false;
    }
    return result;

  }

  // To bind the Username + Password of a client
  public void binding(){
    Credentials crd = new Credentials();
    TradingWebServiceService tradingWebServiceService = new TradingWebServiceService();
    tradingWebService = tradingWebServiceService.getTradingWebServicePort();
    BindingProvider bindingProvider = (BindingProvider) tradingWebService;
    bindingProvider.getRequestContext().put(BindingProvider.USERNAME_PROPERTY, crd.clientUsername());
    bindingProvider.getRequestContext().put(BindingProvider.PASSWORD_PROPERTY, crd.clientPassword());
  }
}
