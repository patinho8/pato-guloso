# Maré Elétrica 0.37.0 — PARCIAL (código-fonte, ainda sem JAR)

Esta versão reúne as levas 1 a 3 da 0.37.0. **Não foi compilada nem testada em jogo** (o ambiente onde foi escrita não tem NeoForge/internet). Compile com `COMPILAR_WINDOWS.bat` e teste antes de usar em mundo importante. Veja também `TUTORIAL_0.37.0.md`.

## Feito nesta leva
- **Roupas de camuflagem não dão mais proteção** (armadura 0 em todas as peças). Continuam só visuais.
- **Shift + M2 não abre mais a oficina da arma.** Agachar e mirar agora só mira. A oficina abre só pela tecla **J**.
- **Pentes com capacidades diferentes:** MP5 agora tem pente de **60**. As demais ficam como estavam (AK 30, M4A1 30, Desert 7, USP 12, M870 6, Sniper 5, SMG Vetor 30). Pentes já existentes continuam válidos; só ganham espaço.
- **Pistolas menores** na mão (escala .78 → .62).
- **Parte de trás da arma** (coronha): rifles ficam mais afastados da câmera, principalmente mirando, para a traseira não ser cortada. Pode precisar de ajuste fino (ver abaixo).
- **Recarga = troca de pente:** o pente velho sai para baixo e cai, a mão vai ao cinto, e um pente novo sobe de baixo para dentro da arma. Antes o mesmo pente saía e voltava.
- **Mira de ponto vermelho** no lugar da retícula comum do Minecraft enquanto houver arma na mão (ponto de 4x4 px com borda escura).

## Ajuste da mira (leva 1b)
- Ponto das miras (todas as armas) aumentado para tamanho médio (~3x o anterior).
- Retícula da sniper: ponto central médio com borda escura; linhas longas com marcações mantidas.

## Leva 1c
- Ponto vermelho do centro reduzido (2x2 px com borda de 1 px).
- Coronha: o modelo de primeira pessoa omitia coronha/apoio de bochecha de propósito. Agora, sem mirar, a arma completa (com coronha) aparece; ao mirar, volta o modelo sem coronha para não tampar a visão.

## Leva 2 — granadas e C4 (aba "Itens de guerra")
Todas arremessadas com **M2**; têm receita (mesa de trabalho, sem forma) e estão no criativo.
| Item | Efeito |
|---|---|
| Granada de fragmentação | Explode após ~3 s. Dá dano de explosão e empurra, **não quebra blocos** |
| Granada de luz | Cega quem estiver a até 16 blocos com linha de visão; mais tempo para quem está olhando para ela |
| Granada de fumaça | Nuvem de fumaça por ~15 s |
| Granada incendiária | Põe fogo nos seres a até 4 blocos, **sem queimar blocos** |
| C4 | Lançada, gruda onde cair. Explosão forte que **quebra blocos** |
| Detonador | M2 detona todas as suas C4 em até 96 blocos |

Receitas: frag = ferro + 2 pólvora + sílex; luz = ferro + pólvora + 2 pó de glowstone; fumaça = ferro + pólvora + 2 carvão vegetal; incendiária = ferro + pólvora + 2 pó de blaze; C4 = 2 TNT + redstone + ferro; detonador = alavanca + redstone + ferro.
Os modelos são caixas simples com texturas do próprio Minecraft (provisórios).

## Leva 3 — o resto da lista
- **MP40** nova (pente de 32, munição de SMG, 6 skins, miras, silenciadores). Receita: igual à MP5, com tinta marrom. A textura reaproveita a da MP5 (o gravado "MP5" no metal ainda aparece).
- **M4A1 com modelo novo:** guarda-mão redondo com trilho e fendas, bloco de gás, anel delta, coronha retrátil com ranhuras, funil do carregador e pente curvo.
- **Slot de camuflagem (lado direito do inventário):** 4 espaços em coluna (C capuz, J jaqueta, P calça, B botas). A roupa fica **por cima de qualquer armadura** (vanilla ou de outros mods). Também dá para equipar com **M2** (botão direito) segurando a peça, ou com Shift+clique. As peças **não vão mais nos espaços de armadura**. Ao morrer, caem (a menos que keepInventory). Visível para outros jogadores.
- **Espiar (corrigido na leva 4):** tecla nova em Controles ("Espiar", padrão **Z**). Segure Z com uma arma na mão e aperte **A** (esquerda) ou **D** (direita): você deixa de andar de lado e se inclina (câmera inclina, arma desloca, corpo inclina para quem vê). Não funciona deitado.
- **Deitar militar:** ao ficar deitado (Shift 2×), o corpo balança, as pernas se abrem e arrastam alternadas enquanto você anda, e sem arma os braços puxam o corpo.

