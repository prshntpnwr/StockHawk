package com.sam_chordas.android.stockhawk.ui;

import android.graphics.Color;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.db.chart.Tools;
import com.db.chart.model.LineSet;
import com.db.chart.view.AxisController;
import com.db.chart.view.ChartView;
import com.db.chart.view.LineChartView;
import com.sam_chordas.android.stockhawk.R;
import com.sam_chordas.android.stockhawk.data.QuoteColumns;
import com.sam_chordas.android.stockhawk.rest.Utils;
import com.sam_chordas.android.stockhawk.service.StockHistoricData.StockHistoricData;
import com.sam_chordas.android.stockhawk.service.StockHistoricData.StockMeta;
import com.sam_chordas.android.stockhawk.service.StockHistoricData.StockSymbol;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class StockDetailActivity extends AppCompatActivity implements StockHistoricData.HistoricCallback {

    StockHistoricData historicData;
    ArrayList<StockSymbol> stockSymbols;

    @BindView(R.id.linechart)
    LineChartView lineChart;
    @BindView(R.id.stock_detail_name)
    TextView stockName;
    @BindView(R.id.stock_detail_symbol)
    TextView stockSymbol;
    @BindView(R.id.stock_detail_first)
    TextView firstTrade;
    @BindView(R.id.stock_detail_last)
    TextView lastTrade;
    @BindView(R.id.stock_detail_currency)
    TextView currency;
    @BindView(R.id.stock_detail_bid)
    TextView tvBidPrice;
    @BindView(R.id.detail_empty_view)
    TextView emptyView;
    @BindView(R.id.line_graph_layout)
    LinearLayout linearLayout;

    String symbol;
    String bidPrice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_line_graph);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);

        //Getting Values from intents
        symbol = getIntent().getStringExtra(QuoteColumns.SYMBOL);
        bidPrice = getIntent().getStringExtra(QuoteColumns.BIDPRICE);

        //Setting values to the text
        stockSymbol.setText(symbol);
        stockSymbol.setContentDescription(stockSymbol.getText());

        tvBidPrice.setText(bidPrice);
        tvBidPrice.setContentDescription(stockSymbol.getText());

        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(true);
            bar.setDisplayShowHomeEnabled(true);
            bar.setTitle(String.format(getString(R.string.symbol_detail), symbol));
        }

        historicData = new StockHistoricData(this, this);

        if (Utils.isNetworkAvailable(this)) {
            historicData.getHistoricData(symbol);
        } else {
            historicData.setHistoricalDataStatus(StockHistoricData.STATUS_NO_INTERNET);
            onFailure();
        }
    }

    @Override
    public void onSuccess(StockMeta stockMeta) {

        ButterKnife.bind(this);
        emptyView.setVisibility(View.GONE);

        stockName.setText(stockMeta.getStockName());
        firstTrade.setText(Utils.convertDate(stockMeta.getFirstTrade()));
        lastTrade.setText(Utils.convertDate(stockMeta.getLastTrade()));
        currency.setText(stockMeta.getCurrency());

        LineSet dataset = new LineSet();
        ArrayList<StockSymbol> stockSymbols = stockMeta.getStockSymbols();

        int minValue = 0;
        int maxValue = 0;

        for (int i = 0; i < stockSymbols.size(); i++) {
            String label = Utils.convertDate(stockSymbols.get(i).getDate());
            float value = stockSymbols.get(i).getClose();
            if (value > maxValue) {
                maxValue = (int) value;
            }
            dataset.addPoint(label, value);
        }

        dataset.setColor(Color.parseColor("#758cbb"))
                .setThickness(5)
                .setSmooth(true)
                .beginAt(1);
        Paint thresPaint = new Paint();
        thresPaint.setColor(Color.parseColor("#0079ae"));
        thresPaint.setStyle(Paint.Style.STROKE);
        thresPaint.setAntiAlias(true);
        thresPaint.setStrokeWidth(Tools.fromDpToPx(.75f));
        thresPaint.setPathEffect(new DashPathEffect(new float[]{10, 10}, 0));

        Paint gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStyle(Paint.Style.STROKE);
        gridPaint.setAntiAlias(true);
        gridPaint.setStrokeWidth(Tools.fromDpToPx(.75f));

        lineChart.addData(dataset);

        lineChart.setAxisThickness(3)
                .setAxisColor(Color.GRAY)
                .setAxisBorderValues(minValue, maxValue * 2)
                .setXLabels(AxisController.LabelPosition.NONE)
                .setYLabels(AxisController.LabelPosition.NONE)
                .setXAxis(false)
                .setYAxis(false)
                .setGrid(ChartView.GridType.FULL, 5, 5, gridPaint)
                .setValueThreshold(80f, 80f, thresPaint);

        lineChart.show();
    }

    @Override
    public void onFailure() {

        ButterKnife.bind(this);
        emptyView.setVisibility(View.VISIBLE);
        String errorMessage = "";

        @StockHistoricData.HistoricalDataStatuses
        int status = PreferenceManager.getDefaultSharedPreferences(this)
                .getInt(getString(R.string.pref_historic_status_key), -1);

        switch (status) {
            case StockHistoricData.STATUS_SERVER_INVALID:
                errorMessage += getString(R.string.error_json);
                break;
            case StockHistoricData.STATUS_NO_INTERNET:
                errorMessage += getString(R.string.no_internet);
                break;
            case StockHistoricData.STATUS_SERVER_DOWN:
                errorMessage += getString(R.string.server_down);
                break;
            case StockHistoricData.STATUS_OK:
                errorMessage += getString(R.string.no_error);
                break;
            default:
                break;
        }

        emptyView.setText(errorMessage);
        emptyView.setContentDescription(emptyView.getText());

        final Snackbar snackbar = Snackbar
                .make(linearLayout, getString(R.string.no_data) + errorMessage, Snackbar.LENGTH_INDEFINITE)
                .setAction(R.string.retry, new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        historicData.getHistoricData(symbol);
                    }
                })
                .setActionTextColor(Color.WHITE);

        View subview = snackbar.getView();
        TextView tv = (TextView) subview.findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);
        snackbar.show();
    }
}