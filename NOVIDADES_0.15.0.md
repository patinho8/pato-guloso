# Maré Elétrica 0.15.0

Minecraft 1.21.1 · NeoForge 21.1.172 · Java 21

## Instalar

Feche o jogo, extraia o ZIP e copie **INSTALAR/mare-eletrica-0.15.0.jar** para a pasta `mods` do perfil usado. Retire o JAR anterior do Maré Elétrica. Cliente e servidor devem usar a mesma versão. Não é necessário compilar; não exige Curios, GeckoLib ou Iron’s Spells. Faça uma cópia do mundo antes de trocar a versão.

Este guia substitui as instruções antigas dos aparelhos alterados. Os demais continuam descritos nos guias anteriores.

## DMT

Equipe no espaço próprio do inventário. **H** abre o painel de cinco espaços; as instruções ficam à direita. As teclas H, F, U, Z, X, C e R podem ser alteradas em **Opções → Controles → Maré Elétrica**.

| Controle padrão | Ação |
|---|---|
| M2 (botão direito) | Lançar/recolher o gancho direito |
| Z / X | Lançar/recolher o gancho esquerdo / direito |
| Scroll para frente | Alongar os cabos presos **0,25 bloco** por passo |
| Scroll para trás | Recolher **0,25 bloco** por passo |
| Espaço com gancho preso | Impulso de gás na direção da mira, mantendo o limite da corda |
| C | Soltar os ganchos |
| F | Alternar entre armas equipadas e guardá-las |
| M1 (botão esquerdo) | Atacar com a arma sacada; com armas guardadas, mineração normal |
| R | Recarregar o tanque, devolvendo cilindro vazio |
| H | Abrir o painel e a ajuda |
| U | Desativar/reativar o DMT; desativar guarda as armas e solta os ganchos |

Desativado, o DMT permanece equipado, mas não captura os comandos. Sem cabo preso, o scroll volta a selecionar a barra rápida. O alcance inicial do gancho continua em 24 blocos; depois de preso, o cabo pode ser regulado entre 2 e 48 blocos.

### Os cinco espaços

1. **Lâminas:** um par de lâminas de manobra. As duas atacam juntas, com animação sincronizada nas duas mãos. O golpe combinado causa 14 de dano base, mais até 6 conforme a velocidade, e alcança até três alvos à frente. Um golpe que acerta gasta uma unidade de durabilidade do par.
2. **Arma de gás:** um disparo preciso, sem espalhamento, com **18 de dano base** e projétil a 12 blocos/tick. Leve cartuchos no inventário e gás no tanque. M1 com a arma vazia recarrega dois disparos; aguarde 1,5 segundo. Cada tiro usa 12 unidades de gás.
3. **Lanças do trovão:** modelo 3D próprio, com corpo, ponta e aletas; fumaça e chama saem da traseira durante o voo. M1 lança uma unidade. Ela prende no impacto e explode cerca de 1,5 segundo depois. A explosão pode quebrar blocos.
4. **Gás:** reserva de cilindros para recarregar o DMT. O cilindro usado volta vazio.
5. **Acessório de tração:** permite prender criaturas leves. Recolha o cabo com o scroll para puxá-las, **sem Shift**. Criaturas grandes/pesadas resistem.

O conteúdo permanece no DMT ao retirar e reequipar. U deve ser usado quando quiser interagir normalmente com objetos usando M2.

## Sniper de precisão

Nova arma preta com modelo 3D, luneta, coronha e carregador. Segure-a na mão principal e leve **munição de precisão** no inventário.

- **Segure M2:** mira circular com retículo e ampliação de 5×. A sensibilidade da visão cai para 22% enquanto mira; a configuração normal do mouse é preservada.
- **M1:** dispara, inclusive com a mira aberta. Cada tiro consome uma munição; intervalo de 1,75 segundo.
- Projétil sem espalhamento, a 20 blocos/tick, com **20 de dano base**. Contra um golem de ferro sem modificadores, tira 20 dos 100 pontos de vida, abaixo de 25%.
- A munição possui modelo próprio. A arma e as balas têm receitas de bancada.

Armadura e efeitos do alvo podem reduzir o dano. Segurar a sniper dá prioridade aos controles dela sobre os do DMT.

## Exterminador devorador

O modo gigante agora é uma criatura mecânica de **5 × 5 × 5**, com mandíbula e pernas articuladas. Ela persegue e devora mobs hostis próximos; ignora animais e não empurra o jogador.

Alimente a base com energia limpa para gerar o exterminador normal. Coloque **20 cilindros de gás** no espaço da base e use a opção de transformação. É preciso espaço livre de 5 × 5 × 5 em volta da criatura. Os 20 cilindros vazios aparecem na saída.

A transformação dura **cinco minutos de jogo carregado**. Ao terminar, a criatura some e a base desliga, evitando criar outra automaticamente. Ligue a base novamente para gerar um novo exterminador normal. O modo normal mantém espada e metralhadora.

## Roda de água 7 × 7 × 7