## Pendências conhecidas
- **Mira da sniper "igual à antiga":** não foi feita, falta uma referência (print) da antiga.
- Tudo desta versão está **sem compilar e sem teste em jogo**. Pontos mais prováveis de precisar de ajuste: direção da inclinação da câmera ao espiar (se for para o lado errado, inverta o sinal em `LeanClient.camera`), posição da coluna de camuflagem no inventário criativo, tamanho do ponto vermelho, aparência da M4A1/MP40, posição da arma na mão.

## Leva 4
- **Espiar de verdade:** ao segurar Z + A/D, o jogador dá um passo lateral de ~0,4 bloco (a câmera, a arma e a hitbox acompanham, e a colisão com paredes é respeitada). Ao soltar, volta. A inclinação do corpo agora pivota no quadril, então o **tronco não se solta mais das pernas**. A câmera inclina só um pouco (5°); se for para o lado errado, inverta o sinal em `LeanClient.camera`.
- **Ponto vermelho** um pouco menor (2x2 com halo bem fraco).
- **Deitado mais rápido** (lentidão de -45% para -28%).
- **P90** (bullpup, pente de 50 em cima, 6 skins, miras e silenciadores). Receita: igual à MP5, com quartzo.
- **Texturas melhores** em todas as armas: granulado e manchas na pintura, riscos, metal escovado, borracha com textura de empunhadura e madeira com veios. Script: `tools/enhance_textures.py` (originais em `tools/atlas_originals`).
- **Inspecionar (tecla V):** a arma gira na mão (um lado, o topo, o outro lado). Com faca, ela dá um giro completo e mostra a lâmina. Cancela ao atirar, recarregar, mirar, correr ou trocar de item.
- **(substituído na leva 5)** Facas (aba "Itens de guerra"): Faca Borboleta, Karambit, Xenocaçador, HRT3 e Banana. Todas com modelo 3D. Faca = espada de ferro mais rápida e leve. Banana: come com M2 (4 de fome) e bate fraco. Modelos gerados por `tools/make_knives.py`.
  Observação: o Xenocaçador e a HRT3 foram desenhados por mim (sem referência); com um print eu refaço fiel.

## Leva 5
- **Rastejar militar (principal):** deitado e andando, o corpo ondula (ombros giram), o joelho oposto empurra e arrasta, e sem arma os cotovelos puxam alternados. Em primeira pessoa a câmera e a arma balançam enquanto você se arrasta.
- **C4 nova:** agora é um **bloco** verde (estilo CS), maior, colocado no chão (não é mais arremessável). Clique direito abre uma tela para programar o tempo: **minutos reais, horas reais ou dias do Minecraft** (ex.: 4 dias do mine = 96 000 ticks). Depois de armada, clique direito mostra o tempo restante; **tesoura** no clique direito desarma. Bipes cada vez mais rápidos. O **Detonador foi removido**. Explosão forte (quebra blocos).
- **Inspeção própria por arma** (tecla V): Desert Eagle (gira e dá cambalhota), USP, M4A1, AK-47, M870, MP5 (giro sobre o cano), SMG, Sniper (fica em pé), MP40, P90 (cambalhota). Cada faca também tem a sua.
- **Facas redesenhadas** (células de 0,25, lâmina chanfrada, texturas com escovado): Borboleta maior (cabos vazados e latão), Karambit com anel, Xenocaçador com núcleo brilhante, HRT3 com serrilha, Banana maior com sulcos.
- **Mão segurando a faca em primeira pessoa:** seu braço aparece, com a faca na mão. Pegadas: frontal (borboleta, Xenocaçador, HRT3) e a karambit com o anel embaixo e a garra para frente.
- **Borboleta fechada na mão; abre ao bater** e fecha de novo.
- **Animação de ataque própria de cada faca:** borboleta (estalo de pulso), karambit (corte lateral), Xenocaçador (estocada), HRT3 (golpe de cima), banana (tapa).
- **Modelos das armas melhores:** pentes curvos (AK, MP5, M4A1), Desert Eagle com cobertura do cano, trilho e freio de boca, USP com trilho sob o cano e alavancas, AK com tampa, bloco de gás, mira e vareta, sniper com freio de boca e ferrolho, M870 com cartuchos laterais. Prévia lateral: `python3 tools/preview_guns.py`.

