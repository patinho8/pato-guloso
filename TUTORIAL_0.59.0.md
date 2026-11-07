# Tutorial rápido — Maré Elétrica 0.59.0 (versão parcial, código-fonte)

## 1. Gerar o JAR
1. Instale o **Java 21**.
2. Dê dois cliques em `COMPILAR_WINDOWS.bat` (pasta `MareEletrica`).
3. O JAR sai pronto na pasta indicada pelo script. Se falhar, ele cria `COMPILACAO.log` — me mande esse arquivo.
4. Coloque o JAR na pasta `mods` (NeoForge 1.21.1), no lugar do da 0.21.0.

## 2. Controles
| Ação | Tecla |
|---|---|
| Atirar / Mirar | M1 / M2 |
| Recarregar (troca de pente) | R |
| Painel de acessórios da arma | J (Shift+M2 não abre mais) |
| Deitar | Shift 2× |
| Espiar | Segurar **Z** + **A** ou **D** (com arma na mão) |
| Inspecionar arma/faca | **V** |
| Granada | M2 com o item na mão (Shift + M2 = rolar por baixo) |
| C4 | Colocar no chão, clique direito, programar o tempo e armar |
| Desarmar C4 | Tesoura + clique direito |
Todas as teclas podem ser mudadas em Controles > Maré Elétrica.

## 3. Roupas de camuflagem
Abra o inventário: coluna à direita (C, J, P, B). Arraste a peça, use Shift+clique, ou segure a peça e aperte M2. Ficam por cima de qualquer armadura e **não dão proteção**.

## 4. Granadas e C4 (aba "Itens de guerra")
Frag (dano, não quebra bloco), Luz (cega), Fumaça, Incendiária. A C4 agora é um bloco com temporizador (minutos, horas ou dias do Minecraft). Receitas no `NOVIDADES_0.37.0.md`.

## 5. Armas e pentes
MP40 (32), MP5 (60), AK/M4A1/SMG (30), Desert 7, USP 12, M870 6, Sniper 5.

## 6. Facas
Borboleta, Karambit, Xenocaçador, HRT3 e Banana (aba Itens de guerra). Batem mais forte, mais rápido e fazem som de corte. Aperte V segurando uma para ver a animação de inspeção.

## 7. P90
Bullpup, pente de 50 (SMG), 6 skins.

## 8. Drone
Coloque o Drone no chão, instale a Bomba (clique direito no drone com a bomba), segure o Controle de Drone e clique direito no ar. WASD voa, Espaço sobe, Shift desce, Ctrl turbo, B abre a escotilha e solta uma bomba (até 10, ficam escondidas dentro do drone), X sai. Não tem mais limite de distância — só a bateria. Recarregue a bateria com redstone ou bloco de redstone.

## 9. Roda gigante com motor
A roda só gira com motor: Manivela (clique direito, encostada na roda) ou Girador Automático alimentado por um Bloco de Controle (configure com um Módulo de Controle).

## 10. Armas novas
Glock 18 (20), SCAR-H (20), UMP-45 (25) e SPAS-12 (8), na aba Armas.

## 11. Mais armas
Beretta M9 (15), Uzi (32), FAMAS (25), M249 (100) e Barrett M82 (10).


## 12. Aba Armas
Todas as armas, munições, miras e acessórios agora ficam na aba **Maré Elétrica · Armas**. A aba Itens de guerra ficou com o resto (granadas, C4, drone, caça, lançador...).

## 13. Controle de mira com várias armas
Shift + clique direito com o Controle de Mira em cada morteiro ou Lançador de 10 bocas (até 16). Clique de novo numa arma para tirá-la. Clique direito no ar ativa: todas viram para o ponto que você está olhando, e o clique esquerdo dispara todas juntas.

