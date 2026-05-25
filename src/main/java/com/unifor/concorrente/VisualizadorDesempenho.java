package com.unifor.concorrente;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;

public class VisualizadorDesempenho extends JFrame {

    public VisualizadorDesempenho(String csvFilePath) {
        setTitle("CCT - Ciência da Computação UNIFOR - Desempenho");
        setSize(900, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        DefaultCategoryDataset dataset = carregarDadosCSV(csvFilePath);

        JFreeChart lineChart = ChartFactory.createLineChart(
                "Análise Comparativa de Tempo Médio (ms)",
                "Volume do Conjunto de Dados",
                "Tempo de Execução (ms)",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        ChartPanel chartPanel = new ChartPanel(lineChart);
        chartPanel.setPreferredSize(new Dimension(860, 540));
        setContentPane(chartPanel);
    }

    private DefaultCategoryDataset carregarDadosCSV(String path) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        Map<String, long[]> agrupaMedias = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(path))) {
            String linha = br.readLine(); // Pular cabeçalho
            while ((linha = br.readLine()) != null) {
                String[] cols = linha.split(",");
                if (cols.length < 6) continue;

                String tamanho = cols[1];
                String metodo = cols[2];
                long tempo = Long.parseLong(cols[5]);

                String chave = tamanho + ";" + metodo;
                if (!agrupaMedias.containsKey(chave)) {
                    agrupaMedias.put(chave, new long[]{0, 0});
                }
                long[] dados = agrupaMedias.get(chave);
                dados[0] += tempo; // Acumulador de tempo
                dados[1] += 1;     // Contador de amostras
            }

            for (Map.Entry<String, long[]> entry : agrupaMedias.entrySet()) {
                String[] partes = entry.getKey().split(";");
                String tamanho = partes[0];
                String metodo = partes[1];
                long[] dados = entry.getValue();
                double media = (double) dados[0] / dados[1];

                dataset.addValue(media, metodo, tamanho);
            }
        } catch (Exception e) {
            System.err.println("Erro ao processar CSV: " + e.getMessage());
        }
        return dataset;
    }
}
