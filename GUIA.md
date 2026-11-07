# Atualização 0.17.0

Mudanças atuais e instalação em NOVIDADES_0.17.0.md. A luneta não aumenta mais a distância de visão.

# Atualização 0.16.0

Instale `INSTALAR/mare-eletrica-0.16.0.jar`. Leia **NOVIDADES_0.16.0.md** antes das instruções históricas abaixo: ele descreve a sniper modular, deitar, revólver, calibração e os novos painéis dos chips. J abre a sniper; M2 mira; M1 dispara; Shift duas vezes deita/levanta; R ou M2 recarrega o revólver.

A aba Armaduras não copia mais itens externos nem contém DMT. Itens de guerra reúne morteiro, DMT, sniper e revólver com munições/acessórios. Testes atuais em VALIDACAO.md.

---

# Atualização atual: 0.15.0

Leia primeiro **NOVIDADES_0.15.0.md**. Seus controles, tamanhos, chances e receitas substituem as descrições antigas abaixo. M2 lança o gancho; U desativa o DMT; a roda nova tem 7×7×7 e gera energia suja.

# Atualização 0.13.0

Leia [NOVIDADES_0.13.0.md](NOVIDADES_0.13.0.md) para as mudanças atuais. O DMT agora deve ser equipado no slot próprio do inventário. A roda ocupa 3×3×3 e as hastes se conectam ao novo centro.

Atualização 0.12.0: consulte primeiro NOVIDADES_0.12.0.md para a roda com água parada, hastes, DMT pela mira e cama LED.

# Atualização 0.11.0

DMT com ganchos e gás, roda-d’água gigante e três novas turbinas: consulte **NOVIDADES_0.11.0.md** para controles, montagem e produção de energia.

# Atualização 0.10.0

Consulte **NOVIDADES_0.10.0.md** para torneira, central de recarga de gás 5×5×5, exterminadores, veículos compactos, pincel contínuo, setas de tamanho e controle de mira. As instruções da 0.10.0 substituem as descrições anteriores desses aparelhos.

# Atualização 0.9.0

As instruções atualizadas do projetor, carrinho, broca, gás e dutos estão em NOVIDADES_0.9.0.md. As seções antigas abaixo descrevem as versões anteriores dessas máquinas.

# Maré Elétrica — 0.8.1 alfa

Minecraft Java **1.21.1**, NeoForge **21.1.172** ou posterior da linha 21.1, Java 21. Projeto independente, sem necessidade de GeckoLib, Curios ou Iron’s Spells. Consulte `LEIA_PRIMEIRO.txt` para instalação e `VALIDACAO.md` para os testes.

**Atualização 0.8.0:** base 3×3×3, controle remoto com câmera roxa, modo barco, duas asas, tiros manuais e seis miras ajustáveis no morteiro. `ATUALIZACAO_0.8.0.md` traz todos os comandos e receitas.

O catálogo tem **73 itens obtíveis: 58 objetos colocáveis e 15 itens/componentes**. Todos os 64 itens da 0.6.0 permanecem. As novidades têm receitas e descrições em português e inglês. `NOVIDADES_0.7.0.png` mostra as formas reais dos modelos; `PREVIA_MODELOS.png` mostra o catálogo completo.

## Projetor de Construções: projeções azuis, sem materiais

O aparelho agora usa **blocos azuis translúcidos de energia**, incluindo formas de escada, laje, parede, cerca, vidro, plantas e portas. Não entrega construções de materiais permanentes nem pede materiais ao jogador. A projeção é sólida para caminhar; portas projetadas abrem com a mão vazia. Baús, camas e bancadas projetados são representações, sem inventário, sono, receitas, aldeões ou loot.

1. Conecte a um núcleo com energia limpa. Capacidade interna: **2.000.000 FE**.
2. Abra o painel e escolha um projeto pelas setas.
3. No campo **Blocos para frente**, digite de **2 a 128** e clique em **Aplicar**. Por exemplo: **20** faz a borda inicial aparecer a 20 blocos à frente, no nível da base e centralizada no visor. Aplicar outra distância encerra a projeção atual; clique em Projetar novamente.
4. Use **Prévia: 20 s** para marcar o contorno da área com partículas, sem custo. Deixe as posições dos blocos do projeto livres de água, plantas e outras construções. Espaços internos vazios de uma casa podem conter ar e entidades; blocos ocupados por entidades são adiados.
5. Clique em **Projetar**. A montagem ocorre em lotes de até 256 blocos por meio segundo. Não há espaços de materiais no painel.
6. Mantenha a alimentação: custo de **8 FE por bloco novo**, mais **2 FE/s por bloco do projeto**, com mínimo de 200 FE/s. O painel mostra o gasto de manutenção e as dimensões. Projeções extensas se beneficiam do Núcleo Abissal e cabo cyberpunk.

Sem energia, desligado, com pausa de redstone ou após quebrar/girar o projetor, os blocos vencem em aproximadamente **2–3 s nos chunks carregados**. Se apenas faltar energia, o pedido de projeção permanece ativo para reconstruir quando a alimentação voltar. O botão de parar cancela esse pedido. A máquina não substitui construções, não altera terreno e não carrega chunks à força. Uma parte obstruída depois do início fica pendente até ser desocupada.

| Projeto | Característica |
|---|---|
| Árvore de carvalho | Copa volumosa, tronco e 11 blocos de altura |
| Casa pequena das planícies | Modelo da vila do próprio Minecraft |
| Torre | 20 blocos de altura, andares e ameias |
| Jardim | Flores, árvores e iluminação projetada |
| Arco | Passagem larga e curva superior |
| Plataforma | Piso com cercas e entrada livre |
| Grande ponte | 49 blocos de extensão; rampas, torres, corrimãos e cabos |
| Floresta inteira | Área 41×41, **25 árvores**, vegetação e caminho central |
| Castelo | Área 31×31, muralhas, portão e quatro torres |
| Casa média das planícies | Modelo original da vila |
| Casa do deserto | Modelo original da vila |
| Casa nevada | Modelo original da vila |
| Casa da taiga | Modelo original da vila |
| Casa da savana | Modelo original da vila |

As seis casas carregam os arquivos de estrutura do Minecraft instalado. As posições e orientações vêm desses modelos; as superfícies são convertidas para formas azuis de energia. Blocos especiais e móveis têm formas simplificadas. Não são arquivos do Minecraft copiados para o pacote do mod. Árvores, castelo, torre e ponte são projetos próprios; não contêm materiais coletáveis.

Ao abrir um projetor antigo, os materiais restantes no inventário da 0.6.0 são devolvidos ao jogador. Construções permanentes que ele já tenha feito continuam no mundo.

## Perfuratriz ampliada — 6×6×8

A estrutura nova tem **6 blocos de largura × 6 de profundidade × 8 de altura** e reserva de **1.000.000 FE**. A ponta gira e desce aproximadamente 4,75 blocos até ficar completamente sob o chão; o restante da torre permanece parado. A animação não acompanha a profundidade real de mineração.

