package com.unifor.concorrente;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

public class BuscadorParaleloCPU {
    public static int contar(String[] palavras, String palavraAlvo, int numThreads) throws InterruptedException, ExecutionException {
        ExecutorService executor = Executors.newFixedThreadPool(numThreads);
        int tamanhoChunk = (int) Math.ceil((double) palavras.length / numThreads);
        List<Future<Integer>> resultados = new ArrayList<>();

        for (int i = 0; i < numThreads; i++) {
            final int inicio = i * tamanhoChunk;
            final int fim = Math.min(inicio + tamanhoChunk, palavras.length);

            if (inicio >= palavras.length) break;

            resultados.add(executor.submit(() -> {
                int parcial = 0;
                for (int j = inicio; j < fim; j++) {
                    if (palavras[j].equalsIgnoreCase(palavraAlvo)) {
                        parcial++;
                    }
                }
                return parcial;
            }));
        }

        int total = 0;
        for (Future<Integer> f : resultados) {
            total += f.get();
        }

        executor.shutdown();
        return total;
    }
}