## 14. Lançador de 10 bocas
Coloque o bloco (ele aponta para onde você está olhando). Clique com Foguetes do lançador para carregar (até 1000). Clique direito vazio = salva de 10 foguetes, Shift + clique = liga/desliga o ATAQUE AUTOMÁTICO (ataca sozinho monstros entre 20 e 200 blocos), sinal de redstone também dispara. Com o Controle de Mira ele calcula sozinho a inclinação para cair exatamente onde você está olhando (alcance ~230 blocos). Os foguetes sobem, fazem a curva e caem assobiando. Receita: 3 barras de ferro / chapa, módulo de controle, chapa / 3 blocos de ferro. Foguetes: pepitas de ferro, pólvora e pó de blaze (rende 8).

## 15. Caça
Coloque o caça no chão (precisa de espaço) e clique direito para entrar. W/S = motor (S com o motor em 0 = freio aerodinâmico / freio das rodas), mouse para cima/baixo = arfagem, mouse para os lados = rolagem (dá para girar o avião inteiro), A/D = leme. Clique esquerdo ou Espaço = metralhadoras das asas, só quebra levando tiro (ou batendo), B = solta a bomba grande (instale clicando no caça com a Bomba do caça). Shift sai. Para decolar: motor no máximo, corra pela pista e puxe o mouse para trás. Se ficar lento demais ele perde sustentação (STALL). Bater rápido em parede ou chão = explode. Shift + clique vazio com ele no chão guarda o caça.

## 16. Granadas e C4
A Flash cega até de longe (64 blocos) se você estiver olhando para ela. As granadas fazem barulho rolando no chão; a de dano tem explosão própria, bem mais alta. A C4 pode ser programada em segundos e a explosão ficou bem maior.


## 18. C4
Armada, a C4 mostra a contagem regressiva em vermelho na telinha dela. Na parede ela fica com a tela para cima.

## 19. Caça (0.40.0)
Até 3 bombas (clique no caça com Bomba do caça 3 vezes). B dispara uma bomba com propulsão para frente. Metralhadoras mais rápidas e derrubam outros caças. Abatido, ele perde o controle e cai em parafuso pegando fogo; aperte Shift para sair (o paraquedas não abre sozinho: use-o no ar).

## 20. Paraquedas
Use no ar (clique direito). W plana para frente, o mouse guia, Shift solta. Receita: 3 lãs brancas, 2 linhas, 1 couro.

## 21. Sinalizador
Clique direito: sobe ~25 blocos, queima numa cor forte, solta fumaça e desce devagar iluminando de verdade a área por ~45 s; dá para ver de muito longe. Cores: vermelho, verde, amarelo, magenta, laranja e ciano. Receita (sem forma): pólvora, redstone, pó de pedra luminosa, papel + corante (vermelho, lima, amarelo, magenta, laranja ou azul-claro) → 4.

## 22. Lançador de 10 bocas
Clique direito vazio: ele gira para o lado em que você está olhando e dispara (gira 360°). Pelo Controle de Mira pode mirar em qualquer direção e até quase na horizontal.

## 23. Drone (0.41.0)
Cada controle precisa ser conectado: Shift + clique direito no drone segurando o controle. Depois o controle só pilota aquele drone. A bomba explode um pouco mais.

## 24. Superaquecimento
Máquinas que gastam energia esquentam enquanto trabalham (o calor aparece na tela da máquina). Acima de 75% soltam fumaça: desligue para esfriar. Em 100% elas PIFAM e param — conserte clicando com 1 bloco de ferro. Geradores, rodas e núcleos não esquentam.

## 25. Armaduras com poder
Cinco conjuntos (aba Armaduras): Salto (superpulo, sem dano de queda), Velocidade, Voo (aperte espaço duas vezes para voar), Titã (força, resistência, não é empurrado) e Sombra (visão noturna, respira na água, invisível agachado). O poder só funciona com o conjunto completo e com carga: Shift + clique direito com o PEITORAL na mão gasta redstone do inventário (pó = 3%, bloco = 30%; dura ~5 min, voando ~2,5 min). A carga aparece no canto da tela. Receitas: como armadura de ferro, com o item especial no meio (gosma, açúcar, membrana de phantom, diamante, pérola do fim).

