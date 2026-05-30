package com.unifor.concorrente;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class BenchmarkMain {

    // Resultados gerados em CSV dentro da pasta resultados
    private static final String OUTPUT_DIR = "resultados";
    private static final String CSV_FILE = OUTPUT_DIR + "/resultados_benchmark.csv";

    private static final String PALAVRA_ALVO = "the";

    public static void main(String[] args) {
        try {
            System.out.println("=== SISTEMA DE BENCHMARK CORE AUTOMATIZADO (LIVROS REAIS) ===");

            File dir = new File(OUTPUT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Diretório '" + OUTPUT_DIR + "' criado com sucesso para o CSV.");
            }

            String[] arquivos = {"Dracula.txt", "MobyDick.txt", "DonQuixote.txt"};
            String[] nomesTamanhos = {"01. Dracula (890KB)", "02. Moby Dick (1.2MB)", "03. Don Quixote (2.12MB)"};

            FileWriter csvWriter = new FileWriter(CSV_FILE);
            csvWriter.append("Arquivo,Tamanho,Metodo,Amostra,Contagem,TempoMS\n");

            for (int i = 0; i < arquivos.length; i++) {
                String caminho = arquivos[i];
                String tamanhoNome = nomesTamanhos[i];

                // Verifica se os arquivos dos livros realmente estão na raiz do projeto
                File fileCheck = new File(caminho);
                if (!fileCheck.exists()) {
                    System.err.println("[ERRO CRÍTICO] O arquivo " + caminho + " não foi encontrado na raiz do projeto!");
                    continue;
                }

                System.out.println("\n--------------------------------------------------");
                System.out.println("Processando base de dados: " + tamanhoNome);

                String texto = new String(Files.readAllBytes(Paths.get(caminho)));
                String[] palavras = texto.split("[^a-zA-Z0-9]+");

                // 1. Execução de Busca Serial
                for (int amostra = 1; amostra <= 3; amostra++) {
                    long inicio = System.currentTimeMillis();
                    int contagem = BuscadorSerial.contar(palavras, PALAVRA_ALVO);
                    long fim = System.currentTimeMillis();
                    long tempo = fim - inicio;
                    System.out.printf("[Serial] Amostra %d -> %d termos em %d ms\n", amostra, contagem, tempo);
                    csvWriter.append(String.format("%s,%s,Serial,%d,%d,%d\n", caminho, tamanhoNome, amostra, contagem, tempo));
                }

                // 2. Execução Parallel CPU variando Núcleos (2, 4 e 8 Threads)
                int[] coresConfig = {2, 4, 8};
                for (int threads : coresConfig) {
                    for (int amostra = 1; amostra <= 3; amostra++) {
                        long inicio = System.currentTimeMillis();
                        int contagem = BuscadorParaleloCPU.contar(palavras, PALAVRA_ALVO, threads);
                        long fim = System.currentTimeMillis();
                        long tempo = fim - inicio;
                        System.out.printf("[Parallel CPU - %d Threads] Amostra %d -> %d termos em %d ms\n", threads, amostra, contagem, tempo);
                        csvWriter.append(String.format("%s,%s,CPU_%d_Threads,%d,%d,%d\n", caminho, tamanhoNome, threads, amostra, contagem, tempo));
                    }
                }

                // 3. Execução Parallel GPU (OpenCL)
                for (int amostra = 1; amostra <= 3; amostra++) {
                    long inicio = System.currentTimeMillis();
                    int contagem = 0;
                    try {
                        contagem = BuscadorParaleloGPU.contar(texto, PALAVRA_ALVO);
                    } catch (Exception e) {
                        System.out.println("[ALERTA GPU] Falha ao invocar OpenCL: " + e.getMessage());
                    }
                    long fim = System.currentTimeMillis();
                    long tempo = fim - inicio;
                    System.out.printf("[Parallel GPU - OpenCL] Amostra %d -> %d termos em %d ms\n", amostra, contagem, tempo);
                    csvWriter.append(String.format("%s,%s,GPU_OpenCL,%d,%d,%d\n", caminho, tamanhoNome, amostra, contagem, tempo));
                }
            }

            csvWriter.flush();
            csvWriter.close();
            System.out.println("\n[SUCESSO] Base estatística exportada para: " + CSV_FILE);

            javax.swing.SwingUtilities.invokeLater(() -> {
                VisualizadorGraficos appGrafico = new VisualizadorGraficos(CSV_FILE);
                appGrafico.setVisible(true);
            });

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}