1. Deixe o volume 6×6×8 livre e coloque um único item sobre a área desejada. O ponto clicado é o canto inferior esquerdo da frente.
2. Conecte um núcleo e ligue no painel: ela começa desligada.
3. Varre a coluna **6×6 até 64 blocos abaixo da base**. Remove apenas minérios reais e guarda os drops em nove espaços, usando o equivalente a uma picareta de diamante sem encantamentos.
4. Cada minério custa **2.000 FE**. Falta de energia ou saída cheia pausa a extração. Use Transferidor e Dutos para retirar os produtos.
5. O botão **Reiniciar busca** varre novamente. Não gera minérios artificiais nem XP.

Minérios vanilla, variantes de ardósia, quartzo, ouro do Nether e detritos ancestrais são reconhecidos. Consultas e quebras respeitam o evento de proteção de área do NeoForge.

**Brocas já colocadas na 0.6.0 permanecem com tamanho 3×3×4 para preservar o mundo. Recolha e coloque novamente numa área livre para obter o modelo 6×6×8 e a animação.** Ao recolher, retire antes os produtos; energia e progresso não ficam guardados no item.

## Estufa mais útil — 3×3×3

A estufa mantém **64.000 FE** de reserva. O ciclo agora dura **5 segundos**, com mais cultivos e uma entrada opcional de adubo.

| Entrada | Uso |
|---|---|
| 1 | Semente/cultivo inicial, reutilizável |
| 2 | 1 Água Potável por ciclo |
| 3 | Opcional: 1 Farinha de Osso por ciclo, dobra a colheita |
| Penúltimo espaço | Saída de frascos vazios |
| Último espaço | Saída de produtos |

Cada ciclo custa **4.000 FE**, ou **8.000 FE com adubo**. Produz 8 trigos, beterrabas, cenouras, batatas, fatias de melancia, canas, bagas doces ou cacaus; 4 abóboras; ou 6 fungos do Nether, conforme o cultivo colocado. Com adubo, produz o dobro. Para trigo, beterraba, melancia e abóbora, use suas sementes.

Falta de energia e saída cheia pausam o trabalho. Trocar o tipo de uma entrada reinicia o progresso. O cultivo reutilizável permanece no primeiro espaço. Plantas do modelo são decorativas; a colheita aparece no inventário. Transferidores podem inserir os insumos e retirar as duas saídas.

## Nivelador de Terreno

Coloque no nível em que deseja limpar o terreno. Ele começa desligado. Conecte ao Núcleo Abissal usando cabo cyberpunk e ligue pelo painel.

- Área **21×21**, centrada na máquina: dez blocos para cada lado.
- Limpa **do Y da base até Y+20**, de cima para baixo. Nunca remove blocos abaixo do controlador.
- Consome **10.000 FE por bloco removido**, até 16 blocos por segundo, conforme energia e espaço. Reserva: **4.000.000 FE**.
- Os drops entram em nove espaços. Saída cheia interrompe o trabalho; use um Transferidor.
- Preserva a própria máquina, blocos com inventário ou entidade de bloco, blocos indestrutíveis, fluidos e blocos encharcados. Proteções de área podem impedir a quebra. Assim, essas exceções podem permanecer acima do piso.
- **Reiniciar área** repete a varredura. O aparelho não coloca aterro nem preenche buracos abaixo do piso.

## Cabo Cyberpunk e distribuição

O cabo tem corpo escuro com anéis violeta e linhas luminosas. Conecta automaticamente nas seis direções, também aos cabos comuns.

| Rede | Limite por trecho |
|---|---:|
| Cabo Isolado | 16.000 FE/s |
| Cabo Cyberpunk | **256.000 FE/s** |

O limite de cada cabo é compartilhado entre todos os emissores e entre os dois tipos de energia; misturar um cabo comum no trajeto limita aquele caminho. Núcleos distribuem até 16.000, 64.000 e 256.000 FE/s, respectivamente. Suas reservas continuam em 20.000, 100.000 e 500.000 FE. Núcleos não se recarregam mutuamente.

Para uma usina grande, faça trajetos curtos do gerador ao núcleo e do núcleo aos consumidores. No circuito sujo, deixe ramais separados de entrada e saída do purificador para não gastar a capacidade do mesmo cabo duas vezes.

## Dois geradores fortes — ambos 3×3×3

| Aparelho | Instalação | Produção | Reserva |
|---|---|---:|---:|
| Reator Geotérmico | Pelo menos **4 células de magma ou lava** imediatamente sob a base 3×3 | **80.000 FE limpos/s** | 2.000.000 FE |
| Dínamo Abissal | Controlador em bioma de oceano e **3 águas-fonte** alinhadas imediatamente à frente da base | **160.000 FE sujos/s** | 4.000.000 FE |

Coloque a estrutura fora de fluidos; depois adicione as fontes nas posições de operação. O reator não consome o magma/lava. O dínamo não retira água do mundo. Energia limpa vai para núcleos; suja vai para purificadores. Reservatório cheio limita a produção.

**Purificador industrial:** encostar um cabo cyberpunk ao Purificador Salino ativa o tratamento de até **160.000 FE sujos/s**, produzindo **128.000 FE limpos/s**. A proporção continua 5:4. Cada carvão trata 24.000 FE sujos; em potência máxima o consumo médio é de aproximadamente 6,67 carvões por segundo. Use uma alimentação automática de carvão. A reserva do purificador foi ampliada para 320.000 FE sujos e 256.000 limpos. Com cabos comuns, mantém a velocidade anterior de 1.200 FE sujos/s.

## Explorador com rodas, barco e planador

A Base do Explorador agora ocupa 3×3×3 e mantém sua reserva de 1.000.000 FE. O carrinho tem seis rodas animadas, bateria de 100.000 FE e capacidade para 256 Balas do Explorador. Explore automaticamente norte, sul, leste e oeste com retorno para recarga.

Vincule o controle usando Shift + usar na base. Use no ar para pilotar a até 1.024 blocos: câmera no módulo roxo e corpo parado. W/S move, A/D e mouse viram, ataque dispara e R encerra. O casco abre ao tocar a água, e duas asas abrem em quedas altas. Só há disparos sob controle manual. O remoto carrega uma região limitada para câmera e retorno; as expedições automáticas normais dependem dos chunks carregados.

Obstáculos podem interromper o retorno. Use uma Bateria Portátil diretamente no veículo sem carga. Bases antigas precisam ser recolocadas para ampliar. Veja `ATUALIZACAO_0.8.0.md` para todos os limites e comandos.

## Morteiro de seis canhões — 3×3×2

Forneça energia limpa e seis Balas de Canhão. No painel, ajuste cada tubo: elevação de 0° a 90°, direção de −90° (esquerda) a +90° (direita). Os valores são relativos à frente da máquina. Aplicar guarda os ângulos; Ativar também aplica antes de armar.

Ligue o aparelho e ative pelo painel ou pela subida de redstone. Gasta seis balas e 6.000 FE; dispara após dois segundos. Durante a contagem, a mira fica travada. Os tubos seguem os ângulos visualmente e os ajustes persistem ao salvar. Os projéteis explodem como na versão anterior. Exemplo: canhão 1 em 30°/+10°, canhão 2 em 60°/0°.

