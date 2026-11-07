# Validação — Maré Elétrica 0.9.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2, Gradle 8.10.2 e ModDevGradle 2.0.107.

## Resultado

Compilação de produção aprovada. O JAR contém 152 classes de produção, com SHA-256 individual idêntico ao das classes usadas no teste gráfico final. Não contém classes do teste, simulações offline nem o agente usado pelo ambiente de compilação.

O cliente gráfico real, com servidor integrado e sem Embeddium, abriu, criou um mundo de sobrevivência descartável e terminou com PASS, BUILD SUCCESSFUL e código de saída 0.

## Verificações no jogo

- Duto de água: uma saída bloqueada preserva a fonte; uma transferência retira a fonte e cria a água na saída. O escoamento chegou a dez blocos do bocal, além dos sete habituais.
- Duto de itens: 64 itens passaram entre dois baús, sem perda ou duplicação.
- Projetor: pintura, alongamento, rejeição de tamanho excessivo sem alterar o projeto e salvamento/restauração do editor. A tela recebeu o projeto e permitiu renderização, giro e zoom.
- Broca: dez cilindros foram consumidos para uma leva de 1.100 minérios; o estado foi salvo e restaurado, e a saída total foi exatamente 1.100.
- Extermina Monstro: montagem da base de 125 blocos, criação do guardião, consumo de 20 gás e transformação com dimensões de 50×50×50. Um zumbi foi eliminado; uma vaca permaneceu viva e com a vida completa.
- Carrinho: controle a 186 blocos, além da distância de renderização; movimento e travessia de chunk com terreno visível e personagem parado. Inventário com cinco espaços próprios, voo com consumo de gás e câmera em terceira pessoa.
- Câmera: saída, restauração da perspectiva anterior, reentrada e desconexão com o controle ativo. O mundo foi salvo e o jogo encerrou normalmente.

As capturas do editor, inventário e avião foram inspecionadas. O painel do veículo foi reposicionado para não cobrir a barra de sede; o teste completo foi repetido após esse ajuste e passou.

## Regras e recursos

O validador aprovou 1.706 JSONs e 62 objetos, incluindo referências de modelos e texturas, receitas e traduções. Os testes isolados aprovaram a distribuição completa dos pesos de minério, os limites de gás, 2.097.152 endereços de voxels, continuidade do alongamento e limites de volume.

## Evidências e limites

`tests/evidence_0.9.0/resultado.txt` guarda as mensagens finais do teste. A mesma pasta contém as capturas e os hashes individuais das classes testadas. As evidências antigas em `tests/evidence` pertencem à 0.8.1; o relatório anterior foi preservado em `VALIDACAO_0.8.2.md`.

Esta rodada não reproduziu o modpack Reinos completo, shaders, servidor dedicado ou todos os combates e transformações em uma sessão manual. A correção anterior para Embeddium foi preservada; o teste com Embeddium documentado na 0.8.2 não foi repetido na 0.9.0. Os resultados acima descrevem o que foi efetivamente executado.

SHA-256 de `mare-eletrica-0.9.0.jar`:
`1096b04283e2b8aa978436691a5485ab9129e956588b22dd417aa61f03f1124c`
