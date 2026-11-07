# Validação — Maré Elétrica 0.10.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2, Gradle 8.10.2 e ModDevGradle 2.0.107. Verificações em 13/10/2026.

## Resultado

O cliente gráfico real abriu e criou um mundo de sobrevivência com servidor integrado. A rodada final terminou com PASS, BUILD SUCCESSFUL e código de saída 0. Depois, a compilação limpa de produção também passou.

O JAR final contém 164 classes de produção, byte a byte iguais às utilizadas no teste gráfico final. Os 1870 recursos empacotados também têm SHA-256 idêntico ao conjunto testado. Classes de integração, simulações e o agente do ambiente de compilação não estão no JAR.

## Verificações dentro do Minecraft

- Água: 18 fontes foram transferidas para uma cavidade de 3×3×2, preenchendo-a por completo. Cada entrega retirou a fonte da entrada. O jato de torneira apareceu, desapareceu após o fornecimento parar e a piscina permaneceu cheia. Dutos sobreviveram sob água comum e água bombeada. Saída bloqueada preservou a fonte; o escoamento alcançou mais de sete blocos.
- Gás: central de 125 posições íntegra; um cilindro vazio tornou-se um cheio após dez ciclos, consumindo exatamente 25.000 FE. A broca retornou dez cilindros vazios e manteve a leva exata de 1.100 minérios após salvar/restaurar. A transformação da aranha devolveu vinte vazios.
- Exterminador: metralhadora disparou exatamente três projéteis em vinte ticks. A aranha funcionou sem energia elétrica na base, eliminou um zumbi, preservou uma vaca e não aplicou empurrão ao jogador. Seu prazo inicial foi de 24.000 ticks; a expiração foi antecipada no teste e confirmou remoção da aranha e desligamento da base.
- Mira: holofote aceitou giro horizontal e inclinação, preservando ambos no salvamento. Morteiro aceitou azimute de −180°, permaneceu sem disparar até 39 ticks e lançou os seis projéteis após o prazo de quarenta ticks.
- Projetor: a tela recebeu a construção, uma seta foi arrastada para alterar a largura e um gesto contínuo de M1 pintou mais de um bloco. A cor foi verificada no estado real do servidor, após confirmação dos pacotes. Giro, zoom, pintura em lote, alongamento e rejeição atômica de tamanho excessivo passaram.
- Carrinho: controle a 186 blocos com personagem imóvel, travessia de chunk com terreno visível, inventário, avião com gás, câmera em terceira pessoa, saída, reentrada e desconexão sem crash. O mundo foi salvo e o jogo encerrou normalmente.
- Itens: 64 itens passaram entre dois baús, sem perda ou duplicação.

## Inspeção visual e verificações isoladas

Foram inspecionadas capturas reais da central com dez cilindros visíveis, humanoide, lancha, avião compacto, inventário e editor com setas. A lancha foi elevada visualmente em 0,28 bloco após a primeira inspeção, e o teste gráfico completo foi repetido.

O validador aprovou 1.851 JSONs e 63 objetos, incluindo texturas, referências, receitas, traduções e separação de código cliente. Os testes de mira e veículos passaram com 268.669 verificações, incluindo todas as direções horizontais.

## Evidências e limites

`tests/evidence_0.10.0/` contém mensagens de resultado, capturas e hashes das classes e recursos. A expiração foi testada acelerando o prazo, sem esperar vinte minutos reais. Os testes de mira exercitam os ângulos no servidor; não reproduzem todas as combinações de botões e dispositivos em multiplayer.

Não foi reproduzido o modpack Reinos completo, shaders ou um servidor dedicado. A compatibilidade anterior com Embeddium foi preservada, mas esta rodada executou sem Embeddium. Os resultados anteriores permanecem em `VALIDACAO_0.9.0.md` e nas pastas de evidências antigas.

SHA-256 de `mare-eletrica-0.10.0.jar`:
`f15410f13305caa42a72d4808c32d8058607e8865c1d6b229ecef33f773d1147`
