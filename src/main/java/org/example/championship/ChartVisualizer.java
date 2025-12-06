package org.example.championship;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;          // <-- импортируем ChartPanel
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.util.Map;

/**
 * Окно, которое рисует столбчатую диаграмму
 * «Топ‑10 команд по суммарной трансферной стоимости».
 *
 * Конструктор принимает **DataMapper**.
 */
public class ChartVisualizer extends JFrame {

    private final DataMapper mapper;

    /** Поле, которое будем проверять в тесте */
    private ChartPanel chartPanel;   // <-- добавляем поле

    public ChartVisualizer(DataMapper mapper) {
        this.mapper = mapper;
        initUI();
    }

    private void initUI() {
        setTitle("Топ‑10 команд по суммарной трансферной стоимости");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        // Создаём набор данных и диаграмму
        DefaultCategoryDataset dataset = createDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "Топ‑10 команд",
                "Команда",
                "Сумма (в руб.)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        // <-- сохраняем ChartPanel в поле, чтобы тест мог к нему обратиться
        this.chartPanel = new ChartPanel(barChart);
        this.chartPanel.setPreferredSize(new Dimension(900, 500));
        setContentPane(this.chartPanel);
    }

    /** Приватный помощник – создаёт набор данных из mapper‑а */
    private DefaultCategoryDataset createDataset() {
        DefaultCategoryDataset ds = new DefaultCategoryDataset();
        Map<String, Long> data = mapper.getTop10TeamsByTransferCost();
        data.forEach((team, cost) -> ds.addValue(cost, "Сумма", team));
        return ds;
    }

    /** Точка входа – удобно для локального запуска */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ChampionshipResolver resolver = new ChampionshipResolver("src/main/resources/fakePlayers.csv");
            DataMapper mapper = new DataMapper(resolver);
            new ChartVisualizer(mapper).setVisible(true);
        });
    }
}
