Resumo:

Este trabalho propõe uma análise detalhada do desempenho de diferentes algoritmos de busca em ambientes seriais e paralelos, utilizando a linguagem de programação Java. A busca por eficiência computacional é essencial em diversas aplicações, e entender como diferentes algoritmos se comportam em diferentes cenários de processamento é de suma importância. Neste estudo, serão abordados três algoritmos serial, paralelo em CPU e paralelo em GPU. 

Serão realizadas análises comparativas utilizando textos como conjuntos de dados de entrada para a contagem de busca de uma palavra. Os resultados serão registrados em arquivos CSV, permitindo uma análise visual através de gráficos ou processamento adicional utilizando Java.

Objetivos:

1. Crie um programa em Java que crie três métodos onde:

- Metodo SerialCPU - Versão serial na CPU: Utilizar um loop simples para iterar sobre cada palavra do texto e contar as ocorrências.

- Metodo ParallelCPU - Versão paralela na CPU: Utilizar um pool de threads para dividir o texto em partes e contar as palavras em paralelo.

- Metodo ParallelGPU- Versão paralela na GPU: Utilizar OpenCL  para processar o texto em paralelo na GPU, contando as palavras de forma eficiente.

Cada método deve ter como entrada um arquivo txt (entre os da amostra em anexou ou outros), com o texto onde serão contadas as palavras, deve informar também uma palavra que será contada. 

A saída deve ser a contagem da palavra e o tempo de execução para essa contagem.

Ex: 

SerialCPU: 66 ocorrências em 133 ms

ParallelCPU: 66 ocorrências em 82 ms

ParallelGPU: 119 ocorrências em 2705 ms

2. Realizar análises de desempenho comparativas entre as versões serial e paralelas dos algoritmos de contagem. 

Atenção aos elementos a seguir:

- Variar o tamanho e a natureza dos conjuntos de dados de entrada para examinar o impacto no desempenho, usando textos com tamanho diferentes;

- Realizar uma execução Serial;

- Realizar execuções paralelas com CPU e GPU;

- Pelo menos 3 amostras de cada execução devem ser executadas, afim de ter amostras suficientes para gerar os gráficos de análise.

3. Investigar o comportamento dos algoritmos sob diferentes configurações de processamento paralelo, ajustando o número de núcleos de processamento disponíveis.

4. Gerar arquivos CSV contendo os resultados das análises para facilitar a visualização e o processamento posterior.

5. Gerar os gráficos indicando as execuções com as entradas de dados diferentes - O gráfico deve ser feito 

Dica: 

- Usar Swing Java, JUPITER ou JSF (api Prime Faces) - n (EXTRA)

- Usar a biblioteca jocl-2.0.4 e as amostras em anexo.

5.Elaborar um relatório com uma apresentação dos resultados e códigos utilizados. Usar o Readme do GITHUB (Formato markdown):

- Resumo

- Introdução – Descrevam os métodos escolhidos e a abordagem para o trabalho.
- Metodologia - “Análise estatística dos resultados obtidos para identificar padrões de desempenho e comparar os algoritmos sob diferentes condições“.
- Resultados e Discussão – Apresente as análises dos resultados dos testes, com percepção e demonstração dos gráficos das execuções.
- Conclusão 
- Referências
- Anexos – Códigos das implementações ----> Incluir no final o link do projeto no GITHUB.

Metodologia:

Implementação de Algoritmos: Criação de algoritmos de busca sequenciais e paralelos em Java.
Framework de Teste: Desenvolvimento de um framework de teste para executar e registrar os tempos de execução.
Execução em Ambientes Variados: Teste em diferentes ambientes para observar a variação no desempenho.
Registro de Dados: Armazenamento dos tempos de execução em arquivos CSV.
Análise Estatística: Análise dos dados coletados para identificar padrões de desempenho.

Resultados Esperados:

Espera-se obter uma compreensão mais profunda do desempenho relativo dos algoritmos de busca em ambientes seriais e paralelos com CPU e GPU. 

Os resultados fornecerão insights sobre quais meios de processamento são mais adequados para diferentes volumes de massa e como o desempenho é afetado por fatores como o tamanho do conjunto de dados meio de processamento. 

Além disso, os arquivos CSV gerados permitirão uma análise visual clara dos resultados em gráficos e facilitarão a comparação com estudos futuros.

Este trabalho contribuirá para o avanço do conhecimento em computação concorrente e paralela, fornecendo informações valiosas para desenvolvedores e pesquisadores interessados em otimizar o desempenho de algoritmos em sistemas distribuídos, multicore e em GPU.

A atividade deve ser feita em DUPLA e vale 2 pontos.
IMPORTANTE!
- Enviar um arquivo PDF do README com o link do GITHUB onde for colocado o projeto.
