package com.start.crypto.android.api;

import com.start.crypto.android.api.model.CoinsResponse;

import io.reactivex.Observable;
import retrofit2.http.GET;

public interface CriptoCompaireService {

  @GET("all/coinlist")
  Observable<CoinsResponse> coins();
}