## Sede e mel, mantidos da 0.6.0

### Sede acima da fome

A barra azul tem dez gotas, equivalentes a 20 pontos de sede, acima da fome. A posição reserva espaço para a barra de ar ao mergulhar. O servidor calcula a sede de cada jogador e salva o valor com ele.

- Em repouso, perde um ponto a cada 90 segundos. Correr ou nadar gasta três vezes esse ritmo; o calor do Nether acrescenta mais uma unidade de esforço por segundo.
- Com quatro pontos ou menos, recebe Lentidão I. Sem água, sofre um ponto de dano a cada quatro segundos, sem cair abaixo de meio coração por essa regra.
- Criativo, espectador e dificuldade Pacífico mantêm a sede cheia. A morte restaura a sede; sair e voltar preserva o valor salvo.
- Segure o botão de usar para beber. Não é preciso estar com fome.

| Bebida | Sede recuperada, em pontos |
|---|---:|
| Frasco vanilla de água | 4 |
| Balde de leite | 6 |
| Frasco de Água Potável | 10 |
| Água com Mel | 12, além de 2 pontos de fome |

Água comum permite sobreviver antes de construir o potabilizador. A água potável e a água com mel devolvem seus frascos de vidro.

### Usos simples para o mel

As receitas são sem formato: coloque os dois ingredientes juntos na bancada. O frasco do mel é devolvido pelas regras da receita vanilla.

| Item | Receita | Efeito |
|---|---|---|
| Pão com Mel | 1 pão + 1 frasco de mel | 6 pontos de fome |
| 4 Balas de Mel | 1 açúcar + 1 frasco de mel | 2 pontos de fome por bala; consumo rápido |
| Água com Mel | 1 Água Potável + 1 frasco de mel | 12 de sede e 2 de fome; devolve o frasco ao beber |

Todos têm modelo 3D no inventário e na mão. O frasco da água potável se torna o recipiente da Água com Mel durante a receita.

## Potabilizador de Água — introduzido na 0.5.0

O potabilizador e seu frasco, introduzidos na 0.5.0, continuam disponíveis. Agora a água potável também recupera sede e abastece a estufa.

### Montagem e uso

1. Deixe uma área de **2×2 blocos de base e 3 blocos de altura totalmente livre**, sem plantas, água ou entidades sobre as peças. A estrutura se monta ao colocar um único item. A frente fica voltada para você; o ponto clicado é o canto inferior esquerdo da frente, junto à entrada azul.
2. Coloque um **bloco de água-fonte imediatamente à frente da entrada azul**, no mesmo nível da base. Correnteza e água dentro de um bloco encharcado não são aceitas. Você também pode instalar a máquina na margem de um rio ou mar, mantendo toda a estrutura fora da água.
3. Ligue um núcleo com energia limpa por **Cabos Isolados**. Os cabos podem encostar em qualquer uma das 12 peças. A Bateria Portátil também funciona em qualquer peça.
4. Clique com a mão vazia em qualquer peça para abrir o painel. Coloque **frascos de vidro vazios na entrada 1** e **carvão ou carvão vegetal na entrada 2**.
5. Ligue pelo botão do painel. A saída à direita recebe um Frasco de Água Potável a cada **4 segundos de trabalho**. Um carvão filtra **8 frascos**. O indicador laranja mostra os usos restantes do filtro instalado.
6. Dutos e Transferidores podem abastecer as entradas e retirar os frascos prontos por qualquer peça. Todas compartilham o mesmo inventário e reservatório. A pilha da saída comporta 16 frascos.

| Característica | Valor |
|---|---|
| Dimensões | 2 blocos de largura × 2 de profundidade × 3 de altura |
| Energia máxima | 32.000 FE limpos |
| Água doce | 800 FE por frasco; 200 FE/s durante o trabalho |
| Água marinha | 1.600 FE por frasco; 400 FE/s durante o trabalho |
| Filtro | 1 carvão comum ou vegetal para 8 frascos |
| Produto | 1 frasco vazio → 1 Frasco de Água Potável |
| Fonte no mundo | Permanece no lugar, como ao encher um frasco vanilla |

A água é considerada marinha quando **o bloco de entrada fica em um bioma de oceano**. Rios e outros biomas usam o custo de água doce. Não existe propagação de salinidade entre blocos; um balde colocado fora do oceano segue o bioma do novo local.

Sem água-fonte, frascos, filtro, espaço de saída ou energia suficiente para o próximo passo, o aparelho não avança nem cobra esse passo. O filtro novo só é consumido quando o trabalho pode começar. Falta de energia e saída cheia pausam o progresso; remover o frasco, perder a fonte ou mudar entre água doce e marinha reinicia o progresso. Energia já gasta não é devolvida. O botão do painel desliga; redstone aplicada à **peça do painel na base** pausa.

É possível beber o produto segurando o botão de usar; recupera **10 pontos de sede** e devolve o frasco vazio. A bebida pode ser usada mesmo com a fome cheia.

### Receita

Use esta disposição na bancada. Os nomes em cada linha vão da esquerda para a direita.

| Linha | Esquerda | Centro | Direita |
|---|---|---|---|
| Superior | Vidro | Caldeirão | Vidro |
| Central | Bobina de Cobre | Pistão | Bobina de Cobre |
| Inferior | Placa de Ferro | Módulo de Controle | Placa de Ferro |

As bobinas e placas vêm da Prensa; o módulo vem da Montadora. A receita é desbloqueada pelo sistema de receitas incluído no projeto.

### Estrutura, rotação e modelo

Quebrar qualquer peça desmonta o conjunto. No sobrevivência, a estrutura rende **um único item de potabilizador** e derruba o inventário; as peças de suporte não rendem itens independentes. A energia, o progresso e os usos restantes do filtro são perdidos ao recolher o aparelho, assim como nas outras máquinas.

Para girar, recolha e coloque novamente olhando para outra direção. Shift + clique informa essa regra; a estrutura não gira montada. Pistões não a movem. Se uma das peças estiver em um chunk indisponível, o processamento espera o carregamento normal. Peças sem controlador são removidas depois que o local dele puder ser conferido.

O modelo tem **dois tanques, cartucho filtrante, tubos, armação e painel**. A geometria foi repartida em 12 modelos locais: cada peça ocupa apenas seu próprio bloco e todas as coordenadas de textura ficam entre 0 e 16. Superfícies de cores diferentes sobrepostas no mesmo plano foram eliminadas. A colisão acompanha as peças em todas as orientações. Essas verificações reduzem as causas de texturas aleatórias e cintilação; a renderização dentro do jogo ainda precisa de teste.

`POTABILIZADOR_3D.png` mostra frente e traseira a partir das geometrias JSON reais, com cores planas. Não é uma captura do Minecraft.

## Recursos mantidos da versão 0.4.0

A versão 0.4.0 acrescentou oito aparelhos com modelo 3D, receita, tradução, painel e conexão com a rede correspondente. Eles permanecem na 0.7.0 junto dos demais itens anteriores.

