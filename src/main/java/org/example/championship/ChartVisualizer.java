package org.example.championship;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;
import javax.swing.*;
import java.awt.*;
import java.util.Map;
import java.util.stream.Collectors;

public class ChartVisualizer extends JFrame {
    private ChampionshipResolver resolver;

    public ChartVisualizer(ChampionshipResolver resolver) {
        this.resolver = resolver;
        initializeUI();
    }

    private void initializeUI() {
        setTitle("Топ-10 команд по суммарной трансферной стоимости");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 600);
        setLocationRelativeTo(null);

        CategoryDataset dataset = createDataset();
        JFreeChart barChart = ChartFactory.createBarChart(
                "Топ-10 команд по суммарной трансферной стоимости",
                "Команды",
                "Суммарная трансферная стоимость",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(barChart);
        chartPanel.setPreferredSize(new Dimension(900, 500));
        setContentPane(chartPanel);
    }

    private CategoryDataset createDataset() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        Map<String, Long> teamTransferCosts = resolver.getPlayers().stream()
                .collect(Collectors.groupingBy(
                        Player::getTeam,
                        Collectors.summingLong(Player::getTransferCost)
                ));

        teamTransferCosts.entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10)
                .forEach(entry -> dataset.addValue(
                        entry.getValue(),
                        "Суммарная стоимость",
                        entry.getKey()
                ));

        return dataset;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                e.printStackTrace();
            }

            ChampionshipResolver resolver = new ChampionshipResolver("fakePlayers.csv");
            new ChartVisualizer(resolver).setVisible(true);
        });
    }
}
