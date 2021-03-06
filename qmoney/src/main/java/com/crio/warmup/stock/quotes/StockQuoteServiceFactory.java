
package com.crio.warmup.stock.quotes;

import org.springframework.web.client.RestTemplate;
import com.crio.warmup.stock.quotes.TiingoService;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.AlphavantageService;

public enum StockQuoteServiceFactory {

  // Note: (Recommended reading)
  // Pros and cons of implementing Singleton via enum.
  // https://softwareengineering.stackexchange.com/q/179386/253205

  INSTANCE;

  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Make sure that you have implemented TiingoService and AlphavantageService
  //  as per the instructions and the tests are passing for them.
  //  Implement the factory function such that it will return TiingoService instance when
  //  provider == 'tiingo' (Case insensitive)
  //  Return new instance of AlphavantageService in all other cases.
  //  RestTemplate is passed as a parameter along, and you will have to pass the same to
  //  the constructor of corresponding class.
  //  Run the tests using command below and make sure it passes
  //  ./gradlew test --tests StockQuoteServiceFactory

  public static StockQuotesService getService(String provider,  RestTemplate restTemplate) {
  //   try {
  //   if (provider == "tiingo" || provider == "Tiingo") {
  //    StockQuotesService tiingo = new TiingoService(restTemplate);
  //    return tiingo ;
  //   }
  //   else {
  //     StockQuotesService alpha = new AlphavantageService(restTemplate);
  //     return alpha;
  //   }
  // }
  // catch (Exception e) {
  //   throw new StockQuoteServiceException("Unavailable", e.getCause());
  // }
  String provider_check = "tiingo";
  StockQuotesService quote_provider;
  if(provider.equalsIgnoreCase(provider_check)) quote_provider = new TiingoService(restTemplate);
  else quote_provider = new AlphavantageService(restTemplate);
  return quote_provider;
}
}
