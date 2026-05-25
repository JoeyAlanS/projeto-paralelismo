#!/usr/bin/env python3
"""
Gera `grafico_execucoes.png` a partir de `resultados/resultados_benchmark.csv`.
Uso: python3 scripts/generate_grafico_execucoes.py
"""
import os
import sys
import csv
from collections import defaultdict, OrderedDict

# Tenta usar pandas para leitura/agrupamento, cai para csv puro em seguida.
try:
    import pandas as pd
except Exception:
    pd = None

try:
    import matplotlib
    matplotlib.use('Agg')
    import matplotlib.pyplot as plt
except Exception:
    print("ERRO: matplotlib não está instalado. Instale com: pip install matplotlib")
    sys.exit(2)

# Caminhos
SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, '..'))
CSV_PATH = os.path.join(PROJECT_ROOT, 'resultados', 'resultados_benchmark.csv')
OUT_PNG = os.path.join(PROJECT_ROOT, 'grafico_execucoes.png')

if not os.path.exists(CSV_PATH):
    print(f"ERRO: arquivo CSV não encontrado: {CSV_PATH}")
    sys.exit(3)

# Ler e agregar
if pd is not None:
    df = pd.read_csv(CSV_PATH)
    # Preservar ordem de aparição das categorias de tamanho
    sizes = list(OrderedDict.fromkeys(df['Tamanho'].tolist()))
    pivot = df.groupby(['Tamanho', 'Metodo'])['TempoMS'].mean().unstack()
    pivot = pivot.reindex(index=sizes)
    methods = list(pivot.columns)
    data = pivot
else:
    # Leitura manual e média
    sums = defaultdict(float)
    counts = defaultdict(int)
    sizes_order = []
    with open(CSV_PATH, newline='') as f:
        reader = csv.DictReader(f)
        for row in reader:
            tamanho = row['Tamanho']
            metodo = row['Metodo']
            try:
                tempo = float(row['TempoMS'])
            except Exception:
                tempo = float(row.get('Tempo', row.get('Tempo_ms', 0)))
            key = (tamanho, metodo)
            sums[key] += tempo
            counts[key] += 1
            if tamanho not in sizes_order:
                sizes_order.append(tamanho)
    methods_set = sorted({k[1] for k in sums.keys()})
    methods = methods_set
    # construir matriz ordenada
    import math
    data = []
    for tamanho in sizes_order:
        row_vals = []
        for metodo in methods:
            key = (tamanho, metodo)
            if counts.get(key, 0) > 0:
                row_vals.append(sums[key] / counts[key])
            else:
                row_vals.append(math.nan)
        data.append(row_vals)
    # transformar em formatos compatíveis
    import numpy as _np
    data = _np.array(data)
    sizes = sizes_order

# Plot
try:
    plt.style.use('seaborn-darkgrid')
except Exception:
    try:
        plt.style.use('seaborn')
    except Exception:
        pass
fig, ax = plt.subplots(figsize=(9,5))

if pd is not None:
    x = list(range(len(data.index)))
    labels = [str(s) for s in data.index]
    for metodo in methods:
        y = data[metodo].values
        ax.plot(x, y, marker='o', label=metodo)
else:
    x = list(range(len(sizes)))
    labels = sizes
    for i, metodo in enumerate(methods):
        y = data[:, i]
        ax.plot(x, y, marker='o', label=metodo)

ax.set_xticks(x)
ax.set_xticklabels(labels, rotation=20)
ax.set_xlabel('Tamanho do arquivo')
ax.set_ylabel('Tempo médio (ms)')
ax.set_title('Comparativo de tempo de execução por tamanho e método')
ax.legend(loc='best')
plt.tight_layout()

try:
    plt.savefig(OUT_PNG, dpi=150)
    print(f'Sucesso: arquivo gerado em {OUT_PNG}')
except Exception as e:
    print('ERRO ao salvar PNG:', e)
    sys.exit(4)