| Aparelho novo | Como usar | Função e consumo |
|---|---|---|
| Painel Solar | Coloque sob céu aberto, fora da água. Conecte por cabos a um núcleo. | Gera 300 FE limpos/s durante o dia e sem chuva; reservatório de 12.000 FE. Energia solar já é limpa |
| Compactador de Materiais | Coloque pelo menos 9 unidades de um material aceito no espaço da esquerda. | 9 unidades → 1 bloco de armazenamento; 200 FE em 2 s. Saída à direita |
| Enchedor de Baldes | Baldes vazios à esquerda; um bloco de água-fonte diretamente à frente da máquina. | Consome um balde vazio, uma fonte de água e 400 FE; entrega um balde de água em 2 s |
| Purificador de Efeitos | Ligue e fique a até 5 blocos do centro. | Remove veneno, wither, lentidão, fraqueza, cegueira e fome; 300 FE por efeito removido |
| Refeitório Automático | Abasteça os 9 espaços com alimentos aceitos e fique a até 4 blocos. | Alimenta automaticamente jogadores com fome; 1 alimento + 100 FE por refeição, até uma refeição a cada 2 s |
| Sensor de Presença | Ligue e coloque um comparador apontando para fora do sensor. | Detecta jogadores a até 6 blocos: saída do comparador 15 quando há alguém, 0 quando não há. Consome 20 FE/s enquanto ligado |
| Extrator Apícola | Gire a frente para tocar uma colmeia ou ninho vanilla com nível de mel 5. | Recolhe 3 favos por 200 FE e esvazia o mel. Guarda os favos em 9 espaços, sem irritar nem expulsar as abelhas |
| Acelerador de Mineração | Ligue e fique a até 8 blocos. Use sua ferramenta normalmente. | Renova Pressa I por 120 FE/jogador, aproximadamente a cada segundo. O efeito dura até 3 s ao sair |

O Painel Solar é um gerador; os outros sete recebem energia limpa de núcleos, cabos ou baterias. Todos podem ser desligados no painel e pausados por redstone. Clique direito com a mão vazia abre o painel; Shift + clique direito vazio gira o aparelho.

### Projetor de Ponte ampliado

O reservatório do próprio Projetor agora guarda **100.000 FE**. O alcance cresce em etapas conforme a energia armazenada, mantendo **3 blocos de largura**. A projeção começa com pelo menos 80 FE. O painel mostra o comprimento que está sendo projetado, o alcance permitido pela carga e o consumo atual.

| Energia armazenada antes do ciclo | Alcance máximo | Consumo com o alcance completo |
|---|---:|---:|
| 0–79 FE | Sem projeção | 0 FE/s |
| 80–9.999 FE | 6 blocos | 160 FE/s |
| 10.000–19.999 FE | 12 blocos | 280 FE/s |
| 20.000–29.999 FE | 18 blocos | 400 FE/s |
| 30.000–39.999 FE | 24 blocos | 520 FE/s |
| 40.000–49.999 FE | 30 blocos | 640 FE/s |
| 50.000–59.999 FE | 36 blocos | 760 FE/s |
| 60.000–69.999 FE | 42 blocos | 880 FE/s |
| 70.000–79.999 FE | 48 blocos | 1.000 FE/s |
| 80.000–89.999 FE | 54 blocos | 1.120 FE/s |
| 90.000–100.000 FE | 60 blocos | 1.240 FE/s |

O consumo ocorre a cada meio segundo: 20 FE mais 10 FE por bloco de **comprimento efetivamente projetado**. Um obstáculo que encurta a ponte também reduz o gasto; caminho totalmente bloqueado não gasta energia. A largura não altera essa conta. Dois projetores sobrepostos pagam pela própria projeção.

Mantenha a alimentação contínua: se a carga cair de faixa, a ponte encurta. Os trechos sem renovação somem em aproximadamente 2–3 s nos chunks carregados, inclusive ao desligar, girar ou quebrar o aparelho. O chão da ponte é sólido e fino; ela não atravessa paredes, não substitui blocos ou água e não carrega chunks distantes. O alcance exibido é um limite; terreno e chunks disponíveis podem reduzi-lo. O alcance atual e o da carga podem diferir brevemente ao atravessar um limiar de energia.

Projetores salvos na 0.3.0 conservam a carga existente ao serem lidos pelo código novo, agora com mais espaço disponível; a carga não é preenchida gratuitamente. Os IDs anteriores foram preservados. Migração dentro de um mundo Minecraft ainda requer teste.

### Materiais e regras dos oito aparelhos

- **Compactador:** aceita lingotes de ferro, cobre, ouro e netherita; carvão; diamante; esmeralda; lápis-lazúli; redstone; e ferro/cobre/ouro brutos. Converte somente grupos de 9 iguais. Carvão vegetal e granulados não são compactados; os granulados continuam servindo à fundição e à Prensa de Componentes.
- **Enchedor:** retira apenas água-fonte em um bloco de água comum. Não retira água de blocos encharcados, correnteza, lava ou da própria máquina. Uma fonte infinita feita pelas regras do Minecraft pode se recompor após a coleta. Se a saída estiver ocupada, faltarem baldes ou faltar energia, não remove água. Transferidores podem inserir baldes vazios e retirar os cheios.
- **Purificador de Efeitos:** remove até um dos seis efeitos listados por jogador a cada meio segundo, cobrando apenas quando a remoção acontece. Mantém efeitos positivos. Não cura corações nem maldições de equipamentos; essas funções pertencem à Estação Revitalizadora e ao Desencantador.
- **Refeitório:** aceita pão, maçã, cenoura, batata assada, cenoura dourada e carnes cozidas de vaca, porco, galinha, ovelha, coelho, bacalhau e salmão. Usa a nutrição e a saturação do alimento no jogo. Não atende jogadores sem fome, criativos ou espectadores. Cada refeição usa uma unidade; use dutos e um Transferidor para repor comida.
- **Sensor:** o sinal é lido por um **comparador** encostado ao sensor; não sai diretamente para pó de redstone. Detecta jogadores vivos, incluindo criativos, e ignora espectadores. A detecção atravessa paredes. Responde a cada meio segundo e desliga a saída se faltar energia ou for pausado. Evite devolver seu circuito de saída para a entrada de pausa por redstone.
- **Extrator:** a frente tem o bocal amarelo. Funciona com colmeias e ninhos vanilla, mantendo o bloco e suas abelhas. Produz favos; não engarrafa mel. Quando não há espaço para os três favos ou energia suficiente, deixa a colmeia cheia. Um Transferidor pode levar os favos a um baú.
- **Acelerador:** concede o efeito Pressa I do Minecraft; você ainda precisa usar a ferramenta adequada para quebrar o bloco. Não minera automaticamente. Preserva um efeito de Pressa mais forte ou com duração maior, sem cobrar uma renovação desnecessária.
- **Painel Solar:** precisa de luz diurna, céu aberto e tempo sem chuva em uma dimensão com céu. Para de gerar à noite, sob cobertura ou submerso. O reservatório já carregado ainda pode enviar energia ao núcleo nessas condições, se o painel continuar ligado.

