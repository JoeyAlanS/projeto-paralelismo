package com.unifor.concorrente;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutionException;

public class ContadorPalavrasPrincipal {

    // 1. Resultados gerados em CSV
    private static final String OUTPUT_DIR = "resultados";
    private static final String CSV_FILE = OUTPUT_DIR + "/resultados_busca.csv";

    private static final String PALAVRA_ALVO = "java";

    public static void main(String[] args) {
        try {
            System.out.println("=== CONTADOR DE PALAVRAS - EXECUÇÃO ===");

            // Criação automática da pasta de resultados
            File dir = new File(OUTPUT_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
                System.out.println("Diretório '" + OUTPUT_DIR + "' criado com sucesso para o CSV.");
            }

            // Arquivos de teste
            inicializarTextosAmostra();

            String[] arquivos = {"texto_pequeno.txt", "texto_medio.txt", "texto_grande.txt"};
            String[] nomesTamanhos = {"01. Pequeno (1MB)", "02. Medio (10MB)", "03. Grande (50MB)"};

            try (FileWriter csvWriter = new FileWriter(CSV_FILE)) {
                csvWriter.append("Arquivo,Tamanho,Metodo,Amostra,Contagem,TempoMS\n");

                for (int i = 0; i < arquivos.length; i++) {
                    String caminho = arquivos[i];
                    String tamanhoNome = nomesTamanhos[i];
                    System.out.println("\n--------------------------------------------------");
                    System.out.println("Processando base de dados: " + tamanhoNome);

                    String texto = new String(Files.readAllBytes(Paths.get(caminho)), StandardCharsets.UTF_8);
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
                System.out.println("\n[SUCESSO] Base estatística exportada para: " + CSV_FILE);
            }

            // Abre a janela Swing com os gráficos apontando para o novo CSV
            javax.swing.SwingUtilities.invokeLater(() -> {
                VisualizadorDesempenho appGrafico = new VisualizadorDesempenho(CSV_FILE);
                appGrafico.setVisible(true);
            });

        } catch (IOException | InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    private static void inicializarTextosAmostra() throws IOException {
        String[] arquivos = {"texto_pequeno.txt", "texto_medio.txt", "texto_grande.txt"};
        int[] multiplicadores = {14000, 140000, 700000}; // Produz ~1MB, ~10MB e ~50MB

        for (int i = 0; i < arquivos.length; i++) {
            File f = new File(arquivos[i]);
            if (!f.exists()) {
                System.out.println("Criando massa de dados inicial automatizada: " + arquivos[i]);
                try (BufferedWriter bw = new BufferedWriter(new FileWriter(f))) {
                    for (int k = 0; k < multiplicadores[i]; k++) {
                        bw.write("java computacao concorrente unifor cct threads paralelismo opencl gpu amd nvidia intel. ");
                        if (k % 7 == 0) {
                            bw.write("texto aleatorio apenas para preenchimento de espaco e analise de desempenho estruturado. ");
                        }
                    }
                }
            }
        }
    }
}
