# Maré Elétrica 0.11.0 — DMT e hidroelétricas grandes

Minecraft 1.21.1 • NeoForge 21.1.172 • Java 21

## DMT: equipamento de manobra tridimensional

Coloque o **DMT na mão secundária** e a **lâmina de manobra** na principal. O equipamento aparece em 3D na cintura; os dois cabos e os ganchos também aparecem no mundo. Você pode usar outra ferramenta na mão principal.

| Controle padrão | Ação |
|---|---|
| Z | Lançar ou soltar o gancho esquerdo |
| X | Lançar ou soltar o gancho direito |
| Botão direito com o DMT | Lançar os dois; usar novamente solta |
| Espaço, com um gancho preso | Impulsionar com gás na direção dos ganchos |
| A / D | Manobrar para os lados |
| C | Soltar os dois ganchos |
| R | Recarregar um cilindro de gás do inventário |
| Shift + botão direito com o DMT | Também recarrega |
| F5 | Ver o equipamento e os cabos em terceira pessoa |

As teclas podem ser alteradas em Opções → Controles. O HUD informa o gás e quais ganchos estão presos.

Mire em uma superfície sólida ou criatura a até **24 blocos**. Depois de preso, o cabo pode passar desse alcance inicial sem se soltar; o limite de segurança é 64 blocos. Blocos destruídos, criaturas removidas, troca de dimensão ou retirada do equipamento soltam os ganchos. O DMT não funciona enquanto você pilota o carrinho.

Um cilindro cheio fornece **60 segundos de impulso**. Cabem dois, totalizando 120 segundos. O gás só diminui durante o impulso com pelo menos um gancho preso; ficar parado ou usar Espaço sem ganchos não gasta gás. Cada recarga devolve um cilindro vazio, reutilizável na central de recarga da versão anterior. Uma recarga precisa de espaço para um cilindro inteiro.

Há proteção contra queda enquanto preso e por 1,5 segundo após soltar. A lâmina funciona como uma espada, com modelo 3D e durabilidade própria.

## Quatro geradores com condições diferentes

Todos têm modelos 3D, peças móveis, receitas e interface de energia. Coloque um único item para montar a estrutura inteira; a área precisa estar livre de construções. Os novos geradores também podem ser colocados dentro da água.

| Equipamento | Tamanho | Como usar | Produção máxima |
|---|---|---|---|
| Roda-d’água gigante | 5×5×5 | Leve correnteza até a faixa inferior da frente. Pelo menos quatro posições precisam ter água, e uma precisa apresentar fluxo horizontal. Mais posições molhadas aumentam a produção. | 64.000 FE/s |
| Turbina de correnteza | 3×3×3 | Faça a água atravessar a frente: pelo menos três posições devem ter corrente horizontal. Quanto mais posições com corrente, maior a geração. | 52.000 FE/s |
| Turbina de queda-d’água | 3×3×3 | Faça uma coluna de água cair no centro do topo. São necessários pelo menos dois blocos de coluna; a produção aumenta até oito. | 96.000 FE/s |
| Turbina de pressão profunda | 3×3×3 | Submerja a estrutura, com água em pelo menos 24 das 36 posições das laterais e uma coluna de pelo menos seis blocos acima do centro. A produção aumenta até dez blocos de coluna. | 160.000 FE/s |

Os valores são limites a 20 ticks por segundo, com as condições completas e espaço no reservatório. Água parada não movimenta a roda gigante nem a turbina de correnteza. Sem as condições necessárias, a produção e a animação param. Os geradores não consomem as fontes de água.

A frente é o lado da entrada visível; a estrutura cresce para trás e para a direita a partir do bloco colocado. Você pode conectar cabos às peças externas da estrutura. Use cabos cyberpunk para transportar mais energia, e núcleos para armazená-la e distribuí-la.

- **Água fora de bioma oceânico:** energia limpa → cabo → núcleo → aparelhos.
- **Bioma oceânico:** energia suja → cabo → purificador de energia → núcleo → aparelhos.

Cada gerador armazena temporariamente energia: roda gigante 1.000.000 FE, correnteza 750.000 FE, queda-d’água 1.500.000 FE e pressão 2.000.000 FE, em cada reservatório. A capacidade do purificador e dos cabos pode limitar o aproveitamento da produção no mar.

## Instalação

Com o jogo fechado, retire o JAR antigo do Maré Elétrica e coloque `INSTALAR/mare-eletrica-0.11.0.jar` na pasta `mods` do mesmo perfil. Em multiplayer, atualize cliente e servidor. O código está incluído, mas você não precisa compilá-lo. Consulte `VALIDACAO.md` para os testes realizados.