As áreas dos aparelhos novos que atendem jogadores usam distância real a partir do centro do bloco. Conexões, controle e regras anteriores continuam descritos abaixo.

## Recursos mantidos da versão 0.3.0

A versão 0.3.0 acrescentou **10 aparelhos**, **1 Transferidor de Itens**, Dutos de Itens e uma cadeia de componentes feita com os granulados. Todos permanecem na versão 0.7.0 com modelos 3D próprios.

| Novo aparelho | Como usar | Custo e resultado |
|---|---|---|
| Prensa de Componentes | Coloque granulados de ferro, cobre ou ouro na entrada. | 2 granulados → 1 placa, bobina ou contato; 400 FE em 2 s |
| Serraria Elétrica | Insira troncos ou blocos de madeira, com ou sem casca. | 1 bloco → 6 tábuas do mesmo tipo; 200 FE em 2 s |
| Congelador Criogênico | Use bola de neve com água encostada para fabricar gelo; ou insira gelo para compactar. | 1 bola de neve → 1 gelo; 9 gelos → 1 compacto; 9 compactos → 1 azul. 300 FE em 3 s |
| Reparador Elétrico | Entrada 1: ferramenta, arma ou armadura danificada. Entrada 2: placa de ferro. | Restaura até 100 pontos de durabilidade; consome 1 placa e 2.000 FE em 4 s |
| Desencantador Elétrico | Insira um equipamento ou livro encantado. | Remove todos os encantamentos, inclusive maldições, por 2.000 FE em 4 s. Não devolve XP |
| Alimentador de Animais | Coloque alimentos adequados nos 9 espaços e mantenha animais adultos por perto. | Consome 1 alimento e 150 FE por animal colocado em modo de reprodução; até 1 animal/s |
| Tosquiador Automático | Ligue perto de ovelhas adultas com lã. | Recolhe 1–3 lãs da cor da ovelha; 150 FE por tosquia, intervalo de 2 s |
| Extintor Automático | Coloque água encostada e ligue perto da área a proteger. | Apaga blocos de fogo por 20 FE e extingue criaturas em chamas por 50 FE |
| Projetor de Ponte | Aponte a frente para um espaço vazio. | Agora guarda 100.000 FE e projeta até 60×3 blocos conforme a carga; consulte a tabela da 0.4.0 acima |
| Montadora de Componentes | Coloque placa de ferro, bobina de cobre e contato de ouro nas entradas 1, 2 e 3. | 1 de cada → 1 módulo de controle; 1.200 FE em 4 s |
| Transferidor de Itens | Trás laranja encostado na origem; frente ciano apontada para o destino ou para dutos. | Até 8 itens a cada meio segundo; 20 FE por item entregue |

Todos os aparelhos usam energia limpa. Clique direito com a mão vazia abre o painel; Shift + clique direito vazio gira o aparelho. Um sinal de redstone pausa a operação.

### Nova função dos granulados

| Granulado | Processo na prensa | Para que serve a peça |
|---|---|---|
| 2 de ferro | 1 Placa de Ferro | Estrutura das novas máquinas, montadora e reparador |
| 2 de cobre | 1 Bobina de Cobre | Componente elétrico para receitas e módulos |
| 2 de ouro | 1 Contato de Ouro | Componente de precisão para receitas e módulos |

O triturador continua entregando 2 granulados por metal bruto. Você agora escolhe entre **fundir os granulados em lingotes** ou **prensá-los em componentes**. Nenhuma receita antiga de fundição foi retirada.

A Montadora usa uma placa, uma bobina e um contato para fazer o **Módulo de Controle**, ingrediente dos aparelhos avançados desta atualização. A Prensa usa ingredientes vanilla; a Montadora usa as peças da Prensa e dispensa módulos. Assim é possível começar essa cadeia no sobrevivência sem depender de uma máquina ainda inacessível. As descrições dos granulados e das peças dentro do inventário explicam seus usos.

### Montar uma linha de transferência

1. Coloque a máquina de origem, por exemplo, um Triturador com metal bruto.
2. Encoste um Transferidor nela. Gire até a **traseira laranja** tocar o Triturador. A frente ciano indica para onde os itens saem.
3. Na frente do Transferidor, coloque diretamente a próxima máquina ou comece uma linha de **Dutos de Itens** até ela.
4. Para fabricar componentes, termine a linha em uma Prensa. Ela aceita os granulados do Triturador.
5. Para mandar as peças prontas da Prensa para um baú ou para a Montadora, coloque **outro Transferidor** com a traseira na Prensa e a frente ligada ao próximo destino.
6. Alimente os Transferidores e as máquinas usando núcleos e **Cabos Isolados**. Os dutos transportam itens; não conduzem eletricidade.

A origem precisa encostar diretamente atrás do Transferidor. O destino pode ficar diretamente à frente ou no final dos dutos. Os dutos conectam para os seis lados, incluindo curvas e subidas. Cada busca percorre no máximo 128 dutos e até 32 blocos por eixo, sem carregar chunks.

Em uma ramificação, o Transferidor escolhe o primeiro inventário compatível encontrado pelo menor caminho nos dutos. Ele respeita os espaços de entrada/saída de cada máquina: não retira o minério bruto da entrada de um Triturador, por exemplo. Para alimentar diferentes entradas da Montadora, ligue linhas de placas, bobinas e contatos aos seus lados; os ingredientes entram nos espaços correspondentes.

Se o destino estiver cheio ou não aceitar o item, não há consumo por uma entrega que não aconteceu. Itens já retirados ficam no inventário interno do Transferidor até haver espaço. Esse inventário é salvo junto com o mundo e derruba seus itens quando a máquina é quebrada. Você também pode retirar os itens pelo painel. Os aparelhos são terminais: o duto não atravessa o inventário de uma máquina para alcançar a seguinte. Use outro Transferidor depois dela.

Baús e outros inventários que exponham a interface de itens do NeoForge também podem ser usados. Seus limites de acesso por lado continuam valendo; a compatibilidade com cada mod externo ainda precisa de teste.

### Detalhes dos novos aparelhos