## 26. Cama LED
Agora ilumina o chão e o ar em volta com a cor em que está.

## 27. Exterminador e Recarregador de gás menores
Agora ocupam 3x3x3 blocos (antes 5x5x5), com o mesmo visual em tamanho menor.

## 28. Lançador antiaéreo (0.44.0)
Coloque e carregue com Mísseis teleguiados (até 32). Clique vazio liga/desliga. Ele trava em caças e drones a até 160 blocos (nunca no dono) e dispara mísseis que perseguem o alvo. Para escapar: manobre forte (o míssil vira menos que o caça e perde a trava se o alvo passar para trás dele) ou aperte G no caça para soltar despistadores. No caça/drone aparece o aviso "MÍSSIL!" com bipes.

## 29. Portão automático
Abre sozinho quando um jogador chega perto (ou com redstone) e fecha depois. Coloque vários lado a lado (e um em cima do outro) para um portão grande. Clique direito: automático → sempre aberto → trancado.

## 30. Rodas gigantes em corrente
Uma roda gigante girando faz girar a roda encostada atrás/na frente dela (mesmo eixo), e assim por diante.

## 31. Câmeras
A visão da câmera de segurança agora é fixa (o mouse não mexe). Setas trocam de câmera, X sai.

## Novidades da 0.44.0
- **Armas no chão:** agache (Shift) e clique direito no topo de um bloco com a arma na mão. Clique direito nela para pegar de volta.
- **Metralhadora fixa:** coloque, clique direito para sentar, segure o botão de atirar. Munição: balas de rifle (clique com elas na mão).
- **Canhão de campanha 3x3:** sente e mire com o mouse, ou vincule ao Controle de mira (clique no canhão com o controle) e mire de longe. Munição: balas de canhão.
- **Binóculo:** segure clique direito; mostra a distância em blocos até onde você mira e carrega mobs/terreno longe, igual à sniper.
- **Óculos térmicos:** vão no espaço do capacete. Mobs ficam brilhando e a tela fica com cor de visão de calor.
- **Lanterna** (clique direito liga/desliga), **GPS** (Shift+clique marca um ponto e ele mostra direção e distância), **Rádio** (clique envia sua posição pra quem tem rádio).
- **Facas:** batidas seguidas em até 0,6 s fazem um combo de 4 golpes diferentes.
- **Projetor:** 5 construções de concreto novas (casa, prédio, bunker, garagem, muro).

## Novidades da 0.45.0
- **DMT novo — puxar blocos:** com o DMT ligado, **segure o botão direito** (mais ou menos meio segundo). O cabo arranca o bloco (o que o gancho direito estiver preso, ou o que você estiver mirando até 24 blocos) e ele fica pendurado na corda na frente de onde você mira.
  - Mexa a câmera rápido pra **bater nos mobs** com o bloco: quanto mais rápido, mais dano e empurrão.
  - **Solte o botão** pra **arremessar** o bloco. Quando cai, ele se quebra (não dropa nada).
  - Gasta 1,5 s de gás por bloco. Não puxa baús/máquinas (blocos com conteúdo), bedrock ou blocos mais duros que obsidiana.
- **Bola de tênis:** clique direito arremessa (agachado = arremesso fraquinho). Ela quica em paredes e no chão e, quando para, vira item pra pegar de volta. **Lobos domesticados buscam a bola** e trazem pra você. Receita: slime + corante verde-limão + lã branca = 4 bolas.

