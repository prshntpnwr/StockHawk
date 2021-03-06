package com.sam_chordas.android.stockhawk.rest;

import android.content.ContentProviderOperation;
import android.content.Context;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.preference.PreferenceManager;
import android.test.suitebuilder.annotation.Suppress;
import android.util.Log;
import android.widget.Toast;

import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.data.QuoteProvider;
import com.sam_chordas.android.stockhawk.service.StockTaskService;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class Utils {

    private static String LOG_TAG = Utils.class.getSimpleName();

    public static boolean showPercent = true;

    public static ArrayList quoteJsonToContentVals(String JSON, Context context) {

        ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();
        JSONObject jsonObject = null;
        JSONArray resultsArray = null;

        try {
            jsonObject = new JSONObject(JSON);

            if (jsonObject != null && jsonObject.length() != 0) {
                jsonObject = jsonObject.getJSONObject("query");
                int count = Integer.parseInt(jsonObject.getString("count"));

                if (count == 1) {
                    jsonObject = jsonObject.getJSONObject("results")
                            .getJSONObject("quote");
                    //batchOperations.add(buildBatchOperation(jsonObject));
                    // if stock is invalid
                    if (jsonObject.getString("Bid").equals("null") || jsonObject.getString("Bid") == null) {
                        //Toast.makeText(context, context.getString(R.string.invalid_stock_symbol), Toast.LENGTH_LONG).show();
                        return null;
                    } else {
                        batchOperations.add(buildBatchOperation(jsonObject));
                    }

                } else {
                    resultsArray = jsonObject.getJSONObject("results").getJSONArray("quote");

                    if (resultsArray != null && resultsArray.length() != 0) {
                        for (int i = 0; i < resultsArray.length(); i++) {
                            jsonObject = resultsArray.getJSONObject(i);
                            batchOperations.add(buildBatchOperation(jsonObject));
                        }
                    }
                }
            }
            setStockStatus(context, StockTaskService.STOCK_STATUS_OK);

        } catch (JSONException e) {
            Log.e(LOG_TAG, "String to JSON failed: " + e);
            setStockStatus(context, StockTaskService.STOCK_STATUS_SERVER_INVALID);
        }
        return batchOperations;
    }

    public static String truncateBidPrice(String bidPrice) {
        try {
            bidPrice = String.format("%.2f", Float.parseFloat(bidPrice));
        }
        catch (NumberFormatException e){
            e.printStackTrace();
        }
        return bidPrice;
    }

    public static String truncateChange(String change, boolean isPercentChange) {
        try {
            String weight = change.substring(0, 1);
            String ampersand = "";

            if (isPercentChange) {
                ampersand = change.substring(change.length() - 1, change.length());
                change = change.substring(0, change.length() - 1);
            }
            change = change.substring(1, change.length());
            double round = (double) Math.round(Double.parseDouble(change) * 100) / 100;
            change = String.format("%.2f", round);
            StringBuffer changeBuffer = new StringBuffer(change);
            changeBuffer.insert(0, weight);
            changeBuffer.append(ampersand);
            change = changeBuffer.toString();
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return change;
    }

    public static ContentProviderOperation buildBatchOperation(JSONObject jsonObject) {
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                QuoteProvider.Quotes.CONTENT_URI);
        try {
            String change = jsonObject.getString("Change");
            builder.withValue(QuoteColumns.SYMBOL, jsonObject.getString("symbol"));
            builder.withValue(QuoteColumns.BIDPRICE, truncateBidPrice(jsonObject.getString("Bid")));
            builder.withValue(QuoteColumns.PERCENT_CHANGE, truncateChange(
                    jsonObject.getString("ChangeinPercent"), true));
            builder.withValue(QuoteColumns.CHANGE, truncateChange(change, false));
            builder.withValue(QuoteColumns.ISCURRENT, 1);
            if (change.charAt(0) == '-') {
                builder.withValue(QuoteColumns.ISUP, 0);
            } else {
                builder.withValue(QuoteColumns.ISUP, 1);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return builder.build();
    }

    static public void setStockStatus(Context c, @StockTaskService.StockStatus int stockStatus) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor spe = sp.edit();
        spe.putInt(c.getString(R.string.pref_stock_status_key), stockStatus);
        spe.commit();
    }

    @SuppressWarnings("ResourceType")
    static public
    @StockTaskService.StockStatus
    int getStockStatus(Context c) {

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        return sp.getInt(c.getString(R.string.pref_stock_status_key), StockTaskService.STOCK_STATUS_UNKNOWN);
    }

    public static String convertDate(String inputDate) {
        StringBuilder outputFormattedDate = new StringBuilder();
        outputFormattedDate.append(inputDate.substring(6))
                .append("/")
                .append(inputDate.substring(4, 6))
                .append("/")
                .append(inputDate.substring(2, 4));
        return outputFormattedDate.toString();
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager connectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        return activeNetwork != null &&
                activeNetwork.isConnectedOrConnecting();
    }
}