- A Serraria reconhece carvalho, pinheiro, bétula, selva, acácia, carvalho escuro, mangue, cerejeira, carmesim e distorcido, incluindo versões descascadas e blocos de madeira/hifas. Não corta árvores no mundo.
- O Congelador exige água apenas na receita de bola de neve; compactar gelo não precisa dela. A água não é consumida.
- O Reparador preserva nome e encantamentos do equipamento. Para recuperar mais de 100 pontos, coloque a saída de volta na entrada com outra placa. Equipamentos sem desgaste não são aceitos.
- O Desencantador elimina também o custo acumulado de trabalhos anteriores na bigorna. Livros encantados viram livros comuns; ele não extrai encantamentos para outro livro.
- O Alimentador aceita trigo, cenoura, batata, beterraba e sementes de trigo, beterraba, abóbora e melancia. Só usa o alimento que o animal reconhece. A área é de 5 blocos por eixo; são necessários dois animais compatíveis, adultos e sem o intervalo de reprodução ativo para nascer um filhote pelas regras normais do jogo.
- O Tosquiador também atua a até 5 blocos por eixo. Quando seu inventário não comporta toda a lã sorteada, ele deixa a ovelha intacta. Ela recupera lã pelas regras normais do Minecraft.
- O Extintor verifica uma área horizontal 9×9, de dois blocos abaixo a dois acima, em pequenos lotes. Para criaturas, a área é de 4 blocos por eixo. Ele apaga fogo, não remove lava nem elimina a fonte de novos incêndios; a detecção não é instantânea.
- A ponte começa um bloco à frente do Projetor, na altura de sua base, com piso fino e sólido. Espaços ocupados e água interrompem ou limitam a projeção. Ela não substitui construções. Ao desligar, girar, quebrar ou deixar o Projetor sem energia, os trechos antigos somem em cerca de 2–3 segundos nos chunks carregados. Não dependa de uma ponte sem alimentação para permanecer sobre um vão.
- A automação pode completar pilhas de ingredientes sem reiniciar o progresso. Trocar o tipo ou os componentes de uma entrada reinicia o processamento.

## Recursos mantidos da versão 0.2.0

| Novo aparelho | Como usar | Custo |
|---|---|---|
| Forno Culinário Elétrico | Ligue ao núcleo e coloque um alimento que tenha receita de defumador na entrada. Cozinha em 1 segundo. | 200 FE por alimento |
| Reciclador Elétrico | Coloque carne podre, alga kelp ou sementes de trigo, abóbora ou melancia para obter farinha de osso. Também converte pedregulho em cascalho e cascalho em sílex. Uma unidade de entrada vira uma unidade de saída, em 4 segundos. | 400 FE por operação |
| Gerador de Pedra | Coloque água encostada ou no espaço da máquina, conecte energia e retire o pedregulho no slot da direita. Gera uma unidade a cada 2 segundos, sem remover a água. | 300 FE por pedregulho |
| Estação de Respiração Aquática | Ligue ao núcleo. Jogadores dentro da água e próximos à estação recebem respiração aquática enquanto houver energia. Área de 6 blocos por eixo; efeito dura até 3 segundos após sair. | 60 FE/s por jogador atendido |

### Mais oito aparelhos e a torreta automática

| Aparelho | Como usar | Energia / limite |
|---|---|---|
| Colhedora Elétrica | Coloque ao lado da plantação. Recolhe somente cultivos maduros na área 9×9, até um bloco acima ou abaixo da máquina. Guarda os produtos em 9 espaços. | 200 FE por planta |
| Plantadora Elétrica | Coloque sementes de trigo/beterraba, cenouras ou batatas nos 9 espaços. Prepara cultivos em terra já arada, na área 9×9 e mesma faixa de altura da colhedora. | 50 FE por plantio |
| Pescador Automático | Coloque na margem, apontado para o lago. O centro da área de pesca fica 2 blocos à frente e 1 abaixo. Pelo menos 6 das 9 posições da área 3×3 precisam conter água. Produz bacalhau e salmão alternadamente. | 400 FE por peixe, a cada 20 s |
| Radar de Minérios | Abra o painel e clique em **Escanear**. Examina uma área 17×17, de 1 a 32 blocos abaixo, e mostra o primeiro minério encontrado e suas coordenadas X/Y/Z. | 4.000 FE por busca; até cerca de 36,5 s |
| Plataforma de Transporte | Alimente duas plataformas, deixe o mesmo canal no painel e agache sobre a origem. Destino livre, na mesma dimensão, a até 128 blocos. | 1.000 FE na origem + 500 FE no destino |
| Estação de Carregamento | Insira uma Bateria Portátil ou outro item que aceite a interface FE do NeoForge. Retire o item após carregar. | Até 800 FE/s |
| Coletor de Experiência | Recolhe orbes de XP próximos e guarda os pontos. Use **Retirar até 100 XP** no painel para receber os pontos, sem gasto adicional. | 2 FE por ponto; banco de 10.000 XP; área de 4 blocos por eixo |
| Controlador de Clima | No Overworld, clique em **Alternar sol/chuva**. A mudança afeta a dimensão inteira por 5 minutos, salvo outra mudança de clima. | 20.000 FE por uso; intervalo de 60 s |
| Torreta Elétrica Automática | Ligue ao núcleo e deixe espaço livre acima dela. Detecta monstros visíveis em todas as direções e dispara um feixe elétrico. O modelo se orienta para o alvo. | Raio de 12 blocos; 400 FE por disparo; até 1 disparo/s; 4 pontos de dano base |

A colhedora libera os espaços para a plantadora; o irrigador anterior cuida da água e do crescimento. Se a colhedora não tiver espaço para todos os produtos de uma planta, ela deixa o cultivo intacto. A plantadora só usa terra arada e espaços livres. Funis retiram a colheita e os peixes e abastecem a plantadora. A pescaria pausa sem água, espaço ou energia; não remove peixes vivos nem produz tesouros.

O radar localiza os minérios vanilla, incluindo ardósia, quartzo, ouro do Nether e detritos ancestrais. Ele não minera nem destaca todos os veios: exibe um resultado por busca, começando de cima para baixo. Não examina chunks descarregados. Uma leitura é um registro da busca; refaça a busca depois de extrair o minério.

As plataformas têm **16 canais**. Clique no botão do canal para alternar. Se houver vários destinos no mesmo canal, será escolhido o destino disponível mais próximo. Ambos os chunks precisam estar carregados, as duas plataformas ligadas e com energia; deixe dois blocos livres acima do destino. Há uma pausa de 3 segundos entre transportes do mesmo jogador. Elas não transportam veículos nem atravessam dimensões. Cabos do mod têm seu próprio limite de alcance; alimente plataformas distantes por circuitos locais ou baterias.

A **Bateria Portátil** é um componente 3D com capacidade de **16.000 FE**. Carregue-a na estação e use-a com clique direito sobre um núcleo ou aparelho para transferir até 1.000 FE por clique. A carga aparece na barra do item e na descrição. Ferramentas vanilla não aceitam carga; a estação também aceita itens FE de outros mods, cuja compatibilidade específica ainda precisa de teste.

O banco de XP guarda **pontos**, não níveis. Orbes agrupados só são recolhidos se houver energia e espaço para o grupo inteiro. Ao quebrar o coletor, sua reserva volta ao mundo em orbes. Retirar XP e trocar o canal das plataformas funciona mesmo com a máquina pausada.

A torreta usa somente eletricidade, sem munição. Seleciona entidades classificadas como monstros pelo jogo; jogadores, aldeões e animais não são alvos. Paredes com colisão, inclusive vidro, interrompem o feixe. Cada disparo causa 4 pontos de dano base (2 corações), sujeito às regras de dano do alvo. Ela não destrói blocos. Pode ser desligada pelo painel ou por redstone, como os outros aparelhos.