## Novidades da 0.46.0
- **Energia eólica (10 aparelhos):** Turbina Pequena, Média e Gigante, Cata-vento de Fazenda, Turbina de Telhado, Mastro Anemômetro, Bateria Eólica, Inversor Eólico, Painel de Controle Eólico e Cabo Eólico.
  - As turbinas geram mais **quanto mais alto** estiverem, em **campo aberto** (sem blocos em volta) e com **chuva/tempestade**. Precisam de espaço livre em cima (a Gigante precisa de 9 blocos).
  - Ligue as turbinas com cabos a um **núcleo**, à **Bateria Eólica** ou ao **Inversor Eólico** (os dois funcionam como núcleo e mandam a energia para as máquinas).
  - **Mastro Anemômetro:** turbinas a até 24 blocos rendem +20%.
  - **Painel de Controle:** mostra a geração somada das turbinas a até 32 blocos; desligue o painel para frear todas.
  - **Turbina de Telhado:** +50% em cima de um prédio.
  - **Cabo Eólico:** leva 4x mais energia que o cabo isolado.
- **Tênis:** **Raquete** — clique direito joga uma bola pra cima na sua frente (precisa ter bola no inventário); **clique esquerdo perto da bola** rebate pra onde você olha (mais forte se a bola estiver a uns 2 blocos). **Rede de tênis:** coloque várias lado a lado; a bola quica nela.
- **Canhão de campanha:** agora sem rodas, maior e fixo numa base de concreto. Você senta atrás da culatra e o assento gira junto com o canhão. Continua funcionando com o **Controle de mira** (Shift + clique no canhão com o controle).
- **Metralhadora fixa:** maior e atira **balas de verdade** (traçantes que voam e acertam), igual às metralhadoras do **caça**.
- **Portão automático:** um item já coloca o portão inteiro (3 de largura x 2 de altura). Quebrar um pedaço tira o portão todo.
- **Sinalizador:** mantém a chunk dele carregada para todo o servidor enquanto queima.
- **Câmera de segurança:** sem braço na tela, lente grande-angular e visual de câmera de vigilância (granulado, linhas, cantos de mira).
- **Aparelhos portáteis em 3D** (binóculo, lanterna, GPS, rádio, óculos térmico, Danoninho e bola de tênis). O detector de minérios foi retirado.

## Novidades da 0.47.0 — tênis de verdade
- **Sacar:** com a raquete na mão, clique direito joga a bola pra cima na sua frente. Espere ela cair e bata.
- **Rebater:** clique esquerdo quando a bola estiver perto (uns 2 blocos é o **ponto doce**, entre o joelho e o ombro). A bola vai **para onde você mira** no chão, fazendo a curva certa pra cair lá.
- **Tipos de golpe:**
  - Normal = **topspin** (cai rápido e quica pra frente).
  - Correndo = **pancada** (bem forte).
  - Agachado = **slice** (vai mais devagar, quica baixo e freia).
  - Agachado olhando pra cima = **balão** (bola alta).
- **Timing:** bater adiantado/atrasado deixa a bola mais fraca e torta; muito errado pega no aro e a bola sobe torta. Aparece na tela como foi a batida.
- A bola **passa por quem está de raquete** (não bate no corpo) e **morre na rede**.
- Na primeira pessoa a raquete faz o movimento de forehand de verdade.

## Novidades da 0.48.0
- **Raquete** com visual novo (desenho limpo de raquete, segurada igual uma ferramenta).
- **Marca-pontos:** placar de LED com 3 blocos de largura (aba "Itens de guerra").
  - Clique direito na **metade esquerda** = +1 para CASA; na **metade direita** = +1 para VISITANTE.
  - **Shift + clique** numa metade = -1.
  - Clique no **meio** troca entre placar normal e **modo tênis** (0, 15, 30, 40, AD, com games e sets automáticos). **Shift + clique no meio** zera tudo.
- As **camuflagens** saíram da aba de armaduras (continuam na aba "Camuflagem").

