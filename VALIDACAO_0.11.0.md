# Validação — Maré Elétrica 0.11.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2, Gradle 8.10.2. Verificações em 14/10/2026.

## Resultado

Cliente gráfico real, com servidor integrado e mundo descartável de sobrevivência: PASS no registro persistente do teste, processo encerrado com código 0. O PASS final exige a conclusão dos testes das versões 0.9, 0.10 e 0.11, do editor e do DMT; o log externo de console ficou parcial. O JAR final foi empacotado diretamente com as 183 classes de produção compiladas e executadas nesse teste e com os 2116 recursos usados pelo cliente. Os hashes de todas as classes e recursos foram comparados com o JAR final. Classes de teste e instrumentação do ambiente não estão no JAR.

## Novidades verificadas

- DMT: recarga de dois cilindros, devolução de vazios, limite do reservatório, nenhum gasto sem ganchos, dois ganchos presos e sincronizados, movimento do jogador com impulso, consumo de gás e limite de velocidade, soltura manual, desprendimento após quebra do bloco, gancho em criatura e cabo mantido além do alcance inicial de lançamento.
- Hidroelétricas: montagem das quatro estruturas; roda gigante com 125 posições; ausência de geração a seco; condições de correnteza, queda e profundidade; geração limpa, energia suja em bioma oceânico, entrega ao núcleo pela peça externa e rejeição de montagem em espaço ocupado.
- Água: colocação submersa preservou água na estrutura e os quatro geradores permaneceram íntegros após os ticks de água.

## Regressões incluídas na mesma execução

Água bombeada e enchimento de piscina, transferência de itens entre baús, recarga de gás, broca e cilindros vazios, exterminador e prazo da aranha, mira e atraso do morteiro, pintura contínua e setas do projetor. Carrinho com câmera distante, movimento, inventário, avião, saída, reentrada e desconexão. Os detalhes e evidências da versão anterior permanecem em VALIDACAO_0.10.0.md.

## Limites

Os testes usaram o cliente padrão, sem reproduzir o modpack Reinos, shaders, Embeddium ou um servidor dedicado. A expiração da aranha foi acelerada no teste. As capturas em tests/evidence_0.11.0 mostram a aparência no cliente real; não representam todos os ângulos, biomas e combinações de mods.

SHA-256 do JAR: `5dc4a9cde79c9db152c10c541c01c483659cc2b3f531c9bfd93300e88cff99e2`
