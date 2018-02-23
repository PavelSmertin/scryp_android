package com.start.crypto.android.api;

import com.start.crypto.android.api.model.CoinsResponse;
import com.start.crypto.android.api.model.PriceMultiFullResponse;

import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface CriptoCompaireMinApiService {

  @GET("pricehistorical")
  Observable<HashMap<String, HashMap<String, Double>>> pricesHistorical(
          @Query("fsym") String fromSymbol,
          @Query("tsyms") String toSymbols,
          @Query("ts") String timestamp,
          @Query("markets") String markets
  );

  @GET("price")
  Observable<HashMap<String, Double>> prices(
          @Query("fsym") String fromSymbol,
          @Query("tsyms") String toSymbols,
          @Query("e") String market
  );

  @GET("pricemulti")
  Observable<HashMap<String, HashMap<String, Double>>> priceMulti(
          @Query("fsyms") String fromSymbol,
          @Query("tsyms") String toSymbols,
          @Query("e") String market
  );

  @GET("pricemultifull")
  Observable<PriceMultiFullResponse> priceMultiFull(
          @Query("fsyms") String fromSymbol,
          @Query("tsyms") String toSymbols,
          @Query("e") String market
  );

  @GET("all/coinlist")
  Observable<CoinsResponse> coins();
}