Limitações honestas: o braço na primeira pessoa é o bloco do braço do jogador (sem dedos separados); todos os valores de posição/ângulo das mãos e animações foram calculados, não vistos no jogo.

## Leva 6 (correções após teste)
- **Mão com a faca:** a posição da mão ficava abaixo da tela por causa de um erro no progresso de equipar. Corrigido.
- **Sniper preta e roxa:** o freio de boca que eu adicionei ultrapassava o limite de coordenadas dos modelos (-16 a 32), então o Minecraft recusava o modelo. Corrigido e conferido em todos os modelos.
- **Karambit:** lâmina bem menos curvada (uma foice suave em vez de gancho).
- **Inspeções:** rifles e SMGs agora são sutis (pequenas inclinações, sem a arma deslizar para o lado). Pistolas mais inventivas: Desert Eagle joga e gira no ar, USP gira em torno do cano, Python faz o giro de pistoleiro (duas voltas).
- **Revólver Python .357** novo: tambor com estrias, cano com nervura e engate, cão e cabo de madeira. Pente de 6 (a recarga troca as balas do tambor). Munição do revólver, 6 skins, miras e silenciadores. Receita igual à MP5, com ouro.
- **Mira da sniper (estilo largo):** barra horizontal larga (fina no centro, grossa nas pontas), haste longa para baixo e haste curta para cima, com o ponto vermelho no meio. Falta confirmar se é isso mesmo.

## Leva 7
- **Borboleta abre:** o código lia a posição errada do "aberta/fechada" e nunca abria. Agora abre ao bater no ar e abre/fecha durante a inspeção (V).
- **Karambit:** pegada igual à foto (cabo atravessando a palma, lâmina para frente, garra para cima, anel atrás, face da lâmina de lado), em primeira e terceira pessoa (F5). Lâmina bem mais curta e com curva suave. Corte lateral com o braço varrendo da direita para a esquerda e **a câmera acompanha** (roll/yaw), mais forte na karambit.
- **Animação da faca só quando acerta o ar** (ou bloco). Ao acertar um animal/mob a animação não toca.
- **Dá para correr enquanto inspeciona.**
- **Mira dos fuzis como as reais** (3D, na lente): 1x = holográfica com anel + ponto; 2x/3x = ACOG com chevron e marcas de queda; 4x/6x = cruz duplex com mil-dots. Pistolas e SMGs continuam com ponto vermelho.
- **Sniper:** ao mirar mostra sempre a lente escura com a mira larga (barra horizontal larga, haste longa para baixo, curta para cima), com ou sem luneta equipada, e a arma some da vista como na luneta.
- **Revólver:** textura nova (aço azulado/inox/níquel/ouro velho com gravação na carcaça, aço polido, cabo de nogueira quadriculado). Recarga nova: **o tambor abre para o lado**, as balas gastas caem, balas novas sobem e o tambor fecha.
- **P90:** o pente agora sai para cima e para trás (e o novo entra de cima), em vez de subir reto.
- **C4 em parede e teto:** pode ser colocada em qualquer face de bloco; cai se o apoio sumir.
- **Granada de fumaça** gera uma nuvem bem mais densa e maior (explosão inicial de fumaça + nuvem de 20 s).

## Leva 8
- **Recarga da P90 de verdade:** a mão de apoio vai até o pente (em cima, atrás), **puxa para cima e para trás levando o pente junto**, joga fora, a mão vai ao cinto, volta **trazendo o pente novo** por cima e o desliza até encaixar, e volta ao apoio.
- **Recarga do revólver de verdade:** a arma inclina o cano para cima, o tambor abre para o lado, as cápsulas gastas caem, a mão vai ao cinto e **traz a carga rápida (balas juntas) até o tambor** (as balas novas não sobem mais do chão), insere, e a mão fecha o tambor.
- **Mão das facas:** mais para o centro da tela e com um "punho" mais grosso envolvendo o cabo.
- **Inspeções de faca (V; V de novo para parar):**
  1. Banana: gira na mão sem parar (loop).
  2. Borboleta: jogada para cima girando no ar e cai na mão. **Alterna a cada inspeção:** uma vez aberta, na próxima fechada.
  3. Karambit: a mão vai para a frente e a karambit gira em volta do anel, na frente da mão, sem parar (loop).
  4. Xeno Hunter: a mão sobe um pouco e a faca gira equilibrada na ponta do dedo.
  5. HRT3 (assumi que "zerofang" é esta): flutua acima da mão, girando como uma bola.