## Novidades da 0.49.0
### Helicóptero
- Coloque no chão e clique para entrar (até **4 pessoas**; quem entra primeiro pilota).
- **Espaço** sobe, **Ctrl** desce, **W/S** frente/ré, **A/D** gira. Sem apertar nada ele **fica parado no ar**.
- **Clique esquerdo**: metralhadora (balas traçantes) para onde você olha. **Botão direito**: foguete.
- Munição: clique no helicóptero com **foguetes de barragem** (até 16). Mísseis teleguiados também perseguem helicópteros.
### Tanque de guerra
- **W/S** anda, **A/D** vira, o **mouse** mira a torre (ela gira devagar até onde você olha).
- **Clique esquerdo**: metralhadora; **botão direito**: canhão (recarrega em 3 s). Atropela mobs. Sobe degraus de 1 bloco.
- Munição: clique no tanque com **balas de canhão** (até 24). Leva 2 pessoas.
### Radar
- Mesa de radar 3x3: a tela redonda mostra tudo a até 128 blocos (norte para cima): **vermelho** = aviões, helicópteros, drones e mísseis; **amarelo** = jogadores e veículos. Mobs não aparecem.
- Quando algo voa a até 128 blocos, a **lâmpada vermelha acende** e ele **dá sinal de redstone** (dá pra ligar alarme).
- Clique nele para ver a lista de contatos com distância e direção.
### Porta blindada
- Não abre com a mão nem com redstone. Clique nela: abre um **teclado**. A **primeira senha** digitada vira a senha da porta (e você vira o dono). Senha certa = abre por 4 s.
- **Cartão de acesso:** Shift + clique direito na porta com o cartão para gravar; depois é só clicar na porta com o cartão.
- O dono apaga a senha com **Shift + clique com a mão vazia**.
### Posto de recarga de armadura e Hangar (máquinas, ligue com cabo)
- **Posto de recarga:** fique em cima usando armadura de poder: carrega até 100% em ~40 s.
- **Hangar de manutenção:** estacione o caça, o helicóptero ou o tanque em cima (área 5x5): conserta o casco e recarrega bombas/foguetes/balas. Um caça abatido que caiu volta a funcionar.
### Esportes (aba "Itens de guerra")
- **Bolas** ficam no mundo: **clique esquerdo** chuta/bate, **botão direito** pega, **botão direito com a bola na mão** joga.
- **Futebol:** encostar na bola andando = conduzir; clique = chute (correndo = chute forte; olhando pra cima = bola alta). **Gol de futebol** (3x2): bola dentro = **GOL** e ponto no marca-pontos mais perto.
- **Basquete:** jogue a bola na **cesta** (coloque com o lado aberto virado pra quadra). Cesta = 2 pontos; de longe (mais de 7,5 blocos) = **3 pontos**.
- **Vôlei:** clique = levantar a bola; pulando e olhando pra baixo com a bola acima da cabeça = **cortada**. Use a rede de tênis como rede. Pontos manuais no marca-pontos.
- Clique no gol/cesta para escolher qual time ganha o ponto ali (CASA ou VISITANTE).

## Novidades da 0.50.0
### Abas do criativo (tudo separado)
⚡ Energia • ⚙ Máquinas • 🔫 Armas • 💣 Explosivos e defesa • 🚁 Veículos • 🎒 Equipamentos • 🛡 Armaduras • 🌿 Camuflagem • ⚽ Esportes • 🔩 Materiais e comida.
### Roupas de esporte (modo de jogo)
Vista a **camisa** do esporte para entrar no modo de jogo (aparece "Modo ..." no canto da tela). Com a **roupa completa** (camisa + calção + chuteira/tênis) você fica mais rápido.
O clique só vira jogada quando tem bola perto (até 6 blocos); sem bola, o clique funciona normal (quebrar bloco, bater).
- **Futebol:** clique = **chuta com o pé** (você vê sua perna subindo). Correndo = chute forte; olhando pra cima = bola alta.
- **Goleiro:** clique = **agarra a bola** (se estiver um pouco longe, você **mergulha** nela). Com a bola na mão: clique = **bicuda**, botão direito = **joga com a mão**.
- **Basquete:** clique = pega/rouba a bola e sai **quicando**. Com a bola: clique = **arremesso** (mira sozinho na cesta para onde você olha; pulando acerta mais), botão direito = **passe**.
- **Vôlei:** clique = **manchete** (vai para onde você mira); bola acima da cabeça = **levantamento**; **pulando** = **cortada**.
- **Tênis:** a raquete alcança mais longe e o ponto doce fica mais fácil.
### Maior e mais bonito
- **Helicóptero** e **tanque** 50% maiores, com modelos e texturas novas (painéis, rebites, vidro, estrela, faixas, lanternas). Os passageiros sentam **dentro** da cabine do helicóptero.
- **Radar** virou uma **mesa grande 3x3** com tela redonda e antena giratória.

