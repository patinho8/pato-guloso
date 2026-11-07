# Validação — Maré Elétrica 0.14.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2. Compilação e execução em cliente gráfico real com servidor integrado e mundo descartável em 2026-10-17. O processo terminou com código 0 e BUILD SUCCESSFUL.

## Novidades verificadas

- Colocação real de roda conectada a outra roda e haste alinhada ao eixo; energia atravessando a cadeia até o núcleo.
- Purificador 3 × 3 × 3; abissal colocado submerso com 27 fontes de água e estrutura íntegra após atualizações de fluido.
- Núcleo de 100 milhões de FE. Três tipos de placas instalados por interação, energia conservada, limite compartilhado, fonte desligada e vínculos preservados no NBT.
- Cone do holofote à frente, giro da direção, limpeza dos pontos antigos e iluminação nível 15 confirmada pelo motor do jogo.
- H abrindo painel DMT, Shift-clique instalando cinco módulos, validação dos tipos, recarga pelo painel e retorno do cilindro vazio. Conteúdo/gás preservados por serialização e reequipamento.
- M1 lança o gancho direito; scroll regula o comprimento sem soltar; F saca lâminas, que causam dano e consomem durabilidade.
- Arma consome cartucho na recarga e gás no disparo. Lança consome item, voa e detona após impacto. Acessório puxa criatura leve e rejeita criatura pesada.
- Capturas do painel, armas nas mãos, purificador, núcleo, placas, rodas, holofote e cama inteira em sete cores/apagada.

## Regressões verificadas

- Roda antiga migrada de 125 para 27 células, mantendo 123.456 FE limpos e 54.321 FE sujos. Integridade da estrutura nova e remoção das peças excedentes verificadas.
- Haste grossa conectada ao eixo central; transferência de energia e interrupção ao quebrar a linha. Roda e turbina gerando com água parada, sem geração a seco. Água atravessando a turbina sem destruí-la. Roda sem painel.
- Cama com duas metades e modelo vanilla. Sete cores da cama inteira e intervalo apagado capturados no cliente. Sono interrompido sem bônus; noite completa concedendo Pressa I e Velocidade I por cinco minutos.
- Slot DMT separado do botão de receitas, tanto no inventário normal quanto no criativo. Rejeição de outros itens, equipar/retirar com Shift-clique sem duplicação e equipar com botão direito. Duas mãos livres.
- Salvamento real do jogador em NBT e carregamento em outro ServerPlayer preservando o DMT e seu gás. O slot continua ligado ao equipamento carregado. Sincronização do slot criativo com o servidor.
- Movimento do DMT, dois ganchos, gasto de gás e impulso pela mira. Simulação do limite da corda preservando movimento orbital.
- Regressões anteriores: entrada e saída da câmera do carrinho, movimento com personagem parado, transição entre chunks, reentrada e desconexão; dutos, geradores, aparelhos e edição do projetor.
- Inspeção visual da cama, LEDs, inventários e roda com haste. Duas capturas da roda permitem conferir a mudança da posição dos raios.

O JAR contém exatamente as 219 classes de produção e os 2230 recursos usados nessa execução. Todos os hashes foram comparados com o JAR final. Classes de teste e instrumentação do ambiente foram excluídas. Fontes, registros resumidos, capturas e hashes estão incluídos em tests/evidence_0.14.0.

Também passaram as verificações estruturais de modelos, texturas, receitas, traduções e referências. Consulte asset-validation.txt para a contagem.

## Limites

Não foi reproduzido o modpack completo do usuário, shaders, servidor dedicado ou uma sessão com dois jogadores. O renderizador utilizado foi o vanilla; a compatibilidade anterior com Embeddium permanece no código, sem nova execução com ele. As receitas foram carregadas pelo jogo, mas não foi fabricado manualmente cada item. A luz se difunde ao redor dos pontos do cone pelo sistema de blocos do Minecraft; não tem recorte de shader. O alcance do morteiro depende do ângulo, terreno e chunks carregados. A persistência foi verificada com salvamento/carregamento real do jogador no teste; não com uma reconexão manual. As regras de morte seguem a implementação de keepInventory e não tiveram um teste de morte separado.

SHA-256 do JAR: `584827b86e3dae1eb1af1cb4dbe8b765a243b8b659840caa77a02e9799cf0b99`
