
package com.crio.warmup.stock;


import com.crio.warmup.stock.dto.*;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.crio.warmup.stock.portfolio.PortfolioManager;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays; 
import java.util.Collections;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.crio.warmup.stock.dto.AnnualizedReturn;
import com.crio.warmup.stock.dto.PortfolioTrade;
import com.crio.warmup.stock.dto.TiingoCandle;
import com.crio.warmup.stock.dto.TotalReturnsDto;
import com.crio.warmup.stock.portfolio.PortfolioManagerFactory;
import com.crio.warmup.stock.log.UncaughtExceptionHandler;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import org.apache.logging.log4j.ThreadContext;
import org.springframework.web.client.RestTemplate;

public class PortfolioManagerApplication {
  public static List<String> mainReadFile(String[] args) throws IOException, URISyntaxException {
    File f = resolveFileFromResources(args[0]);
    ObjectMapper om = getObjectMapper();
    PortfolioTrade[] trades = om.readValue(f, PortfolioTrade[].class);
    List<String> arr = new ArrayList<>();
    for(PortfolioTrade trade:trades)
    {
      arr.add(trade.getSymbol());
    }
    return arr;
    //  return Collections.emptyList();
  }

  private static ObjectMapper getObjectMapper() {
    ObjectMapper om = new ObjectMapper();
    om.registerModule(new JavaTimeModule());
    return om;
  }

  private static void printJsonObject(Object object) throws IOException {
    Logger logger = Logger.getLogger(PortfolioManagerApplication.class.getCanonicalName());
    ObjectMapper mapper = new ObjectMapper();
    logger.info(mapper.writeValueAsString(object));
  }

  private static File resolveFileFromResources(String filename) throws URISyntaxException {
    return Paths.get(Thread.currentThread().getContextClassLoader().getResource(filename).toURI()).toFile();
  }
  // TODO:
  // Build the Url using given parameters and use this function in your code to
  // cann the API.

  public static List<String> debugOutputs() {

    String valueOfArgument0 = "trades.json";
    String resultOfResolveFilePathArgs0 = "/home/crio-user/workspace/nidhij1503-ME_QMONEY_V2/qmoney/bin/main/trades.json";
    String toStringOfObjectMapper = "com.fasterxml.jackson.databind.ObjectMapper@2f9f7dcf";
    String functionNameFromTestFileInStackTrace = "PortfolioManagerApplicationTest.mainReadFile";
    String lineNumberFromTestFileInStackTrace = "29";

    return Arrays.asList(new String[] { valueOfArgument0, resultOfResolveFilePathArgs0, toStringOfObjectMapper,
        functionNameFromTestFileInStackTrace, lineNumberFromTestFileInStackTrace });
  }

  // Note:
  // Remember to confirm that you are getting same results for annualized returns
  // as in Module 3.
  public static List<TotalReturnsDto> allquotes(List<PortfolioTrade> trades, String[] args, ObjectMapper objectMapper)
      throws JsonMappingException, JsonProcessingException {
    List<TotalReturnsDto> st = new ArrayList<TotalReturnsDto>();
    
    //String token = "a5f2f36133e35fc6729a13354fa53cec7d087228";
    String token = "209ac85df2915ec7ab39b5540baebb2eda5db14c";
    RestTemplate rest = new RestTemplate();
    for (PortfolioTrade t : trades) {

      String url = prepareUrl(t, LocalDate.parse(args[1]), token);
      String result = rest.getForObject(url, String.class);
      List<TiingoCandle> tc = objectMapper.readValue(result, new TypeReference<List<TiingoCandle>>() {
      });

      TiingoCandle c = tc.get(tc.size() - 1);

      TotalReturnsDto returns = new TotalReturnsDto(t.getSymbol(), c.getClose());
      st.add(returns);
    }
    return st;
  }

  // TODO: CRIO_TASK_MODULE_REST_API
  // Find out the closing price of each stock on the end_date and return the list
  // of all symbols in ascending order by its close value on end date.


  // TODO: CRIO_TASK_MODULE_REST_API
  //  Find out the closing price of each stock on the end_date and return the list
  //  of all symbols in ascending order by its close value on end date.

  // Note:
  // 1. You may have to register on Tiingo to get the api_token.
  // 2. Look at args parameter and the module instructions carefully.
  // 2. You can copy relevant code from #mainReadFile to parse the Json.
  // 3. Use RestTemplate#getForObject in order to call the API,
  // and deserialize the results in List<Candle>

  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile

  // TODO:
  // Build the Url using given parameters and use this function in your code to
  // cann the API.

  public static List<String> mainReadQuotes(String[] args) throws IOException, URISyntaxException {
    List<PortfolioTrade> trades = readTradesFromJson(args[0]);

    ObjectMapper objectMapper = getObjectMapper();
    List<TotalReturnsDto> stocks = allquotes(trades, args, objectMapper);

    Collections.sort(stocks, new sortByClosingPrice());
    List<String> toReturn = new ArrayList<String>();
    for (TotalReturnsDto s : stocks) {
      toReturn.add(s.getSymbol());
    }
    return toReturn;
  }


  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Now that you have the list of PortfolioTrade and their data, calculate
  // annualized returns
  // for the stocks provided in the Json.
  // Use the function you just wrote #calculateAnnualizedReturns.
  // Return the list of AnnualizedReturns sorted by annualizedReturns in
  // descending order.

  // Note:
  // 1. You may need to copy relevant code from #mainReadQuotes to parse the Json.
  // 2. Remember to get the latest quotes from Tiingo API.

  // TODO:
  // Ensure all tests are passing using below command
  // ./gradlew test --tests ModuleThreeRefactorTest
  static Double getOpeningPriceOnStartDate(List<Candle> candles) {
    return candles.get(0).getOpen();
  }


  public static Double getClosingPriceOnEndDate(List<Candle> candles) {    
    return candles.get(candles.size()-1).getClose();
  }

  public static List<Candle> fetchCandles(PortfolioTrade trade, LocalDate endDate, String token)
      throws JsonMappingException, JsonProcessingException {
    String url = prepareUrl(trade, endDate, token);
    ObjectMapper objectMapper = getObjectMapper();
    RestTemplate rt = new RestTemplate();
    TiingoCandle[] tc = rt.getForObject(url, TiingoCandle[].class);
    return Arrays.asList(tc);
  }
  
  // TODO:
  //  After refactor, make sure that the tests pass by using these two commands
  //  ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  //  ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
 

  // TODO:
  //  Build the Url using given parameters and use this function in your code to cann the API.
 


  public static List<AnnualizedReturn> mainCalculateSingleReturn(String[] args) throws IOException, URISyntaxException {
    File file = resolveFileFromResources(args[0]);
    ObjectMapper obj = getObjectMapper();
    PortfolioTrade[] trade = obj.readValue(file, PortfolioTrade[].class);
    String token = "209ac85df2915ec7ab39b5540baebb2eda5db14c";
    //String token = "a5f2f36133e35fc6729a13354fa53cec7d087228";
    List<AnnualizedReturn> annualizedForAllStocks = new ArrayList<AnnualizedReturn>();
    for (PortfolioTrade t : trade) {
      List<Candle> tc = fetchCandles(t, LocalDate.parse(args[1]), token);
      Double closingPrice = getClosingPriceOnEndDate(tc);
      Double purchasePrice = getOpeningPriceOnStartDate(tc);
      AnnualizedReturn a = calculateAnnualizedReturns(LocalDate.parse(args[1]), t, purchasePrice, closingPrice);
      annualizedForAllStocks.add(a);
    }
    Collections.sort(annualizedForAllStocks, new sortByDecreasingAnnualReturn());
    return annualizedForAllStocks;
  }

  // TODO: CRIO_TASK_MODULE_CALCULATIONS
  // Return the populated list of AnnualizedReturn for all stocks.
  // Annualized returns should be calculated in two steps:
  // 1. Calculate totalReturn = (sell_value - buy_value) / buy_value.
  // 1.1 Store the same as totalReturns
  // 2. Calculate extrapolated annualized returns by scaling the same in years
  // span.
  // The formula is:
  // annualized_returns = (1 + total_returns) ^ (1 / total_num_years) - 1
  // 2.1 Store the same as annualized_returns
  // Test the same using below specified command. The build should be successful.
  // ./gradlew test --tests
  // PortfolioManagerApplicationTest.testCalculateAnnualizedReturn