Todos têm modelos 3D próprios, receitas, painel e reservatório interno. Os processadores aceitam retirada de produtos por funis; o gerador de pedra não precisa de itens de entrada.

### Holofote com área ampliada

O holofote mantém luz de nível 15 e agora projeta **14 pontos temporários de iluminação**, distribuídos de 4 a 16 blocos à frente, com largura crescente de até 8 blocos para cada lado. A luz se espalha a partir desses pontos pelo sistema de iluminação do Minecraft. O consumo passou de 50 para **150 FE/s**.

Use **Shift + clique direito com a mão vazia** para apontar a frente do holofote para o lugar desejado. Deixe espaço livre acima da cabeça do aparelho: a projeção parte um bloco acima da base. Paredes e outros blocos com colisão interrompem a projeção. Vidro também bloqueia esta implementação.

Os pontos de luz não têm colisão, não são itens e podem ser substituídos por construções ou água. Só são colocados em espaços de ar: não removem água, plantas ou blocos existentes. Ao desligar, girar, quebrar ou deixar o holofote sem energia, os pontos antigos somem em aproximadamente **2 a 3 segundos**, em chunks carregados. Dois holofotes podem compartilhar pontos sem apagar um ao outro.

### Atualizar a instalação

1. Extraia a pasta desta versão e execute `COMPILAR_WINDOWS.bat`.
2. Após uma compilação bem-sucedida, feche o Minecraft.
3. Retire o JAR anterior (incluindo `mare-eletrica-0.4.0.jar`) da pasta `mods` e coloque `mare-eletrica-0.8.0.jar`.
4. Não mantenha as duas versões juntas. Os IDs dos itens antigos foram preservados; a migração de mundos ainda precisa de teste. Faça uma cópia do mundo antes de atualizar.

## Gerar e instalar

1. Extraia o ZIP inteiro.
2. Entre na pasta `MareEletrica` e abra `COMPILAR_WINDOWS.bat`.
3. O script usa um JDK 21 existente, quando o encontra, ou baixa uma cópia portátil do Eclipse Temurin. Também baixa Gradle 8.10.2. Ambos ficam na pasta `.tools` do projeto; não precisam de instalação como administrador. Os arquivos baixados têm seus SHA-256 conferidos.
4. Aguarde a mensagem `COMPILACAO CONCLUIDA`. Na primeira execução, os downloads e a preparação do Minecraft podem ser demorados.
5. O arquivo destinado ao jogo será `build/libs/mare-eletrica-0.8.0.jar`.
6. Coloque esse JAR na pasta `mods` da instalação/perfil **Minecraft 1.21.1 + NeoForge**. A pasta do perfil pode ser diferente da pasta padrão `.minecraft`; use a opção do seu launcher para abri-la.
7. Teste primeiro em um mundo novo. Em servidor, instale o mesmo arquivo dos dois lados.

O arquivo `mare-eletrica-0.8.0-sources.jar`, quando gerado, é o código-fonte, não o arquivo para jogar. Não coloque o ZIP do projeto na pasta `mods`.

O script não foi executado no Windows durante a criação. Se falhar, a janela permanece aberta e o arquivo `COMPILACAO.log` registra a tentativa. Envie esse arquivo para diagnosticar o erro.

Para desenvolvimento, `TESTAR_CLIENTE_WINDOWS.bat` solicita ao Gradle a abertura de um cliente de teste. No Linux, com JDK 21, Python 3.11+, curl e unzip instalados, use `bash compilar_linux.sh`. Não é necessário instalar Python para a compilação pelo Windows.

## A primeira usina

### Água corrente

1. Faça um canal com água corrente e um piso sob a roda. Deixe água tocando pelo menos **duas posições** entre o espaço da roda e seus seis vizinhos diretos.
2. A água precisa fluir em pelo menos uma dessas posições. Um tanque de água totalmente parada não movimenta a roda.
3. Coloque uma **Roda de Corrente**. Ela gera até 800 FE/s e guarda até 8.000 FE de cada tipo em reservatórios separados.
4. Ligue a roda por **Cabos Isolados** a um **Núcleo Compacto**.
5. Ligue uma **Fornalha Elétrica** ao núcleo por cabos. Abra os painéis com clique direito e acompanhe a energia.
6. Coloque um ingrediente de receita de fornalha no espaço da esquerda. O produto aparece à direita.

Fora do oceano, a roda gera energia limpa. Se a roda estiver em um bioma marcado como oceano, a energia gerada será suja e deverá passar pelo purificador.

### Energia do mar

1. Coloque a **Turbina de Maré** em um bioma de oceano, com água tocando pelo menos **três posições** entre o espaço da turbina e seus seis vizinhos diretos.
2. Essa turbina funciona também em água parada e gera até **2.400 FE sujos/s**.
3. Ligue-a ao **Purificador Salino** e coloque carvão ou carvão vegetal no espaço do filtro.
4. Ligue a saída à rede de um núcleo; as máquinas recebem energia limpa desse núcleo.
5. Com cabos comuns, cada purificador trata até **1.200 FE sujos/s**, entregando **960 FE limpos/s**; use dois para acompanhar uma turbina. Um **cabo cyberpunk encostado ao purificador** ativa o modo industrial: até **160.000 FE sujos/s → 128.000 FE limpos/s**.
6. Cada unidade de carvão filtra 24.000 FE sujos, resultando em até 19.200 FE limpos. A perda de 20% faz parte da mecânica do mod.

A rede pode compartilhar os mesmos cabos entre geradores, filtros, núcleo e consumidores. O código mantém os dois tipos de energia separados. As máquinas são terminais: para passar uma linha por trás de uma máquina, use cabos ao redor dela.

A energia marinha suja é uma regra fictícia de jogabilidade, não uma característica física da eletricidade real.

## Equipamentos implementados no código

Valores por segundo pressupõem o ritmo normal de 20 ticks/s do Minecraft.

| Equipamento | Função | Energia / limite |
|---|---|---|
| Roda de Corrente | Usa água corrente; no oceano gera energia suja | Até 800 FE/s |
| Turbina de Maré | Gera em oceanos; exige contato com água | Até 2.400 FE sujos/s; reserva de 24.000 |
| Purificador Salino | Consome carvão para filtrar energia | 1.200 → 960 FE/s; com cabo cyberpunk: 160.000 → 128.000 FE/s |
| Núcleo Compacto | Armazena e distribui | 20.000 FE; até 16.000 FE/s |
| Núcleo de Distribuição | Armazena e distribui | 100.000 FE; até 64.000 FE/s |
| Núcleo Abissal | Armazena e distribui | 500.000 FE; até 256.000 FE/s |
| Fornalha Elétrica | Usa receitas de fundição; não precisa de combustível | 2 s e 800 FE por item |
| Triturador de Minérios | 1 ferro/cobre/ouro bruto → 2 granulados | 4 s e 1.600 FE |
| Irrigador Giratório | Hidrata terra arada em área 9×9 e acelera plantações | 5 FE por terra hidratada; 120 FE por crescimento |
| Coletor Magnético | Recolhe itens soltos numa área de 6 blocos por eixo | 5 FE por item; 9 espaços |
| Estação Revitalizadora | Recupera meio coração por jogador a cada 2 s | 400 FE por recuperação; área de 5 blocos por eixo |
| Elevador de Vórtice | Coluna de elevação com queda lenta ao sair | 80 FE/s por jogador; coluna de cerca de 9 blocos |
| Holofote Elétrico | Emite luz de nível 15 e projeta pontos até 16 blocos à frente | 150 FE/s |
| Repulsor de Monstros | Empurra criaturas hostis; não causa dano direto | 80 FE por impulso; raio de 6 blocos |
| Cabo Isolado | Conecta por cima, por baixo e pelos quatro lados | Curvas e junções automáticas |

