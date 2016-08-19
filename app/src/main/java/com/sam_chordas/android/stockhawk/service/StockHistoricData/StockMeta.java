package com.sam_chordas.android.stockhawk.service.StockHistoricData;

import java.util.ArrayList;

public class StockMeta {

    private String mStockName;
    private String mFirstTrade;
    private String mLastTrade;
    private String mCurrency;
    private double mBidPrice;
    private ArrayList<StockSymbol> mStockSymbols;

    public StockMeta(String stockName, String firstTrade, String lastTrade, String currency,
                     double bidPrice, ArrayList<StockSymbol> stockSymbols) {
        this.mStockName = stockName;
        this.mFirstTrade = firstTrade;
        this.mLastTrade = lastTrade;
        this.mCurrency = currency;
        this.mBidPrice = bidPrice;
        this.mStockSymbols = stockSymbols;
    }

    public String getStockName() {
        return mStockName;
    }

    public String getFirstTrade() {
        return mFirstTrade;
    }

    public String getLastTrade() {
        return mLastTrade;
    }

    public String getCurrency() {
        return mCurrency;
    }

    public double getBidPrice() {
        return mBidPrice;
    }

    public ArrayList<StockSymbol> getStockSymbols() {
        return mStockSymbols;
    }
}