## Leva 9
- **Karambit (inspeção):** estilo Valorant. A mão vai para a frente e a karambit gira **de frente para a câmera, em volta do anel, à frente do punho** (antes girava de lado e atravessava a mão). Sem parar.
- **HRT3 (inspeção):** fica **deitada** (lâmina na horizontal) sobre a mão e gira como um rotor.
- **Banana (inspeção):** deita com a curva **para cima** (um sorriso) e gira como um prato giratório, sem parar.
- **Borboleta:** modelo novo (cabos vazados com recortes de verdade, bordas chanfradas, parafusos de latão, lâmina clip-point com canal). A inspeção agora **sobe devagar e cai mais rápido**.
- **Sem movimento de tela** nos ataques de faca.
- **Animação de bater:** agora tem temporizador próprio (≈0,6 s) e dispara a cada novo golpe no ar ou em bloco; ao acertar um animal/mob não toca.

## Leva 10
- **Braço das facas:** mais **fino** (antes parecia largo) e **sem o punho largo** que eu tinha adicionado. Na inspeção da karambit a mão fica mais baixa, deixando o braço esticado para a frente em vez de curvado para cima; a karambit gira em cima da ponta do dedo.
- **Banana (inspeção):** agora fica **em cima da mão**.
- **Prints com Win+Shift+S:** o jogo não pausa mais ao perder o foco, então a tela não borra nem abre o menu de opções/sair. (Desliga a opção "Pausar ao perder o foco" uma vez por sessão; dá para ligar de novo nas configurações.)

## Leva 11 (a partir dos prints)
- **Facas bem menores na tela:** estavam enormes (escala 0,55). Agora 0,34 (a **karambit um pouco maior: 0,40**) e a mão fica um pouco mais longe da câmera, o que também deixa o braço mais longo.
- **Mão não cai mais ao bater:** o "abaixar depois do golpe" do Minecraft não afeta mais a mão da faca. Só abaixa e sobe quando você troca de item.
- Ajustei as compensações de pivô das inspeções para a escala nova.

## Leva 12
- **Facas flutuando:** nas inspeções com a faca afastada da mão (karambit, Xeno Hunter, HRT3) agora aparece um **dedo fino** ligando a mão ao ponto onde a faca gira.
- **Banana entrando na mão:** a banana foi subida para ficar **apoiada em cima da mão** em vez de atravessar o braço.

## Leva 13 (a partir dos prints)
- **"Dedo" removido:** na karambit e no Xeno Hunter o **próprio braço se estende** até o ponto onde a faca gira (anel / pomo). Na karambit a mão fica baixa e o braço reto.
- **Banana:** menor (0,27) e bem mais alta, apoiada **acima** do braço para não atravessá-lo.

## Leva 14 — roda gigante movida a motor, Bloco de Controle, Girador Automático, Manivela
- **Roda gigante:** agora **só gira com motor** (não funciona mais com água). Quando gira, gera energia como antes (vai para o tanque "sujo" e precisa do purificador).
- **Manivela** (bloco novo): encoste em qualquer parte da roda e **clique com o botão direito**. Cada clique dá 5 s de rotação (acumula até 60 s) e gasta um pouco de fome. Receita: alavanca + placa de ferro + graveto + lingote de ferro.
- **Girador Automático** (aparelho novo): encoste na roda; gasta energia que recebe do Bloco de Controle e gira a roda sozinho. Rendimento de no máximo ~84 % (sempre perde energia, não dá para fazer energia infinita).
- **Bloco de Controle** (aparelho novo): conecte aos cabos. Recebe energia do núcleo e **repassa só aos Giradores Automáticos**, com:
  - **limite de energia** (10, 25, 50, 75 ou 100 %),
  - **ciclo ligar/desligar** (segundos ligado / segundos desligado; 0 = sempre ligado).
  Para configurar, **clique com o botão direito segurando um Módulo de Controle**. O botão direito sem item abre o painel normal.
  Importante: os núcleos agora **não alimentam o Girador diretamente**, só pelo Bloco de Controle.
- **Roda que some:** a caixa de renderização do bloco raiz era de 1x1x1; ao olhar de certos ângulos o canto saía da tela e o Minecraft escondia a roda inteira. Agora a caixa cobre a estrutura toda.
- **Ainda não feito:** haste mais curta (roda mais perto da outra).

