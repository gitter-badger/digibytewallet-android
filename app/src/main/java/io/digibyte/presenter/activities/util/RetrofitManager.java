package io.digibyte.presenter.activities.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;

import io.digibyte.presenter.activities.models.AddressAssets;
import io.digibyte.presenter.activities.models.AssetTxModel;
import io.digibyte.presenter.activities.models.MetaModel;
import io.digibyte.presenter.activities.models.SendAssetResponse;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class RetrofitManager {
    public static RetrofitManager instance = new RetrofitManager();
    private Retrofit retrofit;

    private RetrofitManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl("https://api.digiassets.net:443/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private interface AssetEndpoints {
        @GET("addressinfo/{address}")
        Call<AddressAssets> getAssets(@Path("address") String address);

        @GET("assetmetadata/{assetid}/{utxotxid}:{index}")
        Call<MetaModel> getMeta(@Path("assetid") String assetid, @Path("utxotxid") String utxotxid,
                @Path("index") String index);

        @POST("sendasset/")
        @Headers({"cache-control: no-cache", "Content-Type: application/json"})
        Call<ResponseBody> sendAsset(@Body RequestBody body);

        @POST("broadcast/")
        Call<String> broadcastTx(@Body AssetTxModel body);
    }

    public interface AssetsCallback {
        void assetsRetrieved(AddressAssets addressAssets);
    }

    public void getAssets(String address, AssetsCallback assetsCallback) {
        AssetEndpoints apiService = instance.retrofit.create(AssetEndpoints.class);
        Call<AddressAssets> call = apiService.getAssets(address);
        call.enqueue(new Callback<AddressAssets>() {
            @Override
            public void onResponse(Call<AddressAssets> call, Response<AddressAssets> response) {
                Log.d(RetrofitManager.class.getSimpleName(), "Assets retrieved for address: ");
                assetsCallback.assetsRetrieved(response.body());
            }

            @Override
            public void onFailure(Call<AddressAssets> call, Throwable t) {
                Log.d(RetrofitManager.class.getSimpleName(), "Error Retrieving Asset");
                t.printStackTrace();
            }
        });
    }

    public interface MetaCallback {
        void metaRetrieved(MetaModel metalModel);
    }

    public void getAssetMeta(String assetid, String utxotdid, String index,
            MetaCallback metaCallback) {
        AssetEndpoints apiService = instance.retrofit.create(AssetEndpoints.class);
        Call<MetaModel> call = apiService.getMeta(assetid, utxotdid, index);
        call.enqueue(new Callback<MetaModel>() {
            @Override
            public void onResponse(Call<MetaModel> call, Response<MetaModel> response) {
                Log.d(RetrofitManager.class.getSimpleName(), "Meta retrieved for address");
                metaCallback.metaRetrieved(response.body());
            }

            @Override
            public void onFailure(Call<MetaModel> call, Throwable t) {
                Log.d(RetrofitManager.class.getSimpleName(), "Error Retrieving Meta");
                t.printStackTrace();
            }
        });
    }

    public interface SendAssetCallback {
        void response(SendAssetResponse sendAssetResponse);
    }

    public void sendAsset(String sendAsset, SendAssetCallback sendAssetCallback) {
        AssetEndpoints apiService = instance.retrofit.create(AssetEndpoints.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), sendAsset);
        Call<ResponseBody> call = apiService.sendAsset(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Gson gson = new Gson();
                Type listType = new TypeToken<SendAssetResponse>() {
                }.getType();
                try {
                    SendAssetResponse sendAssetResponse = gson.fromJson(response.body().string(),
                            listType);
                    sendAssetCallback.response(sendAssetResponse);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    public interface BroadcastTransaction {
        void response(String broadcastResponse);
    }

    public void broadcast(AssetTxModel assetTxModel, BroadcastTransaction broadcastTransaction) {
        AssetEndpoints apiService = instance.retrofit.create(AssetEndpoints.class);
        Call<String> call = apiService.broadcastTx(assetTxModel);
        call.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                broadcastTransaction.response(response.body());
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}