// Kernel OpenCL para buscar ocorrências exatas de uma palavra em um buffer de texto
// Compatível com o uso via JOCL

__kernel void contar_palavra(__global const char* texto, int textoLen,
                             __global const char* palavra, int palavraLen,
                             __global int* resultado) {
    int gid = get_global_id(0);
    if (gid + palavraLen > textoLen) return;

    // Comparação direta dos bytes (assume ASCII / UTF-8 simples para letras latinas)
    for (int i = 0; i < palavraLen; i++) {
        if (texto[gid + i] != palavra[i]) return;
    }

    // Verifica fronteiras para garantir palavra inteira (não substring)
    if (gid > 0) {
        char p = texto[gid - 1];
        if ((p >= 'a' && p <= 'z') || (p >= 'A' && p <= 'Z') || (p >= '0' && p <= '9')) return;
    }
    if (gid + palavraLen < textoLen) {
        char n = texto[gid + palavraLen];
        if ((n >= 'a' && n <= 'z') || (n >= 'A' && n <= 'Z') || (n >= '0' && n <= '9')) return;
    }

    // Incremento atômico do contador de resultados
    atomic_inc(resultado);
}
