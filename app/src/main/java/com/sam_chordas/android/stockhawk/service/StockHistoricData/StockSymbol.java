package com.sam_chordas.android.stockhawk.service.StockHistoricData;

public class StockSymbol {

    private String mDate;
    private float mClose;

    public StockSymbol(String date, float close){
        mDate = date;
        mClose = close;
    }

    public String getDate() {
        return mDate;
    }

    public float getClose() {
        return mClose;
    }
}