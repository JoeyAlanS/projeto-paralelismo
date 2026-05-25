#!/usr/bin/env bash
set -euo pipefail

# Script para compilar e rodar os benchmarks sem sudo
# - limpa a pasta de build `out` (se possível)
# - compila todos os fontes em src/main/java/com/unifor/concorrente
# - executa o MinimalBenchmarkRunner
# - gera o gráfico via scripts/generate_grafico_execucoes.py

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "[run_benchmarks] Diretório do projeto: $ROOT_DIR"

# Limpar build anterior
if [ -d out ]; then
  if [ -w out ]; then
    echo "[run_benchmarks] Removendo diretório out/ existente..."
    rm -rf out
  else
    echo "[run_benchmarks] ERRO: diretório 'out' existe mas não é gravável pelo usuário atual."
    echo "Remova manualmente ou ajuste permissões (ex: sudo chown -R $(id -u):$(id -g) out) e reexecute este script."
    exit 1
  fi
fi

mkdir -p out

echo "[run_benchmarks] Compilando fontes Java..."
# Compila apenas os arquivos necessários ao runner (evita dependências gráficas como JFreeChart)
javac -cp jocl/jocl-2.0.4.jar -d out src/main/java/com/unifor/concorrente/BuscadorSerial.java src/main/java/com/unifor/concorrente/BuscadorParaleloCPU.java src/main/java/com/unifor/concorrente/BuscadorParaleloGPU.java src/main/java/com/unifor/concorrente/MinimalBenchmarkRunner.java

echo "[run_benchmarks] Execução do runner para gerar CSV..."
java -cp out:jocl/jocl-2.0.4.jar com.unifor.concorrente.MinimalBenchmarkRunner

echo "[run_benchmarks] Gerando gráfico (PNG) a partir do CSV..."
python3 scripts/generate_grafico_execucoes.py

echo "[run_benchmarks] Concluído. CSV e PNG gerados."
