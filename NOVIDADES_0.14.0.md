# Maré Elétrica 0.14.0

Minecraft 1.21.1 • NeoForge 21.1.172 • Java 21

## Instalação

Feche o jogo, extraia o ZIP e coloque `INSTALAR/mare-eletrica-0.14.0.jar` na pasta `mods` do seu perfil. Retire o JAR antigo do Maré Elétrica. Em multiplayer, cliente e servidor precisam da mesma versão. Não precisa compilar nem instalar Curios ou GeckoLib.

## DMT: equipar e abrir

Equipe o aparelho no espaço próprio do inventário ou use o botão direito segurando o DMT. A palavra ao lado do espaço foi retirada. O equipamento aparece na cintura, com cilindros metálicos, carretéis, suportes e correias. Os cabos são pretos e terminam em ganchos metálicos.

**H abre o painel.** Todas as teclas do DMT podem ser alteradas em Opções → Controles → Maré Elétrica. O painel tem cinco espaços exclusivos:

| Espaço | Coloque | Uso |
|---|---|---|
| Lâminas | Par de lâminas de manobra | Aparecem nas duas mãos; ataques alcançam uma área à frente, com até três alvos por golpe. Têm durabilidade. |
| Arma | Arma de gás | Aparece na mão direita. Tem dois disparos por recarga, espalha projéteis e dá um pequeno recuo. |
| Lanças | Até 16 lanças do trovão | Aparecem na mão direita. A lança gruda no alvo ou na superfície e explode após aproximadamente 1,5 segundo. |
| Gás | Até 64 cilindros de gás comprimido | Reserva para recarregar o tanque do DMT. |
| Gancho | Acessório de tração | Permite puxar criaturas leves, encurtando o cabo enquanto segura Shift. |

As peças e a reserva de gás ficam dentro do próprio DMT quando ele é retirado, guardado ou derrubado. Shift + clique move as peças entre inventário e painel. Cada espaço aceita apenas o item correto.

### Controles padrão

| Controle | Ação |
|---|---|
| H | Abrir o painel |
| F | Alternar entre lâminas, arma, lanças disponíveis e guardar |
| M1, armas guardadas | Lançar/recolher o gancho direito |
| M1, arma sacada | Atacar/disparar; segurar repete respeitando a cadência |
| Z / X | Lançar/recolher o gancho esquerdo / direito |
| C | Soltar os ganchos |
| Scroll para a frente | Alongar os cabos presos, 2 blocos por passo |
| Scroll para trás | Encurtar os cabos presos, 2 blocos por passo |
| Shift com acessório | Puxar a criatura presa se ela for leve o suficiente |
| Shift + M1, armas guardadas | Permitir a mineração normal |
| Espaço com gancho preso | Impulso de gás na direção da sua visão |
| R ou botão Recarregar gás | Recarregar o tanque |

Os ganchos são lançados até 24 blocos. Depois de presos, o comprimento pode ser regulado entre 2 e 48 blocos; o movimento orbital continua funcionando. Sem gancho preso, o scroll muda a barra rápida normalmente.

Um cilindro fornece 60 segundos de impulso e o tanque comporta 120. A recarga usa primeiro o espaço do painel, depois o inventário, e devolve o cilindro vazio. O gás também é gasto nos disparos da arma. Use os cilindros vazios no recarregador de gás existente.

**Arma de gás:** leve cartuchos de gás no inventário normal e mantenha gás no tanque do DMT. Quando os dois canos estão vazios, M1 inicia a recarga; aguarde 1,5 segundo e dispare. Cada cartucho carrega dois disparos. As lanças são consumidas individualmente e a explosão pode quebrar blocos.

## Roda e hastes

A roda mantém 3 × 3 × 3 blocos, visual de madeira e rotação contínua. Não tem painel. Para gerar energia, precisa de água, que pode estar parada e atravessar sua estrutura.

- Segure a haste e clique na face dianteira ou traseira da roda: ela será alinhada ao eixo central.
- Segure outra roda e clique na face dianteira ou traseira: a nova roda fica alinhada e conectada à primeira.
- Estenda as hastes até um núcleo. A energia pode atravessar rodas conectadas.
- O eixo que já vem na roda foi encurtado; os trechos de ligação aparecem onde há haste ou outra roda.

Os cliques na roda agora permitem colocar itens; não são consumidos por um painel inexistente.

## Núcleo quântico e placas sem fio

