package com.unifor.concorrente;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;

public class MinimalBenchmarkRunner {

    private static final String OUTPUT_DIR = "resultados";
    private static final String CSV_FILE = OUTPUT_DIR + "/resultados_benchmark.csv";
    private static final String PALAVRA_ALVO = "java";

    public static void main(String[] args) {
        try {
            File outDir = new File(OUTPUT_DIR);
            if (!outDir.exists()) outDir.mkdirs();

            // localizar arquivos .txt em amostras/
            Path amostrasDir = Paths.get("amostras");
            if (!Files.exists(amostrasDir) || !Files.isDirectory(amostrasDir)) {
                System.err.println("Diretório amostras/ não encontrado.");
                System.exit(2);
            }

            // listar e ordenar por tamanho decrescente para previsibilidade
            File[] arquivos = amostrasDir.toFile().listFiles((d, name) -> name.toLowerCase().endsWith(".txt"));
            if (arquivos == null || arquivos.length == 0) {
                System.err.println("Nenhum arquivo .txt encontrado em amostras/.");
                System.exit(3);
            }
            Arrays.sort(arquivos, Comparator.comparingLong(File::length).reversed());

            try (BufferedWriter csv = new BufferedWriter(new FileWriter(CSV_FILE))) {
                csv.write("Arquivo,Tamanho,Metodo,Amostra,Contagem,TempoMS\n");

                for (File f : arquivos) {
                    String caminho = f.getName();
                    String tamanhoNome = String.format("%s (%,d bytes)", caminho, f.length());
                    System.out.println("Processando: " + tamanhoNome);

                    String texto = new String(Files.readAllBytes(f.toPath()), StandardCharsets.UTF_8);
                    String[] palavras = texto.split("[^a-zA-Z0-9]+");

                    // Serial
                    for (int amostra = 1; amostra <= 3; amostra++) {
                        long inicio = System.currentTimeMillis();
                        int contagem = BuscadorSerial.contar(palavras, PALAVRA_ALVO);
                        long fim = System.currentTimeMillis();
                        csv.write(String.format("%s,%s,Serial,%d,%d,%d\n", caminho, tamanhoNome, amostra, contagem, (fim - inicio)));
                    }

                    // Parallel CPU (2,4,8)
                    int[] cores = {2, 4, 8};
                    for (int coresNum : cores) {
                        for (int amostra = 1; amostra <= 3; amostra++) {
                            long inicio = System.currentTimeMillis();
                            int contagem = BuscadorParaleloCPU.contar(palavras, PALAVRA_ALVO, coresNum);
                            long fim = System.currentTimeMillis();
                            csv.write(String.format("%s,%s,CPU_%d_Threads,%d,%d,%d\n", caminho, tamanhoNome, coresNum, amostra, contagem, (fim - inicio)));
                        }
                    }

                    // Parallel GPU
                    for (int amostra = 1; amostra <= 3; amostra++) {
                        long inicio = System.currentTimeMillis();
                        int contagem = 0;
                        try {
                            contagem = BuscadorParaleloGPU.contar(texto, PALAVRA_ALVO);
                        } catch (Throwable e) {
                            System.err.println("[ALERTA GPU] " + e.getClass().getSimpleName() + ": " + e.getMessage());
                        }
                        long fim = System.currentTimeMillis();
                        csv.write(String.format("%s,%s,GPU_OpenCL,%d,%d,%d\n", caminho, tamanhoNome, amostra, contagem, (fim - inicio)));
                    }

                    csv.flush();
                }

            }

            System.out.println("CSV gerado em: " + CSV_FILE);
        } catch (IOException | InterruptedException | java.util.concurrent.ExecutionException e) {
            e.printStackTrace();
            System.exit(5);
        }
    }
}
