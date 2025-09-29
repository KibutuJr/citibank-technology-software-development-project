package stockmonitorapp;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Stage;
import yahoofinance.Stock;
import yahoofinance.YahooFinance;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.Queue;

public class App extends Application {

    private static final Queue<XYChart.Data<Number, Number>> stockQueue = new LinkedList<>();
    private XYChart.Series<Number, Number> series;
    private int timeCounter = 0;

    @Override
    public void start(Stage primaryStage) {
        NumberAxis xAxis = new NumberAxis();
        xAxis.setLabel("Time (seconds)");

        NumberAxis yAxis = new NumberAxis();
        yAxis.setLabel("Stock Price");

        LineChart<Number, Number> lineChart = new LineChart<>(xAxis, yAxis);
        lineChart.setTitle("Dow Jones Industrial Average (Live)");

        series = new XYChart.Series<>();
        series.setName("DJIA");

        lineChart.getData().add(series);

        Scene scene = new Scene(lineChart, 800, 600);
        primaryStage.setScene(scene);
        primaryStage.setTitle("Citi Stock Monitor");
        primaryStage.show();

        // Start fetching stock prices every 5 seconds
        Thread fetchThread = new Thread(this::fetchStockPrices);
        fetchThread.setDaemon(true);
        fetchThread.start();
    }

    private void fetchStockPrices() {
        String symbol = "^DJI";

        while (true) {
            try {
                Stock stock = YahooFinance.get(symbol);
                BigDecimal price = stock.getQuote().getPrice();
                timeCounter += 5; // seconds

                XYChart.Data<Number, Number> dataPoint = new XYChart.Data<>(timeCounter, price.doubleValue());
                stockQueue.add(dataPoint);

                // Update the chart on the JavaFX Application Thread
                Platform.runLater(() -> {
                    series.getData().clear();
                    series.getData().addAll(stockQueue);
                });

                Thread.sleep(5000);
            } catch (IOException e) {
                System.out.println("Failed to fetch stock data: " + e.getMessage());
            } catch (InterruptedException e) {
                e.printStackTrace();
                break;
            }
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}