## Novidades da 0.51.0 — Base e comando
### ⚡ Energia mais difícil (nerf)
- Os **núcleos distribuem bem menos** por segundo (pequeno 6 mil FE/s, médio 20 mil, grande 80 mil, quântico 500 mil).
- **Tensão:** cada núcleo solta uma tensão (pequeno = baixa, médio/bateria eólica = média, grande/inversor = alta, quântico = extrema). Cada máquina aguenta até uma tensão (aparece na descrição do item).
- Se uma máquina recebe **tensão maior que aguenta**, ela solta faíscas ("SOBRETENSÃO") e em ~10 s **PIFA** (conserte com 1 bloco de ferro).
- **Transformadores** (baixa/média/alta): coloque no meio do cabo e a energia sai naquela tensão.
### 🏰 Painel de Comando (mesa com 3 monitores)
Clique para abrir. Funciona em tudo a até 64 blocos. Abas:
- **Resumo:** energia guardada, geração, consumo e máquinas pifadas/em sobretensão (com posição).
- **Radar:** tela de radar com contatos (vermelho = aeronave inimiga, azul = aeronave amiga, verde = amigos, amarelo = outros).
- **Mapa:** mapa visto de cima (100x100 blocos) com os contatos.
- **Câmeras:** botão para ver cada câmera de segurança perto.
- **Defesa:** armar alarme, modo das torretas (desligadas / só monstros / monstros + intrusos), escudo, abrir/fechar portões, chamar helicóptero ao heliponto.
- **Membros:** quem é da base (quem coloca o painel já é membro). Membros não disparam o alarme.
### 🚨 Alarme
Com o alarme armado, aeronaves de fora, jogadores que não são membros (até 32 blocos) ou monstros perto (16 blocos) disparam: **sirenes** tocam e giram, as **portas blindadas fecham e trancam** para quem não é membro.
### 🛡 Gerador de Escudo (máquina, alta tensão)
Cúpula azul de raio 16: explosões não quebram nada nem machucam lá dentro, e tiros vindos de fora param na cúpula. Gasta muita energia.
### 🚗 Garagem e veículos
- **Garagem de Veículos:** estacione o helicóptero/tanque vazio no pátio 7x7 em frente e clique na garagem: **guardar** (até 6), **tirar**, e **pintar** (oliva, deserto, floresta, neve, preto, urbano, marinha).
- **Kits** (clique no veículo): blindagem (+25% casco), motor (+12% velocidade), munição (+50%) — até nível 3 cada.
- **Combustível:** helicóptero e tanque gastam combustível. Recarregue com o **galão** (clique no veículo) ou no hangar.
- **Controle da garagem:** Shift + clique na garagem para ligar. Clique direito: o veículo **sai da garagem e vem até você** sozinho (helicóptero voa e pousa, tanque vem dirigindo).
- **Portão de Garagem:** 5x4 de uma vez, abre clicando, pelo painel ou **sozinho quando um veículo seu chega perto**.
- **Elevador de Veículos:** plataforma 3x3. Coloque **Paradas** em cima na mesma coluna (deixe o poço 3x3 livre). Clique no elevador/plataforma/parada ou ligue redstone. **Saia do veículo** antes de subir.
- **Heliponto:** clique nele (ou no painel) e o helicóptero vazio mais perto vem pousar sozinho.
- **Luzes de pista:** coloque em fila; a luz corre para onde você estava olhando ao colocar.
### 📦 Drone de entrega
Estação de entrega (baú de 27). Com a **etiqueta de rota**: clique na estação de destino e depois na que envia. Ponha um **drone de entrega** no 1º espaço: a cada 30 s ele leva até 9 pilhas para a outra estação e volta.
### 🏠 Base portátil
Clique no chão: monta um bunker 9x9 na sua frente com porta blindada, painel, sirene, núcleo, painéis solares, luzes, baú e cama.

