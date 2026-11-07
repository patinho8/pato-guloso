# Validação — Maré Elétrica 0.16.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2. Cliente gráfico real com servidor integrado, mundo descartável e renderizador vanilla. Execução em 2026-10-21, código de saída 0 e BUILD SUCCESSFUL.

## Verificado nesta atualização

- Minecraft iniciou, abriu um mundo separado e encerrou normalmente.
- Painéis frontais: raycasts reais em broca, purificador, núcleo, potabilizador, fornalha, morteiro e estufa; instalação dos três tipos de chip. Fornalhas nas quatro orientações.
- Dez miras, cinco silenciadores, bipé e seis adesivos registrados. Módulos e munição persistem em uma cópia da arma. Itens incorretos rejeitados. Trocar a mira zera a regulagem. Botões de distância/horizontal funcionam; arma na mão não pode ser trocada para um encaixe do próprio painel.
- J abre os cinco espaços. Capturas reais do painel, modelo, mira com ponto vermelho e recuo. A animação de equipar não reinicia ao mudar a munição.
- Sniper: 20 de dano em golem, um cartucho consumido do painel, saída automática da luneta e recuo da câmera. Segurar M2 não reabre a mira depois do tiro.
- Shift duplo deita e levanta, com pose e altura de colisão sincronizadas. Teto impede levantar dentro de um bloco. Bipé parado no chão elimina a dispersão de deitado; removê-lo aumenta a dispersão. Tiro deitado apoiado acerta o alvo.
- Zoom pequeno mantém a visão em 4 chunks. Luneta maior aumenta temporariamente para 6; chunk distante recebido pelo cliente. Sair da mira restaura 4 chunks.
- Revólver: seis tiros independentes de 12 de dano, recarga de seis cartuchos, tambor vazio não fica negativo, recarga parcial consome somente os cartuchos disponíveis.
- Aba Armaduras sem DMT/armaduras externas; aba guerra sem exterminador, torreta e munição de carrinho.
- Verificação estrutural de JSON, modelos, texturas, receitas e traduções. Sons originais OGG presentes e eventos carregados pelo jogo.

O JAR usa exatamente as 250 classes de produção e os 2724 recursos da execução. Hashes comparados após empacotar; testes e instrumentação foram excluídos. Evidências em tests/evidence_0.16.0.

## Limites

Teste em cliente vanilla com servidor integrado. Não foi reproduzido o modpack completo do usuário, shaders, Embeddium, servidor dedicado ou sessão com dois jogadores. Áudio foi carregado com dispositivo de saída virtual; não houve avaliação auditiva em alto-falantes. A calibração foi verificada por valores e compensação matemática, sem um ensaio de todos os alcances. O teste de chunks verificou aumento de 4 para 6; em multiplayer, o limite do servidor continua valendo. Não foi avaliado desempenho prolongado no zoom máximo de 32 chunks. Evidências antigas são históricas, não testes novamente executados.

SHA-256: `fa86993e2faa2f56e517510f5f659945184217d96876cdd8f8c4fd203ad3425a`
