package io.digibyte.presenter.activities.util;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;

import io.digibyte.presenter.activities.models.AddressInfo;
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
    private Retrofit api;

    private RetrofitManager() {
        api = new Retrofit.Builder()
                .baseUrl("https://api.digiassets.net:443/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
    }

    private interface AssetEndpoints {
        @GET("addressinfo/{address}")
        Call<AddressInfo> getAssets(@Path("address") String address);

        @GET("assetmetadata/{assetid}/{utxotxid}:{index}")
        Call<MetaModel> getMeta(@Path("assetid") String assetid, @Path("utxotxid") String utxotxid,
                @Path("index") String index);

        @POST("sendasset/")
        @Headers({"cache-control: no-cache", "Content-Type: application/json"})
        Call<ResponseBody> sendAsset(@Body RequestBody body);

        @POST("broadcast/")
        Call<ResponseBody> broadcastTx(@Body RequestBody body);
    }

    public interface AssetsCallback {
        void assetsRetrieved(AddressInfo addressAssets);
    }

    public void getAssets(String address, AssetsCallback assetsCallback) {
        AssetEndpoints apiService = api.create(AssetEndpoints.class);
        Call<AddressInfo> call = apiService.getAssets(address);
        call.enqueue(new Callback<AddressInfo>() {
            @Override
            public void onResponse(Call<AddressInfo> call, Response<AddressInfo> response) {
                Log.d(RetrofitManager.class.getSimpleName(), "Assets retrieved for address: ");
                assetsCallback.assetsRetrieved(response.body());
            }

            @Override
            public void onFailure(Call<AddressInfo> call, Throwable t) {
                Log.d(RetrofitManager.class.getSimpleName(), "Error Retrieving Asset or no assets");
                t.printStackTrace();
            }
        });
    }

    public interface MetaCallback {
        void metaRetrieved(MetaModel metalModel);
    }

    public void getAssetMeta(String assetid, String utxotdid, String index,
            MetaCallback metaCallback) {
        AssetEndpoints apiService = api.create(AssetEndpoints.class);
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
        void success(SendAssetResponse sendAssetResponse);

        void error(String message);
    }

    public void sendAsset(String sendAsset, SendAssetCallback sendAssetCallback) {
        AssetEndpoints apiService = api.create(AssetEndpoints.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), sendAsset);
        Call<ResponseBody> call = apiService.sendAsset(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.code() != 200) {
                        try {
                            JSONObject error = new JSONObject(response.errorBody().string());
                            String message = error.getString("message");
                            Log.d(RetrofitManager.class.getSimpleName(),
                                    "Send Asset Error: " + message);
                            sendAssetCallback.error(message);
                        } catch (JSONException e) {
                            sendAssetCallback.error("");
                        }
                    } else {
                        Gson gson = new Gson();
                        Type listType = new TypeToken<SendAssetResponse>() {
                        }.getType();
                        SendAssetResponse sendAssetResponse = gson.fromJson(
                                response.body().string(), listType);
                        sendAssetCallback.success(sendAssetResponse);
                    }
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

    public void broadcast(String transactionHex, BroadcastTransaction broadcastTransaction) {
        AssetEndpoints apiService = instance.api.create(AssetEndpoints.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), transactionHex);
        Call<ResponseBody> call = apiService.broadcastTx(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(RetrofitManager.class.getSimpleName(), "Status Code: " + response.code());
                Log.d(RetrofitManager.class.getSimpleName(),
                        "Status Message: " + response.message());
                //broadcastTransaction.response(response.body());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}