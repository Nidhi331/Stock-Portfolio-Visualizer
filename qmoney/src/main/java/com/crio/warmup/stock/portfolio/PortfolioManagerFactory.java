
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.util.List;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;

import com.crio.warmup.stock.quotes.StockQuoteServiceFactory;
import com.crio.warmup.stock.quotes.StockQuotesService;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerFactory  {

  
  public static PortfolioManager getPortfolioManager(RestTemplate restTemplate) {
    // try {
    return new PortfolioManagerImpl(restTemplate);
    } 
    // catch(Exception e){
      // throw new StockQuoteServiceException("unavailable", e.getCause());
    // }

  // }
  // TODO: CRIO_TASK_MODULE_ADDITIONAL_REFACTOR
  //  Implement the method to return new instance of PortfolioManager.
  //  Steps:
  //    1. Create appropriate instance of StoockQuoteService using StockQuoteServiceFactory and then
  //       use the same instance of StockQuoteService to create the instance of PortfolioManager.
  //    2. Mark the earlier constructor of PortfolioManager as @Deprecated.
  //    3. Make sure all of the tests pass by using the gradle command below:
  //       ./gradlew test --tests PortfolioManagerFactory

  //  @Deprecated
   public static PortfolioManager getPortfolioManager(String provider,
     RestTemplate restTemplate) {
      //  if (provider==null || provider.isEmpty()) {
      //    throw new StockQuoteServiceException("provider is null");
      //  }
      //  try {
         StockQuotesService StockQuotes = StockQuoteServiceFactory.getService(provider, restTemplate);
         return new PortfolioManagerImpl(StockQuotes);
      }
      //  }
      //  catch(Exception e) {
      //    throw new StockQuoteServiceException("unavailable", e.getCause());
       }
  //  }

// }
