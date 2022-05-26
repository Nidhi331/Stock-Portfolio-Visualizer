
package com.crio.warmup.stock.portfolio;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.Candle;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.exception.StockQuoteServiceException;
import com.crio.warmup.stock.quotes.AlphavantageService;
import com.crio.warmup.stock.quotes.StockQuotesService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import static java.time.temporal.ChronoUnit.DAYS;

import javax.naming.ServiceUnavailableException;

import org.springframework.web.client.RestTemplate;

public class PortfolioManagerImpl implements PortfolioManager {

  private StockQuotesService stockQuotes;
  private RestTemplate restTemplate;
  private ExecutorService threadPool = null;

  // Caution: Do not delete or modify the constructor, or else your build will
  // break!
  // This is absolutely necessary for backward compatibility
  protected PortfolioManagerImpl(RestTemplate restTemplate) {
    this.restTemplate = restTemplate;
  }
  
  public PortfolioManagerImpl(StockQuotesService stockQuotesService) {
    this.stockQuotes = stockQuotesService;
  }
    
  

 

 

  private Comparator<AnnualizedReturn> getComparator() {
    return Comparator.comparing(AnnualizedReturn::getAnnualizedReturn).reversed();
  }

  // CHECKSTYLE:OFF

  // TODO: CRIO_TASK_MODULE_REFACTOR
  // Extract the logic to call Tiingo third-party APIs to a separate function.
  // Remember to fill out the buildUri function and use that.

  public List<Candle> getStockQuote(String symbol, LocalDate from, LocalDate to)
      throws JsonProcessingException, StockQuoteServiceException {
    try {
    String url = buildUri(symbol, to, from);
    ObjectMapper objectMapper = new ObjectMapper();
    objectMapper.registerModule(new JavaTimeModule());
    RestTemplate rt = new RestTemplate();
    TiingoCandle[] tc = rt.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(tc);
    }
    catch(Exception e){
      throw new StockQuoteServiceException("unavailable",e.getCause());
    }
  }

  // protected String buildUri(String symbol, LocalDate startDate, LocalDate
  // endDate) {
  // String uriTemplate = "https://api.tiingo.com/tiingo/daily/$SYMBOL/prices?"
  // + "startDate=$STARTDATE&endDate=$ENDDATE&token=$APIKEY";
  // return uriTemplate;
  // }

  protected String buildUri(String symbol, LocalDate startDate, LocalDate endDate) {
    return "https://api.tiingo.com/tiingo/daily/" + symbol + "/prices?startDate=" + startDate + "&endDate=" + endDate
        + "&token=" + getToken();
  }

  public static String getToken() {
    String token = "209ac85df2915ec7ab39b5540baebb2eda5db14c";
    return token;
  }

  @Override
  public List<AnnualizedReturn> calculateAnnualizedReturn(List<PortfolioTrade> portfolioTrades, LocalDate endDate)
      throws StockQuoteServiceException, NullPointerException, RuntimeException {
    // TODO Auto-generated method stub
    String token = "a5f2f36133e35fc6729a13354fa53cec7d087228";
    try {
    List<AnnualizedReturn> annualizedForAllStocks = new ArrayList<AnnualizedReturn>();
    for (PortfolioTrade t : portfolioTrades) {
      List<Candle> tc = stockQuotes.getStockQuote(t.getSymbol(),endDate, t.getPurchaseDate());
      Double sellPrice = tc.get(tc.size()-1).getClose();
      Double buyPrice = tc.get(0).getOpen();
      Double totalReturns = (sellPrice - buyPrice) / buyPrice;
      double noOfDaysBetween = ChronoUnit.DAYS.between(t.getPurchaseDate(), endDate);
      double total_num_years = noOfDaysBetween / 365.0;
      Double annualized_returns = Math.pow((1 + totalReturns), (double) (1 / total_num_years)) - 1;
      annualizedForAllStocks.add(new AnnualizedReturn(t.getSymbol(), annualized_returns, totalReturns));
      
    }
      Collections.sort(annualizedForAllStocks, getComparator());
      return annualizedForAllStocks;
  }
    catch (JsonProcessingException e) {
      throw new StockQuoteServiceException("Unable to process json file", e);
    }
    catch(RuntimeException e){
      throw new StockQuoteServiceException("unavailable",e);
    }
  
}

@Override
public List<AnnualizedReturn> calculateAnnualizedReturnParallel(List<PortfolioTrade> portfolioTrades, LocalDate endDate,
    int numThreads) throws InterruptedException, StockQuoteServiceException {
  // TODO Auto-generated method stub
  List<AnnualizedReturn> annualizedReturns = new ArrayList<>();
    List<Callable<List<Object>>> callableTasks = new ArrayList<>();

    if (threadPool == null)
      threadPool = Executors.newFixedThreadPool(numThreads);

    for (PortfolioTrade trade : portfolioTrades) {
      String symbol = trade.getSymbol();
      LocalDate startDate = trade.getPurchaseDate();

      callableTasks.add(() -> {
        List<Candle> quotes = stockQuotes.getStockQuote(symbol, startDate, endDate);

        return Arrays.asList(quotes, symbol, startDate);
      });
    }

    List<Future<List<Object>>> futureTasks = threadPool.invokeAll(callableTasks);

    for (Future<List<Object>> task : futureTasks) {
      LocalDate startDate = LocalDate.now();
      String symbol = "";
      List<Candle> quotes = new ArrayList<>();

      try {
        quotes = (List<Candle>) task.get().get(0);
        symbol = (String) task.get().get(1);
        startDate = (LocalDate) task.get().get(2);
      } catch (ExecutionException e) {
        throw new StockQuoteServiceException(e.getMessage());
      } catch (InterruptedException e) {
        e.printStackTrace();
      }

      double totalNumOfYears = (double) startDate.until(endDate, DAYS) / 365.24;
      double buyPrice = quotes.get(0).getOpen();
      double sellPrice = quotes.get(quotes.size() - 1).getClose();

      Double totalReturns = (sellPrice - buyPrice) / buyPrice;
      Double annualizedReturn = Math.pow((1 + totalReturns), (1 / totalNumOfYears)) - 1;

      annualizedReturns.add(new AnnualizedReturn(symbol, annualizedReturn, totalReturns));
    }

    Collections.sort(annualizedReturns, this.getComparator());

    threadPool.shutdown();
    try {
      if (!threadPool.awaitTermination(800, TimeUnit.MILLISECONDS)) {
        threadPool.shutdownNow();
      }
    } catch (InterruptedException e) {
      threadPool.shutdownNow();
    }

    return annualizedReturns;
  }
}
