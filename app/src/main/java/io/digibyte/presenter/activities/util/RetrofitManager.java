package io.digibyte.presenter.activities.util;

import android.util.Log;

import io.digibyte.presenter.activities.models.AddressAssets;
import io.digibyte.presenter.activities.models.MetaModel;
import io.digibyte.presenter.activities.models.SendAsset;
import io.digibyte.presenter.activities.models.SendAssetResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;

public class RetrofitManager {
    public static RetrofitManager instance = new RetrofitManager();
    private Retrofit retrofit;

    private RetrofitManager() {
        retrofit = new Retrofit.Builder()
                .baseUrl("http://api.digiassets.net/")
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
        Call<SendAssetResponse> sendAsset(@Body SendAsset body);
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

    public void sendAsset(SendAsset sendAsset, SendAssetCallback sendAssetCallback) {
        AssetEndpoints apiService = instance.retrofit.create(AssetEndpoints.class);
        Call<SendAssetResponse> call = apiService.sendAsset(sendAsset);
        call.enqueue(new Callback<SendAssetResponse>() {
            @Override
            public void onResponse(Call<SendAssetResponse> call,
                    Response<SendAssetResponse> response) {
                sendAssetCallback.response(response.body());
            }

            @Override
            public void onFailure(Call<SendAssetResponse> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}