## Novidades da 0.53.0
- **Binóculo:** enquanto você olha por ele, a câmera mexe bem devagar (mira firme de longe).


## Novidades da 0.53.0
- **Q** perto da bola faz a jogada especial: cavadinha (futebol), soco (goleiro), enterrada ou passe quicado (basquete), manchete (vôlei). No basquete, o **M1** arremessa.
- **Lâmpadas LED** de 10 cores (aba Energia). Clique para ligar e desligar.
- O **elevador** é uma plataforma de 5x5, só para pessoas.
- O **drone de entrega** pode ser estacionado no chão: clique nele para pegá-lo de volta.
- O **DMT** segura o bloco num cabo só, como um pêndulo.
- Blocos grandes (radar, painel, gol, cesta, heliponto, elevador) ocupam os blocos certos e precisam de espaço livre.


## Novidades da 0.54.0 — Galinha de Briga
- Choque a galinha clicando no chão. Clique nela para ela te seguir ou esperar, e agache + clique para ligar o modo briga.
- Galinhas de donos diferentes, as duas em modo briga, brigam sozinhas: bicada, voadora, furacão de asas e fúria. Com o nível sobem a bicada do trovão (nível 5) e o ovo-bomba (nível 8).
- O **Apito de Briga** manda atacar (clique no alvo), faz a voadora (clique no ar) e o furacão de asas (agachar + clique no ar).
- A perdedora fica nocauteada, não morre. A vencedora sobe de nível.


## Novidades da 0.55.0 — Ringue de Galos
- Coloque o **Ringue de Galos**. Com sua galinha perto, clique no ringue; o amigo faz o mesmo e a batalha por turnos começa. Sozinho, agache + clique para treinar contra o computador.
- Escolha 1 dos 4 golpes por turno. Os tipos funcionam como no Pokémon: Fogo ganha de Planta, Água ganha de Fogo e assim por diante.
- São 9 raças de galo, cada uma com sua cor e seu tipo. O Ovo Surpresa choca uma raça aleatória, e cada Ovo de raça sai do Ovo Surpresa + um item.


## Novidades da 0.56.0
- **Helicóptero** em primeira pessoa: cabine com painel e HUD verde (mira, bússola, horizonte, altitude, avisos). O **tanque** ganhou luneta de mira.
- **Esportes**: com a camisa vestida, use **Q, R, C e G** para os 4 golpes especiais de cada esporte. A lista aparece no canto da tela, e as teclas mudam em Controles.


## Novidades da 0.57.0
- **Vôlei**: G = bloqueio. Para o saque viagem, corra e aperte o botão direito.
- **Campos prontos**: clique no chão com o kit para ver a área e clique de novo para construir.
- **Cassino**: troque ouro, esmeralda ou diamante por fichas, jogue nas 12 máquinas e troque as fichas no Balcão de Prêmios.


## Novidades da 0.58.0
- **Mina terrestre**: coloque, espere armar (luz vermelha) e agache + clique para desarmar.
- **Lança-mísseis portátil**: segure o botão direito no alvo até TRAVAR e solte para disparar.
- **Sinalizador de ataque aéreo**: jogue, saia de perto e as bombas caem em 5 s.
- O alarme da base só toca com aviões, helicópteros e tanques inimigos.


## Novidades da 0.59.0
- **B-2**: voa como o caça. Clique ou B solta 4 bombas em sequência (12 no total).
- **RPG**: botão direito mira, clique dispara. Segure a mira num veículo até TRAVAR e o míssil persegue o alvo.
- A **mina** agora destrói tanques. O **ataque aéreo** traz um B-2 que solta as bombas.
