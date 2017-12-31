package com.start.crypto.android.api;

import com.start.crypto.android.api.model.Auth;
import com.start.crypto.android.api.model.JWTResponse;
import com.start.crypto.android.api.model.Portfolio;
import com.start.crypto.android.api.model.RestoreResponse;

import java.util.List;

import io.reactivex.Observable;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface MainApiService {

  @POST("user_token")
  Observable<JWTResponse> signin(
          @Body Auth auth
  );

  @POST("users")
  Observable<JWTResponse> signup(
          @Query("user[email]") String email,
          @Query("user[password]") String password,
          @Query("user[password_confirmation]") String password_confirmation
  );

  @POST("password_resets")
  Observable<RestoreResponse> restoreRequest(
          @Query("password_reset[email]") String email
  );

  @PUT("password_resets/{id}")
  Observable<JWTResponse> restorePassword(
          @Path("id") String resource,
          @Query("password_reset[code]") String email,
          @Query("password_reset[password]") String password,
          @Query("password_reset[password_confirmation]") String passwordConfirmation
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


  @GET("sync")
  Observable<ResponseBody> syncDownload();

  @POST("sync")
  Call<Object> syncUpload(
          @Query("data") String userName
  );

}