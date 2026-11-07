# Validação — Maré Elétrica 0.17.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2. Cliente gráfico real com servidor integrado, mundo descartável e renderizador vanilla. Execução com código de saída 0 e BUILD SUCCESSFUL.

## Verificado nesta atualização

- Cliente gráfico real iniciou, abriu um mundo descartável e encerrou normalmente.
- Raycasts reais e instalação de chips em máquinas pequenas/grandes e nas quatro orientações, com painéis menores.
- DMT: impulso e aceleração lateral bloqueados ao prender em criatura; caso misto criatura/parede também bloqueado. Preso apenas à parede, o gás ainda dá impulso. Puxar uma criatura leve com acessório continua funcionando sem consumir gás.
- Disparos reais de teste compararam a dispersão das balas sem mirar e mirando. Silenciadores usam volume inferior a 1 em todos os níveis; recursos de áudio abafado carregados.
- Painel da sniper, módulos e munição, calibração, recuo de mais de dez graus mesmo com silenciador de nível máximo, saída automática da mira e liberação de M2.
- Shift duplo deita/levanta; altura de colisão sincronizada, tiro deitado apoiado e teto impedindo levantar dentro do bloco.
- Luneta de 24× preservou a distância de renderização do cliente e a distância de visão do servidor integrado. F5 mostra o personagem mirando sem aplicar a máscara da luneta.
- Revólver e consumo de munição, recarga parcial, abas do criativo: regressões verificadas.
- Capturas reais: mãos, pintura, oficina, retícula, recuo, postura de mira em pé/deitado e painéis.
- Validação estrutural dos modelos, texturas e receitas; fumaça usa partícula translúcida própria.

O JAR usa exatamente as 254 classes de produção e os 2839 recursos da execução. Hashes comparados após empacotar; testes/instrumentação foram excluídos. Evidências em tests/evidence_0.17.0.

## Limites

Teste com renderizador vanilla e servidor integrado; não foi reproduzido o modpack completo, shaders, Embeddium ou uma sessão com dois clientes em servidor dedicado. A verificação de chunks compara as configurações do cliente e servidor durante a mira; não mede tráfego de uma segunda conexão. Saída de áudio virtual: o som foi carregado e seus níveis medidos, sem avaliação auditiva em alto-falantes. Evidências de versões anteriores são históricas.

SHA-256: `f36b0b3826eb9cf0d045403c18048b3205c6f14f3556c34fa21d02c3b19ddc95`