## Leva 15 — Drone com bomba
- **Drone** (item): clique direito no chão para colocar. Tem 8 de vida.
- **Controle de Drone** (item): clique direito no ar para pilotar o drone seu mais próximo (até 64 blocos). A câmera vira a do drone. **WASD** voa, **Espaço** sobe, **Shift** desce, o mouse gira. **B** solta a bomba, **X** (ou clique direito no controle de novo) sai. Enquanto pilota, o seu corpo fica parado e os cliques ficam desligados.
- **Bomba de Drone** (item): clique direito **no drone** segurando a bomba para instalar (1 por vez). Ao apertar B ela cai e **explode ao tocar, quebrando blocos** (força 4).
- **Drone abatido a tiros** (as balas do mod acertam): perde o controle, **cai e explode** (força 4 se tinha bomba, 2 se não).
- Para recolher: **Shift + clique direito com a mão vazia** no drone (devolve o drone e a bomba).
- Receitas: drone = 4 penas + 2 placas de ferro + 2 bobinas de cobre + módulo de controle; controle = lingote de cobre + módulo + 3 placas de ferro; bomba (rende 2) = TNT + 2 placas de ferro + pólvora.

## Leva 16
- **Borboleta realista:** modelo novo de balisong de verdade — dois cabos de titânio **lado a lado** (canal por dentro, furos, parafusos, pinos de latão e trava na ponta) e lâmina clip-point com fio, contrafio e chanfro. **Abre como uma de verdade:** o cabo da mão fica parado, a lâmina gira no pino dele e o outro cabo dá a volta e encaixa do outro lado. Fechada, a lâmina fica escondida entre os cabos.
- **Animação de bater nos bichos:** agora a animação da faca toca também quando você acerta animais e mobs.
- **Karambit:** o golpe agora é um **corte de baixo para cima** (começa embaixo à direita e sobe para cima à esquerda).
- **4 armas novas:**
  | Arma | Tipo | Pente | Munição |
  |---|---|---:|---|
  | Glock 18 | pistola automática | 20 | USP |
  | SCAR-H | fuzil pesado (mira até 6×) | 20 | fuzil |
  | UMP-45 | submetralhadora | 25 | SMG |
  | SPAS-12 | escopeta (9 projéteis) | 8 | escopeta |
  Cada uma tem 6 skins, 3 miras, 3 silenciadores, pente próprio, inspeção própria e receita (igual à MP5, mudando o corante: cinza, laranja, cinza-claro, ciano).
- **Granadas com física de verdade:** quicam nas paredes e no chão perdendo força, **rolam** e param, afundam devagar na água e batem de volta em pessoas e mobs. **Shift + M2** rola a granada por baixo (arremesso curto).
- **Modelos novos das granadas:** frag redonda (estilo M67) com alavanca, pino e faixa amarela; luz cilíndrica com furos (M84); fumaça em lata alta (M18); incendiária com faixa vermelha. Elas giram no ar e ficam deitadas ao parar.

## Leva 17 — Drone melhorado
- **Modelo 3D novo:** corpo com cantos chanfrados e bateria em cima, **câmera em gimbal** na frente, braços em X, motores, hélices de duas pás com pontas laranja, **luzes de navegação** (verdes na frente, vermelhas atrás), antenas, trem de pouso e trilho de bombas. As hélices giram devagar parado e rápido quando você pilota.
- **Voo mais suave e rápido:** acelera e freia aos poucos, balança de leve parado no ar, a câmera inclina nas curvas e segue o mouse sem atraso. **Ctrl = turbo** (quase o dobro da velocidade).
- **Tela de câmera de drone:** bússola com graus, retícula, cantos de enquadramento, altitude, altura até o chão, velocidade em km/h, distância/alcance, **bateria**, **sinal** (cai com a distância), bombas restantes, tempo de queda da bomba e "REC". A barra de itens e os corações somem enquanto você pilota.
- **Até 3 bombas:** clique direito no drone com bombas para carregar (aparecem penduradas embaixo). B solta uma de cada vez. Abatido, explode mais forte quanto mais bombas carregar.
- **Bateria:** dura ~5 min de voo (turbo gasta 3×). Acabou, o drone desce devagar e pousa. **Recarregar:** clique direito no drone com **redstone (+20 %)** ou **bloco de redstone (100 %)**. Ao recolher (Shift + clique direito com a mão vazia), o item guarda a bateria e as bombas e mostra a barra de carga.