O **núcleo quântico** armazena **100.000.000 FE limpos**. Pode receber dos geradores/purificador e alimentar máquinas por cabos ou placas.

As placas são pequenas peças tecnológicas montadas na superfície da máquina. Instale uma no núcleo e outra do mesmo tipo na máquina. Ao instalar a primeira, as placas que restam na sua mão ficam vinculadas ao núcleo. Também é possível segurar placas e usar Shift + botão direito no núcleo para vinculá-las.

| Placa | Equivalente | Alcance | Limite total por núcleo e tipo |
|---|---|---:|---:|
| Placa sem fio | Cabo comum | 32 blocos | 8.000 FE a cada 10 ticks |
| Placa cibernética | Cabo cibernético | 128 blocos | 128.000 FE a cada 10 ticks |
| Placa hidráulica | Haste energética | 64 blocos | 128.000 FE a cada 10 ticks |

Os limites são compartilhados pelas máquinas atendidas naquele intervalo. A transferência usa a energia já armazenada no núcleo. Núcleo e máquina precisam estar ligados e em chunks carregados, na mesma dimensão. Paredes não bloqueiam o sinal.

Se a placa não estiver vinculada, procura um núcleo carregado e próximo com placa igual. Shift + clique com uma placa na face da máquina receptora retira a placa daquela face. Quebrar a máquina devolve suas placas. No núcleo, Shift + clique serve para vincular as placas da mão.

## Outras mudanças

- **Purificador:** novas colocações ocupam 3 × 3 × 3, com dois reservatórios, tubulações e câmara central. Guarda 4 milhões de FE sujos e 2 milhões limpos. Converte cinco unidades sujas em quatro limpas, usando carvão como filtro. A versão grande processa até 80.000 FE sujos por ciclo de 10 ticks e exporta até 128.000 FE, conforme o cabo e o destino. Purificadores antigos compactos continuam utilizáveis; quebre e recoloque para adotar o modelo grande.
- **Gerador abissal:** pode ser colocado submerso e mantém a água dentro das 27 partes. A água não desmonta a estrutura. As condições de geração em oceano continuam valendo.
- **Cama:** a cama inteira muda de cor, incluindo colchão, travesseiro e estrutura, com intervalos apagados. Mantém o formato e o sono da cama normal. Dormir a noite completa concede Pressa I e Velocidade I por cinco minutos.
- **Morteiro:** projéteis mais rápidos, maior alcance e explosão maior. Mantém os seis canhões ajustáveis e o atraso de dois segundos depois da ativação.
- **Holofote:** projeta um cone de luz até 48 blocos à frente, bloqueado por obstáculos. A luz acompanha a cabeça móvel e atinge nível 15 nos pontos iluminados. Consome 250 FE por ciclo de 10 ticks.

### Mirar o holofote ou o morteiro

Segure o controle de mira, use Shift + botão direito na máquina para vincular e botão direito no ar para ativar. Movimente sua visão: a cabeça acompanha sua mira em 360° na horizontal. No holofote, o cone é atualizado junto com a direção. Botão direito novamente fixa a mira. No morteiro, o scroll seleciona todos ou um canhão e M1 inicia a sequência de disparo. O controle funciona até 256 blocos com o local carregado.

O Minecraft usa iluminação por blocos: a luz se difunde ao redor dos pontos do cone; não é um efeito de shader ou um raio perfeitamente recortado. A origem não ilumina igualmente em todas as direções.

## Nove componentes avançados

| Componente | Papel na progressão |
|---|---|
| Liga reforçada | Estrutura resistente, combinando materiais de ferro, diamante e netherita |
| Bobina supercondutora | Transferência de energia de alta potência |
| Circuito quântico | Controle das máquinas avançadas |
| Cristal sintético | Foco de energia e componentes superiores |
| Câmara de pressão | Contenção de gás e energia |
| Servomotor | Movimento de aparelhos grandes |
| Célula de plasma | Armazenamento energético intermediário |
| Matriz dimensional | Núcleos e máquinas de alto nível |
| Semente de singularidade | Etapa final do núcleo quântico, com estrela do Nether |

As receitas da broca, exterminador, nivelador, projetor, reator, gerador abissal e purificador agora usam essa progressão. Máquinas já colocadas permanecem no mundo. Todas as receitas estão nos arquivos de dados e funcionam na bancada; os itens aparecem na aba criativa Maré Elétrica.

Veja `VALIDACAO.md` para o que foi testado e as limitações da validação. As funções das versões anteriores continuam descritas nos guias correspondentes.