  public static AnnualizedReturn calculateAnnualizedReturns(LocalDate endDate, PortfolioTrade trade, Double buyPrice,
      Double sellPrice) {
    Double totalReturns = (sellPrice - buyPrice) / buyPrice;
    double noOfDaysBetween = ChronoUnit.DAYS.between(trade.getPurchaseDate(), endDate);
    double total_num_years = noOfDaysBetween / 365.0;
    Double annualized_returns = Math.pow((1 + totalReturns), (double) (1 / total_num_years)) - 1;

    return new AnnualizedReturn(trade.getSymbol(), annualized_returns, totalReturns);
  }




  // TODO:
  // After refactor, make sure that the tests pass by using these two commands
  // ./gradlew test --tests PortfolioManagerApplicationTest.readTradesFromJson
  // ./gradlew test --tests PortfolioManagerApplicationTest.mainReadFile
  public static List<PortfolioTrade> readTradesFromJson(String filename) throws IOException, URISyntaxException {
    ObjectMapper om = getObjectMapper();
    File file = resolveFileFromResources(filename);
    List<PortfolioTrade> trade = om.readValue(file, new TypeReference<List<PortfolioTrade>>() {
    });
    return trade;
  }

  public static String getToken() {
    String token = "209ac85df2915ec7ab39b5540baebb2eda5db14c";
    return token;
  }

  // TODO:
  // Build the Url using given parameters and use this function in your code to
  // cann the API.
  public static String prepareUrl(PortfolioTrade trade, LocalDate endDate, String token) {
    String url = "https://api.tiingo.com/tiingo/daily/" + trade.getSymbol() + "/prices" + "?" + "startDate="
        + trade.getPurchaseDate() + "&endDate=" + endDate + "&token=" + token;
    return url;
  }

  // TODO: CRIO_TASK_MODULE_REFACTOR
  //  Once you are done with the implementation inside PortfolioManagerImpl and
  //  PortfolioManagerFactory, create PortfolioManager using PortfolioManagerFactory.
  //  Refer to the code from previous modules to get the List<PortfolioTrades> and endDate, and
  //  call the newly implemented method in PortfolioManager to calculate the annualized returns.

  // Note:
  // Remember to confirm that you are getting same results for annualized returns as in Module 3.
  public static String readFileAsString(String fileName) throws IOException, URISyntaxException {
    return new String(Files.readAllBytes(resolveFileFromResources(fileName).toPath()));

  }
  public static List<AnnualizedReturn> mainCalculateReturnsAfterRefactor(String[] args)
      throws Exception {
       String file = args[0];
       LocalDate endDate = LocalDate.parse(args[1]);
       String contents = readFileAsString(file);
       ObjectMapper objectMapper = getObjectMapper();
       PortfolioTrade[] portfolioTrades = objectMapper.readValue(contents,PortfolioTrade[].class);
       PortfolioManager portfolioManager = PortfolioManagerFactory.getPortfolioManager(new RestTemplate());
       return portfolioManager.calculateAnnualizedReturn(Arrays.asList(portfolioTrades), endDate);
  }


  public static void main(String[] args) throws Exception {
    Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler());
    ThreadContext.put("runId", UUID.randomUUID().toString());

    // printJsonObject(mainReadQuotes(args));
    printJsonObject(mainCalculateReturnsAfterRefactor(args));

    

  }
}

class sortByClosingPrice implements Comparator<TotalReturnsDto> {

  @Override
  public int compare(TotalReturnsDto arg0, TotalReturnsDto arg1) {
    // TODO Auto-generated method stub
    if (arg0.getClosingPrice() > arg1.getClosingPrice()) {
      return 1;
    }
    if (arg0.getClosingPrice() < arg1.getClosingPrice()) {
      return -1;
    }
    if (arg0.getClosingPrice() == arg1.getClosingPrice()) {
      return 0;
    }
    return 0;
  }


}

class sortByDecreasingAnnualReturn implements Comparator<AnnualizedReturn> {

  @Override
  public int compare(AnnualizedReturn arg0, AnnualizedReturn arg1) {
    // TODO Auto-generated method stub
    if (arg0.getAnnualizedReturn() > arg1.getAnnualizedReturn()) {
      return -1;
    }
    if (arg0.getAnnualizedReturn() < arg1.getAnnualizedReturn()) {
      return 1;
    }
    if (arg0.getAnnualizedReturn() == arg1.getAnnualizedReturn()) {
      return 0;
    }
    return 0;
  }
}

