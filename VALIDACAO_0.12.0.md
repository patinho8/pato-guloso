# Validação — Maré Elétrica 0.12.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2. Teste com cliente gráfico real, servidor integrado e mundo descartável. Os registros persistentes encerraram com PASS e o processo com código 0.

Verificações: roda e turbina gerando com fonte de água dentro da estrutura; parada a seco; água atravessando a turbina sem quebrá-la; roda sem menu; transferência por hastes, interrupção ao quebrar uma e aceitação de água. Modelo de madeira sem armação; cama com duas metades e LEDs alternando. Sono interrompido não deu bônus; uma noite real completada concedeu Pressa I e Velocidade I por cinco minutos. DMT com movimento real para a direção da mira, gás, dois ganchos e soltura; simulação do limite radial com movimento orbital. As verificações anteriores de carrinho, dutos, geradores, projetor e aparelhos também foram executadas na mesma sequência.

O JAR foi empacotado diretamente com as 188 classes de produção e os 2135 recursos usados nessa execução. Todos os hashes foram comparados com o JAR final. Classes dos testes e instrumentação do ambiente foram excluídas. As capturas, hashes e registros estão em tests/evidence_0.12.0.

O teste usou o cliente padrão, sem reproduzir o modpack completo do usuário, shaders ou servidor dedicado. A simulação de órbita verifica a geometria do cabo; o teste com jogador real verifica direção do impulso e manutenção dos ganchos. Consulte os fontes de testes incluídos para o escopo exato.

SHA-256: `8374a4468f65506a1715b27a0538fd6a67e3d0177d552e105f3a4b9ba7c80741`
