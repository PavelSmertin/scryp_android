package com.start.crypto.android.api;

import com.start.crypto.android.api.model.Auth;
import com.start.crypto.android.api.model.JWTResponse;
import com.start.crypto.android.api.model.Portfolio;

import java.util.HashMap;
import java.util.List;

import io.reactivex.Observable;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface MainApiService {

  @POST("user_token")
  Observable<JWTResponse> signin(
          @Body Auth auth
  );

  @POST("users")
  Observable<HashMap<String, HashMap<String, Double>>> signup(
          @Query("user[email]") String email,
          @Query("user[password]") String password,
          @Query("user[password_confirmation]") String password_confirmation
  );

  @GET("portfolios.json")
  Observable<List<Portfolio>> portfolios();

  @POST("portfolios.json")
  Observable<Object> pushPortfolio(
          @Query("portfolio[user_name]") String userName,
          @Query("portfolio[coins_count]") int coinsCount,
          @Query("portfolio[profit_24h]") double profit24h,
          @Query("portfolio[profit_7d]") double profit7d

  );

}