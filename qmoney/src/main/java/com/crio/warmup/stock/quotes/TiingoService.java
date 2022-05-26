
package com.crio.warmup.stock.quotes;

import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.naming.ServiceUnavailableException;

import org.springframework.web.client.RestTemplate;

public class TiingoService implements StockQuotesService {

  private RestTemplate restTemplate;

  protected TiingoService(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;

  }

  public static String getToken() {
    String token = "209ac85df2915ec7ab39b5540baebb2eda5db14c";
    return token;
  }

  private String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    return "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate + "&endDate=" + endDate
        + "&token=" + getToken();
  }

  @Override
  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws NullPointerException, RuntimeException, JsonMappingException, JsonProcessingException, StockQuoteServiceException
      {
    // TODO Auto-generated method stub
    if (to.isBefore(from))
      throw new RuntimeException();
    String url = buildUri(symbol, from, to);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    List<Candle> candles = new ArrayList<>();
    TiingoCandle[] tc = null;
    String response;

    try {
      response = this.restTemplate.getForObject(url, String.class);
      if (response == null || response.isEmpty()) {
        throw new StockQuoteServiceException("No response");
      }
      tc = objectMapper.readValue(response, TiingoCandle[].class);

      for (TiingoCandle c : tc) {
        candles.add((Candle) c);
      }
      Collections.sort(candles, getComparator());
      return Arrays.asList(tc);

    } catch (IOException e) {
      throw new StockQuoteServiceException("Not able to process the response from a third-party service.", e);

    } catch (NullPointerException e) {
      throw new StockQuoteServiceException("The third party api returned null", e);

    } catch (RuntimeException e) {
      throw new StockQuoteServiceException("Not able to process the response from a third-party service.", e);
    }
    

  }

  private Comparator<Candle> getComparator() {
    return Comparator.comparing(Candle::getDate);
  }

}