Novas colocações ocupam **7 × 7 × 7**, com madeira, aro, pás e eixo central, girando continuamente. Sem painel. Água natural parada serve; ela pode atravessar a estrutura.

**A roda agora produz energia suja**, inclusive em rios. Conecte por hastes ou cabos a um **purificador**, depois ao núcleo. Ligue outra roda clicando na face dianteira/traseira com uma roda na mão; uma haste colocada nessas faces se alinha ao eixo central.

Rodas antigas de 3 × 3 × 3 permanecem com o tamanho e a energia armazenada, para não apagar construções ao redor. Para aumentar uma delas, libere a área, retire e recoloque. A energia armazenada não acompanha o item ao quebrar; esvazie a roda antes.

## Água de balde

Água colocada com balde, inclusive por ejetor, **não gera energia**. Os fluxos derivados dela também ficam marcados. O duto preserva a origem ao transportar essa água. A regra vale para rodas, turbinas e geradores hidráulicos.

Use rios, mares e outras fontes naturais. Água colocada **antes de instalar a 0.15.0** não pode ser distinguida retroativamente da natural, pois versões anteriores não registravam sua origem.

## Núcleo quântico e chips

O núcleo quântico novo ocupa **3 × 3 × 3**, com base, pilares e cristal roxo, mantendo **100.000.000 FE** de capacidade. Núcleos antigos compactos permanecem funcionando; retire e recoloque para usar o novo tamanho, esvaziando a energia antes.

As máquinas compatíveis agora têm **três encaixes marcados na base frontal**. Cada tipo de chip só entra no encaixe correspondente. Clicar no teto ou em outra parte não instala nem consome o item.

Instale primeiro no núcleo; depois, com as placas restantes da mesma pilha, instale na máquina. Também pode vincular uma pilha usando Shift + M2 no encaixe do núcleo. Use Shift + M2 com uma placa no encaixe da máquina para removê-la.

| Encaixe/tipo | Alcance | Transferência máxima por núcleo e tipo |
|---|---:|---:|
| Comum | 32 blocos | 8.000 FE a cada 10 ticks |
| Cibernético | 128 blocos | 128.000 FE a cada 10 ticks |
| Hidráulico | 64 blocos | 128.000 FE a cada 10 ticks |

Instale o mesmo tipo nos dois lados. Transfere energia limpa já armazenada; os limites são compartilhados pelos receptores. Núcleo e máquina devem estar ligados, carregados e na mesma dimensão.

## Cama LED

A cama inteira muda lentamente entre sete cores, com transições contínuas, sem apagar ou voltar ao branco entre elas. Emite **luz de nível 12**. Aldeões reconhecem a cama e podem dormir nela. Continua com tamanho e funcionamento de cama normal; uma noite completa do jogador concede Pressa I e Velocidade I por cinco minutos.

## Balanceamento, broca e abas

- **Geotérmico:** geração reduzida de 40.000 para **10.000 FE por ciclo de 10 ticks**, mantendo as condições de calor e estrutura.
- **Broca:** detritos ancestrais passam a ter aproximadamente **0,305% por resultado** e o molde de melhoria de netherita **0,102%**. Cada resultado tem uma escolha independente. As outras chances são pesos relativos; a soma original acima de 100% foi normalizada. As levas continuam com 100 resultados, mais 100 por espaço de gás abastecido, até 1.100.
- **Criativo:** novas abas **Itens de guerra** e **Armaduras**. A segunda reúne o DMT e as armaduras disponíveis; esta atualização não cria conjuntos de armadura novos.
- **Morteiro:** comportamento mantido.

### Nove componentes mais acessíveis

As receitas foram reduzidas, especialmente o uso de netherita/estrela do Nether, mantendo uma progressão para máquinas poderosas.

| Componente | Ingredientes | Rendimento |
|---|---|---:|
| Liga reforçada | 6 ferros, 2 obsidianas, 1 diamante | 4 |
| Bobina supercondutora | 6 cobres, 2 placas de ferro, 1 redstone | 2 |
| Cristal sintético | 4 fragmentos de ametista, 1 diamante | 2 |
| Circuito quântico | 4 redstones, 2 ouros, 2 pérolas do Ender, 1 circuito de controle | 2 |
| Câmara de pressão | 6 ferros, 2 vidros | 2 |
| Servomotor | 1 placa de ferro, 2 bobinas de cobre, 1 pistão, 1 redstone | 2 |
| Célula de plasma | 1 placa de ferro, 2 pós de pedra luminosa, 1 cristal sintético, 1 redstone | 2 |
| Matriz dimensional | 4 ligas reforçadas, 2 circuitos quânticos, 2 pérolas do Ender, 1 diamante | 2 |
| Semente de singularidade | 1 obsidiana, 2 diamantes, 1 matriz dimensional, 1 fragmento de eco | 1 |

Consulte `VALIDACAO.md` para os testes realizados. Os arquivos de receita incluídos mostram a disposição exata dos ingredientes.
