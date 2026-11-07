# Maré Elétrica 0.9.0 — NeoForge 1.21.1

## Projetor

Abra com o botão direito. Arraste com o botão direito para girar a construção em qualquer direção; botão do meio move a imagem; roda do mouse aproxima. Escolha uma cor à direita e clique num bloco para pintar. Shift + clique pinta todos os blocos do mesmo material. Os pontos brancos na paleta indicam cores usadas. Para acessar blocos internos, escolha o eixo e avance as camadas com +/−; “Tudo” mostra a construção inteira.

“Esticar” abre largura, altura e comprimento, nessa ordem. Digite e pressione “Aplicar”. A escala reproduz os blocos existentes e a pintura. Limites: 128 por eixo, 65.536 blocos ocupados e 524.288 posições no volume. Um pedido acima do limite não apaga o projeto anterior.

“Distância” define o começo da construção de 2 a 128 blocos à frente. Ligue e clique em “Projetar”: a construção usa hologramas coloridos translúcidos, energia e nenhum material. Pintar e esticar pausam a projeção; clique em “Projetar” novamente para atualizar. “Repor” recupera a forma e as cores originais do projeto selecionado.

## Carrinho / explorador

A câmera fica atrás do veículo, até 7 blocos de distância, e se aproxima quando há uma parede. Seu personagem permanece parado. Sair restaura seu modo de câmera anterior.

| Ação | Controle |
|---|---|
| Movimento | W / S |
| Direção | Mouse e A / D |
| Atirar | Botão de ataque |
| Alternar avião | G |
| Subir / descer no avião | Espaço / Shift |
| Abrir carga remotamente | I |
| Sair do controle | R |

Na água ele vira um barco com casco completo; em terra volta às rodas. Quedas abrem as asas do planador. O avião tem fuselagem, asas, cauda e motores próprios; é um modo manual. Cada gás fornece 30 segundos de voo motorizado. O combustível restante é preservado ao desligar o avião. Ao acabar o gás, ele plana. Também precisa de bateria.

Clique no veículo para abrir seu painel: qualquer jogador próximo pode acessá-lo. Há um espaço de balas, um de gás e três espaços livres para itens. Balas passam para o carregador de até 256; o botão de descarregar permite recuperá-las e pausa a recarga brevemente. Os tiros do carrinho saem na horizontal a 6 blocos/tick, com alcance de até 120 blocos, e causam dano somente a mobs hostis. O carrinho só atira no modo manual.

## Gás

Receita simples: duas pepitas de ferro, duas garrafas de vidro e um carvão produzem quatro cilindros. Consulte o livro de receitas. Serve para avião, broca e transformação do exterminador.

## Broca 6×6×8

A haste telescópica prende a broca giratória ao suporte superior. A ponta desce somente até ficar sob o chão. A máquina produz minérios por levas e não escava os blocos reais abaixo dela.

Cada leva começa com 100 minérios. Cada um dos dez espaços de gás ocupado acrescenta 100, até 1.100. Cada espaço aceita um cilindro; a máquina consome um por espaço ao iniciar. Gasta 2.000 FE por minério. Saída cheia pausa a produção e preserva o resultado pendente; retire os itens ou conecte um duto. O progresso é salvo ao fechar o mundo.

Os números pedidos somam 196,0001%; foram interpretados como **pesos relativos**, com um resultado por sorteio:

| Minério | Peso |
|---|---:|
| Carvão | 54 |
| Ferro bruto | 42 |
| Ouro bruto | 32 |
| Cobre bruto | 65 |
| Diamante | 3 |
| Detritos ancestrais, material da netherita | 0,0001 |

## Extermina Monstro

Coloque a base numa área livre de 5×5×5, deixando espaço à frente. Conecte energia limpa, abra e ligue. Ela cria uma máquina que persegue hostis, dá investidas, corta de perto e atira à distância. Usa lâminas contra endermen, frio contra imunes ao fogo e fogo contra mortos-vivos. Consome 2.000 FE/s.

Coloque 20 gás na base e pressione “Aranha”. O guardião vira uma aranha 50×50×50, que fica de guarda e elimina hostis a até 60 blocos horizontalmente. A forma gigante consome 50.000 FE/s; use em local aberto. Seus ataques não escolhem jogadores nem animais passivos, nem destroem terreno. Desligar a base interrompe os ataques. Verifica até 24 alvos a cada meio segundo.

## Duto de itens

Monte: **baú de origem → dutos → baú de destino**. Comece colocando o primeiro duto contra o baú de origem: ele ativa a extração desse lado. Os seguintes transportam. Não exige aparelho transferidor nem energia.

Para dutos antigos ou ajuste manual, clique com a mão vazia na face oposta ao baú de origem; a mensagem confirma o lado de onde puxa. Outro clique deixa apenas transporte. Use uma entrada numa ligação simples entre dois baús. Redstone pausa a entrada.

Transporta até oito itens a cada oito ticks. Se o destino encher, os itens ficam no baú ou no buffer salvo do duto. Quebrar o duto derruba o conteúdo desse buffer. A busca alcança até 64 blocos de distância e 256 posições, sem carregar chunks novos.

## Duto de água

Monte **entrada azul → dutos de água → saída laranja**. Clique de mão vazia no lado desejado de um bocal para apontá-lo nessa direção. A boca azul deve tocar um bloco-fonte de água; a laranja deve apontar para ar. A entrada move uma fonte real até a saída. Saída bloqueada não consome água. Redstone pausa o bocal.

A água bombeada alcança até 14 blocos horizontalmente num piso plano e escoa para baixo. Não cria fontes infinitas por conta própria. A rede alcança até 64 blocos de distância e 256 posições; não bombeia água no Nether.

## Instalação

Feche o Minecraft, retire o JAR antigo do Maré Elétrica e coloque somente o JAR novo da pasta `INSTALAR` em `mods`. Em servidor, atualize os dois lados. Use Minecraft 1.21.1, NeoForge 21.1.172 ou posterior da linha 21.1 e Java 21. Não exige GeckoLib ou outros mods.

O DMT de Attack on Titan fica para a próxima versão. O pedido está registrado em `PROXIMA_VERSAO.md`.