## Leva 18 — Mais skins
- **6 skins novas para cada uma das 15 armas** (agora são **12 por arma, 180 no total**), na aba Camuflagem:
  | Nº | Skin | Corante da receita |
  |---|---|---|
  | 7 | Tigre (laranja com listras pretas) | laranja |
  | 8 | Digital (camuflagem de pixels) | verde-limão |
  | 9 | Fibra de Carbono | preto |
  | 10 | Damasco (aço em ondas) | cinza-claro |
  | 11 | Hex Neon (colmeia brilhante ciano/roxa) | ciano |
  | 12 | Galáxia (nebulosa roxa com estrelas) | magenta |
  Mesma receita das outras skins (papel + corante + pepita de ferro + o item marcador da arma). Aplicam na oficina da arma (J), no espaço de pintura.
- Script das estampas: `tools/make_skins.py`.

## Leva 19 — 5 armas novas e mod mais leve
- **5 armas novas** (cada uma com 12 skins, 3 miras, 3 silenciadores, pente, inspeção e receita igual à da MP5 mudando o corante):
  | Arma | Tipo | Pente | Munição | Corante |
  |---|---|---:|---|---|
  | Beretta M9 | pistola | 15 | USP | marrom |
  | Uzi | submetralhadora (pente no cabo) | 32 | SMG | preto |
  | FAMAS | fuzil bullpup com alça e bipé | 25 | fuzil | azul |
  | M249 | metralhadora com caixa de munição | **100** | fuzil | verde |
  | Barrett M82 | sniper pesada, mira 4×/8×/12× | 10 | precisão | roxo |
  Agora são **20 armas**.
- **Miras de fuzil reais** também na SCAR, FAMAS e M249; a Barrett usa a mira larga da sniper.
- **Mod muito mais leve**, sem mudar o visual no jogo:
  - texturas das armas em 256 px com paleta de cores (ainda 4× mais detalhe que um item normal);
  - as 13 cores de cada peça de arma agora são "filhos" do modelo base (só trocam a textura) — quase 3000 modelos deixaram de repetir a geometria;
  - JSON sem espaços. Script: `tools/compact_resources.py` (rodar por último, depois dos geradores).

## Leva 20
- **COMPILAR_WINDOWS.bat instala sozinho:** ao terminar, copia o JAR novo para `%APPDATA%\.minecraft\versions\hidroeletrica\mods` e **move** os JARs antigos do Maré Elétrica para `hidroeletrica\mods_antigos_mare_eletrica` (não apaga nada).

## Leva 21 — correção da compilação
- Corrigidos os 3 erros apontados pelo compilador no PC: a caixa de desenho das máquinas grandes (a roda que sumia) foi para o `MachineRenderer` (lugar certo no NeoForge 1.21.1) e o filtro do núcleo agora compara o tipo direto (`target != Kind.AUTO_CRANK`).

## Leva 22
- **Drone sem travar:** o voo agora é calculado no seu próprio jogo (como os barcos do Minecraft) e o servidor só recebe a posição — a câmera não espera mais a internet. O som do motor toca bem menos vezes.
- **Painel de chips menor:** o painel sem fio da frente das máquinas ficou com metade do tamanho e desceu para o rodapé.
- **Granadas:** frag com explosão **maior e mais bonita** (bola de fogo, fumaça, anel de poeira, terra voando; ainda não quebra blocos). **Incendiária põe fogo no chão** em volta. **Fumaça** mais grossa e por 25 s. **Flash deixa a tela toda branca** e toca um **zumbido** que vai sumindo (mais forte se você estiver olhando).
- **Roda gigante:** a colisão agora é **redonda** (aro + eixo) em vez de um quadrado de 7×7. A **Manivela encaixa na própria roda**: clique direito na roda com a manivela; depois, clique direito na roda com a mão vazia para girar (a manivela aparece no eixo e gira junto). Quebrar a roda devolve a manivela.
- **Bloco de Controle novo:** clique direito abre um painel com limite de energia, ciclo liga/desliga e a **lista de aparelhos conectados** (até 8): ligar/desligar cada um, **quanto % da energia** cada um recebe (repartir energia) e remover.
- **Pen Drive (item novo):** Shift + clique direito num aparelho ou câmera grava ele; clique direito no Bloco de Controle com o pen drive conecta (até 64 blocos).
- **Câmera de Segurança (bloco novo):** vai em parede, chão ou teto. No painel do Bloco de Controle, botão **Ver** mostra a imagem da câmera (mouse olha em volta, setas trocam de câmera, X sai). Só aparece o que estiver dentro da sua distância de renderização.
- **Ainda não:** haste mais curta entre as rodas (precisa refazer a estrutura 7×7×7 da roda).
