package io.digibyte.presenter.activities.utils;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.ArrayMap;
import android.util.Log;

import androidx.annotation.NonNull;

import com.crashlytics.android.Crashlytics;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.digibyte.DigiByte;
import io.digibyte.presenter.activities.models.AddressInfo;
import io.digibyte.presenter.activities.models.MetaModel;
import io.digibyte.presenter.activities.models.SendAssetResponse;
import okhttp3.Cache;
import okhttp3.CacheControl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
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
    private Retrofit assetsApi;
    private Cache cache = new Cache(new File(DigiByte.getContext().getCacheDir(), "assets"), 1024 * 1024 * 10);
    private Handler handler = new Handler(Looper.getMainLooper());

    private RetrofitManager() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.cache(cache);
        builder.addNetworkInterceptor(chain -> {
            okhttp3.Response response = chain.proceed(chain.request());

            CacheControl cacheControl = new CacheControl.Builder()
                    .maxAge(365, TimeUnit.DAYS)
                    .build();

            return response.newBuilder()
                    .removeHeader("Cache-Control")
                    .header("Cache-Control", cacheControl.toString())
                    .build();
        });
        assetsApi = new Retrofit.Builder()
                .baseUrl("https://api.digiassets.net:443/v3/")
                .addConverterFactory(GsonConverterFactory.create())
                .client(builder.build())
                .build();
    }

    public void clearCache(String... addresses) {
        try {
            Iterator<String> urls = cache.urls();
            while (urls.hasNext()) {
                String url = urls.next();
                for (String address : addresses) {
                    if (!TextUtils.isEmpty(address) && url.contains(address)) {
                        urls.remove();
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void clearMetaCache(String assetId) {
        try {
            Iterator<String> urls = cache.urls();
            while (urls.hasNext()) {
                String url = urls.next();
                if (url.contains(assetId)) {
                    urls.remove();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void clearCache() {
        try {
            Iterator<String> urls = cache.urls();
            while (urls.hasNext()) {
                urls.next();
                urls.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
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
        AssetEndpoints apiService = assetsApi.create(AssetEndpoints.class);
        Call<AddressInfo> call = apiService.getAssets(address);
        call.enqueue(new Callback<AddressInfo>() {
            @Override
            public void onResponse(@NonNull Call<AddressInfo> call, @NonNull Response<AddressInfo> response) {
                handler.post(() -> assetsCallback.assetsRetrieved(response.body()));
            }

            @Override
            public void onFailure(@NonNull Call<AddressInfo> call, @NonNull Throwable t) {
                Crashlytics.logException(t);
                handler.post(() -> assetsCallback.assetsRetrieved(null));
            }
        });
    }

    public interface MetaCallback {
        void metaRetrieved(MetaModel metalModel);

        void failure(int statusCode, String meesage);
    }

    public void getAssetMeta(String assetid, String utxotdid, String index,
                             MetaCallback metaCallback) {
        AssetEndpoints apiService = assetsApi.create(AssetEndpoints.class);
        Call<MetaModel> call = apiService.getMeta(assetid, utxotdid, index);
        call.enqueue(new Callback<MetaModel>() {
            @Override
            public void onResponse(@NonNull Call<MetaModel> call, @NonNull Response<MetaModel> response) {
                if (response.body() != null) {
                    metaCallback.metaRetrieved(response.body());
                } else {
                    handler.post(() -> metaCallback.failure(response.code(), response.message()));
                }
            }

            @Override
            public void onFailure(@NonNull Call<MetaModel> call, @NonNull Throwable t) {
                Crashlytics.logException(t);
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        metaCallback.failure(0, "");
                    }
                });
            }
        });
    }

    public interface SendAssetCallback {
        void success(SendAssetResponse sendAssetResponse);

        void error(String message, Throwable throwable);
    }

    public void sendAsset(String sendAsset, SendAssetCallback sendAssetCallback) {
        AssetEndpoints apiService = assetsApi.create(AssetEndpoints.class);
        RequestBody body = RequestBody.create(MediaType.parse("application/json"), sendAsset);
        Call<ResponseBody> call = apiService.sendAsset(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                handler.post(() -> {
                    try {
                        if (response.code() != 200) {
                            try {
                                JSONObject error = new JSONObject(response.errorBody().string());
                                String message = error.getString("message");
                                Log.d(RetrofitManager.class.getSimpleName(),
                                        "Send Asset Error: " + message);
                                sendAssetCallback.error(message, new Exception("non 200 response for: " + call.request().body().toString()));
                            } catch (Exception e) {
                                Crashlytics.logException(e);
                                sendAssetCallback.error("", e);
                            }
                        } else {
                            Gson gson = new Gson();
                            Type listType = new TypeToken<SendAssetResponse>() {
                            }.getType();
                            SendAssetResponse sendAssetResponse = gson.fromJson(response.body().string(), listType);
                            sendAssetCallback.success(sendAssetResponse);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Crashlytics.logException(t);
                handler.post(() -> sendAssetCallback.error("", t));
            }
        });
    }

    public interface BroadcastTransaction {
        void success(String broadcastResponse);

        void onError(String errorMessage);
    }

    public void broadcast(String txHex, BroadcastTransaction broadcastTransaction) {
        Map<String, Object> jsonParams = new ArrayMap<>();
        jsonParams.put("txHex", txHex);
        RequestBody body = RequestBody.create(okhttp3.MediaType.parse("application/json; charset=utf-8"), (new JSONObject(jsonParams)).toString());
        AssetEndpoints assetService = assetsApi.create(AssetEndpoints.class);
        Call<ResponseBody> call = assetService.broadcastTx(body);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                handler.post(() -> {
                    Log.d(RetrofitManager.class.getSimpleName(), "Status Code: " + response.code());
                    Log.d(RetrofitManager.class.getSimpleName(), "Status Message: " + response.message());
                    if (response.code() == 200) {
                        String txId = "";
                        try {
                            txId = response.body().string();
                        } catch (IOException e) {

                        }
                        broadcastTransaction.success(txId);
                    } else {
                        String errorMessage = "";
                        try {
                            errorMessage = response.errorBody().string();
                        } catch (Exception e) {

                        }
                        broadcastTransaction.onError(errorMessage);
                    }
                });
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                handler.post(() -> {
                    broadcastTransaction.onError("");
                    t.printStackTrace();
                });
            }
        });
    }
}