
package com.crio.warmup.stock.quotes;

import static java.time.temporal.ChronoUnit.DAYS;
import static java.time.temporal.ChronoUnit.SECONDS;

import java.io.IOException;

import com.crio.warmup.stock.dto.AlphavantageCandle;
import com.crio.warmup.stock.dto.AlphavantageDailyResponse;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import javax.naming.ServiceUnavailableException;

import org.springframework.web.client.RestTemplate;
import java.util.Collections;

public class AlphavantageService implements StockQuotesService {
  private RestTemplate restTemplate;

  public AlphavantageService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;

  }

  public static String getToken() {
    String token = "715D6TJYDINIHZUQ";
    return token;
  }

  public String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    return "https://www.alphavantage.co/query?function=TIME_SERIES_DAILY" + "&symbol=" + symbol + "&apikey="
        + getToken();
  }

  private Comparator<Candle> getComparator() {
    return Comparator.comparing(Candle::getDate);
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws StockQuoteServiceException, JsonMappingException, JsonProcessingException{
    // TODO Auto-generated method stub
    String url = buildUri(symbol, from, to);
    if (to.isBefore(from))
      throw new StockQuoteServiceException("runtime");

    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    String tc;

    List<Candle> can = new ArrayList<>();
    

    try {
      tc = this.restTemplate.getForObject(url, String.class);
      if (tc == null || tc.isEmpty()) {
        throw new StockQuoteServiceException("No response");
      }
      AlphavantageDailyResponse alpha = objectMapper.readValue(tc, AlphavantageDailyResponse.class);

      Map<LocalDate, AlphavantageCandle> candles = alpha.getCandles();

      for (Map.Entry<LocalDate, AlphavantageCandle> entry : candles.entrySet()) {
        LocalDate date = entry.getKey();
        if ((date.isEqual(from) || date.isEqual(to)) || (date.isAfter(from) && date.isBefore(to))) {
          AlphavantageCandle candle = entry.getValue();
          if (candle != null) {
            candle.setDate(date);
            can.add(candle);
          }
        }
      }
    } catch (IOException e) {
      throw new StockQuoteServiceException("Not able to process the response from a third-party service.", e);

    } catch (NullPointerException e) {
      throw new StockQuoteServiceException("Not able to process the response from a third-party service.", e);
    } catch (RuntimeException e) {
      throw new StockQuoteServiceException("Not able to process the response from a third-party service.", e);
    }
    
    Collections.sort(can, getComparator());
    return can;
}
}


  // TODO: CRIO_TASK_MODULE_EXCEPTIONS
  //   1. Update the method signature to match the signature change in the interface.
  //   2. Start throwing new StockQuoteServiceException when you get some invalid response from
  //      Alphavantage, or you encounter a runtime exception during Json parsing.
  //   3. Make sure that the exception propagates all the way from PortfolioManager, so that the
  //      external user's of our API are able to explicitly handle this exception upfront.
  //CHECKSTYLE:OFF



