package com.unifor.concorrente;

public class BuscadorSerial {
    public static int contar(String[] palavras, String palavraAlvo) {
        int contador = 0;
        for (String p : palavras) {
            if (p.equalsIgnoreCase(palavraAlvo)) {
                contador++;
            }
        }
        return contador;
    }
}