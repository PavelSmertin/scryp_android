package com.start.crypto.android.api;

import com.start.crypto.android.api.model.Auth;
import com.start.crypto.android.api.model.JWTResponse;

import java.util.HashMap;

import io.reactivex.Observable;
import retrofit2.http.Body;
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

}