Os granulados podem ser fundidos para virar lingotes. Há receitas para fornalha normal e alto-forno, além do uso na fornalha elétrica.

O irrigador exige água em seu espaço ou em um vizinho direto. Coloque-o ao lado dos cultivos ou um bloco acima. Ele verifica terra arada até dois blocos abaixo e pode aplicar crescimento a uma plantação a cada dois segundos. Não colhe nem replanta.

O coletor espera passar o pequeno atraso de coleta do item recém-solto. Se não tiver espaço ou energia, deixa o item no chão. Funis podem retirar seus itens.

No elevador, fique sobre a plataforma para subir e agache para deixar de renovar a elevação. A elevação já aplicada leva até um segundo para terminar. A queda lenta dura até cinco segundos após sair.

O repulsor atua em entidades identificadas pelo jogo como hostis. Mobs que mudam seu comportamento conforme a situação podem ser classificados de outra forma.

## Controles e automação

- **Clique direito com a mão vazia:** abre o painel. Mostra energia, estado e, quando aplicável, inventário e progresso.
- **Shift + clique direito com a mão vazia:** gira o modelo 90 graus.
- **Botão Ligado/Desligado:** habilita ou pausa a máquina.
- **Sinal de redstone:** pausa a operação, inclusive geração e distribuição pelo núcleo. A exceção é o morteiro: a subida do sinal o ativa.
- **Funis:** inserem ingredientes nos processadores, carvão no purificador, sementes na plantadora e baterias no carregador. Retiram produtos, itens coletados e baterias. O gerador de pedra dispensa ingredientes.
- **Energia de outros mods:** núcleos e consumidores oferecem a interface FE do NeoForge. Núcleos permitem extração por cabos de outro mod. A interoperabilidade não foi testada com mods externos. Os cabos deste projeto percorrem apenas máquinas deste projeto.

A interface de energia externa só expõe energia limpa. A energia suja permanece no circuito do mod. A pausa por redstone afeta a operação interna; equipamentos externos ainda podem carregar reservatórios habilitados.

A fornalha não consome energia sem receita ou com saída bloqueada. Ela para o progresso quando fica sem energia. Trocar o tipo ou os componentes do item da entrada reinicia o progresso. A saída não aceita itens inseridos pelo jogador.

## Receitas

Todos os equipamentos têm receita na mesa de trabalho. Ao obter um lingote de ferro, as receitas dos equipamentos são liberadas no livro de receitas. Exemplos:

| Receita | Linha superior | Linha central | Linha inferior |
|---|---|---|---|
| 8 Cabos | cobre / cobre / cobre | alga seca / alga seca / alga seca | cobre / cobre / cobre |
| Roda | ferro / tábua de carvalho / ferro | tábua / bloco de cobre / tábua | ferro / redstone / ferro |
| Turbina de Maré | ferro / fragmento de prismarinho / ferro | fragmento / bloco de cobre / fragmento | ferro / bloco de redstone / ferro |
| Purificador | ferro / vidro / ferro | carvão vegetal / fornalha / carvão vegetal | ferro / redstone / ferro |
| Núcleo Compacto | ferro / cobre / ferro | redstone / bloco de lápis-lazúli / redstone | ferro / cobre / ferro |
| Fornalha Elétrica | ferro / cobre / ferro | cobre / fornalha / cobre | ferro / redstone / ferro |

O núcleo médio usa o pequeno como ingrediente; o abissal usa o médio. O restante das receitas também está nos arquivos legíveis `src/main/resources/data/mareeletrica/recipe`.

## Modelos e animações

São 58 objetos colocáveis, 3 granulados, 4 componentes industriais, uma bateria portátil, o frasco de água potável, 3 itens de mel , a Bala de Canhão, o Controle Remoto e a Bala do Explorador: **73 itens obtíveis**, todos com geometria 3D própria. O explorador e o projétil também têm renderizadores próprios. Roda, turbina, cristais, triturador, irrigador e repulsor têm animação prevista no renderizador enquanto ativos. Continuam respeitando a grade do Minecraft; a forma visível não é um cubo cheio.

A imagem `PREVIA_MODELOS.png` é uma projeção dos modelos JSON, com cores simplificadas. Não é uma captura do Minecraft e não comprova as animações ou a iluminação dentro do jogo.

## Persistência e limites desta alfa

- Ao salvar o mundo, energia, inventário, filtro, progresso, botão ligado/desligado, XP guardado, canal de transporte, estado da busca de minério e intervalo do clima são gravados.
- **Quebrar uma máquina derruba seus itens internos, mas descarrega a energia e perde o restante do filtro.** O item da máquina não armazena esses dados.
- As turbinas não removem água do mundo. Cabos e máquinas aceitam água em seus espaços por waterlogging.
- Reservatórios cheios param a geração; não há explosões por sobrecarga.
- Os circuitos só operam em chunks carregados. A busca percorre até 1.024 posições de busca e até 128 blocos por eixo a partir da máquina emissora. Divida usinas maiores em circuitos menores.
- Mais de um núcleo na mesma rede pode alimentar os mesmos consumidores. Núcleos não transferem energia entre si automaticamente.
- A fornalha usa o produto da receita, mas esta versão não concede experiência pela fundição.
- Não há barragem multibloco, tubos de fluidos ou ondas. O carrinho carrega chunks de forma limitada durante controle remoto e retorno imediato. O foco desta entrega é o circuito elétrico e os aparelhos descritos acima.

## Fontes técnicas de referência

A configuração e as APIs foram consultadas na documentação oficial:

- [MDK de NeoForge 1.21.1 com ModDevGradle](https://github.com/NeoForgeMDKs/MDK-1.21.1-ModDevGradle)
- [Entidades de bloco](https://docs.neoforged.net/docs/1.21.1/blockentities/)
- [Menus](https://docs.neoforged.net/docs/1.21.1/gui/menus/)
- [Telas](https://docs.neoforged.net/docs/1.21.1/gui/screens/)
- [Modelos](https://docs.neoforged.net/docs/1.21.1/resources/client/models/)
- [Capabilities](https://docs.neoforged.net/docs/1.21.1/inventories/capabilities/)
- [Plugin ModDevGradle 2.0.107](https://plugins.gradle.org/plugin/net.neoforged.moddev/2.0.107)

A compilação completa passou; essas referências não substituem testes de integração dentro do jogo.
