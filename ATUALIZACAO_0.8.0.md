# Maré Elétrica 0.8.0

Minecraft Java 1.21.1 • NeoForge 21.1.172 • Java 21.

## Base 3×3×3

Coloque a Base do Explorador em uma área 3×3×3 livre, com espaço à frente. Conecte energia limpa e ligue no painel. A base guarda 1.000.000 FE e cria um carrinho com bateria de 100.000 FE, seis rodas animadas e capacidade para 256 Balas do Explorador.

A exploração automática mantém o ciclo norte → sul → leste → oeste, escaneia minérios e retorna para recarregar. Obstáculos podem interromper o retorno; ele não atravessa paredes. Uma Bateria Portátil transfere até 10.000 FE por interação diretamente no veículo.

Bases de mundos antigos conservam o tamanho anterior. Recolha e recoloque para ampliar. Antes de quebrar a base, retire a munição do carrinho: agache e interaja com a mão vazia para retirar até 64 por vez. Desmontar a base invalida seu veículo.

## Controle e câmera

1. Crie o Controle Remoto do Explorador.
2. Segure o controle, agache e use-o em qualquer peça da base para vincular.
3. Fique em terra firme e use no ar. A base e o carrinho precisam estar na mesma dimensão e a até 1.024 blocos de você.
4. A câmera passa para o módulo roxo. Seu personagem permanece parado no local onde iniciou o controle.

| Comando padrão | Ação |
|---|---|
| W / S | Avançar / recuar |
| A / D | Virar |
| Mouse | Direção e mira |
| Ataque | Atirar; pode manter pressionado |
| R ou Esc | Encerrar e pedir retorno |

O painel mostra bateria, munição, modo e distância. O controle termina ao receber dano, perder sinal, deixar de segurar o controle, trocar de dimensão ou esgotar a bateria. Seu corpo continua exposto no mundo.

O remoto mantém carregada uma região limitada ao redor do carrinho e da base. Ao encerrar, mantém a região durante o retorno até atracar, parar, ficar sem energia ou atingir dez minutos. Expedições automáticas normais dependem dos chunks já carregados.

## Barco e asas

Ao tocar a água, o casco se abre e as rodas recolhem. Corpo, câmera, bateria e munição permanecem. Ao sair da água, volta às rodas.

Em uma queda de pelo menos 2,5 blocos, duas asas abrem para planar. Elas recolhem ao pousar ou entrar na água. O carrinho não sofre dano de queda. O planador limita a descida; não oferece voo livre para subir.

## Balas do Explorador

É uma munição nova, diferente da Bala de Canhão. Coloque-a no espaço da base ou use diretamente no carrinho. Ele recebe até 32 balas por meio segundo enquanto atracado, até o limite de 256.

**Só atira no modo manual.** Cada disparo usa uma bala e 200 FE, com intervalo de 0,4 segundo. Causa 6 pontos de dano a criaturas hostis, sem explosões ou destruição de blocos. Não ataca jogadores ou animais passivos.

Pilotar gasta 400 FE/s avançando ou recuando e 100 FE/s parado, além dos disparos. Valores a 20 ticks/s.

## Seis canhões ajustáveis

Abra o morteiro e configure cada tubo. A numeração segue da esquerda para a direita, primeiro a fileira da frente, depois a de trás, olhando a frente da máquina.

| Ajuste | Significado |
|---|---|
| Elevação 0° | Horizontal |
| Elevação 30° | Trinta graus acima do horizonte |
| Elevação 60° | Sessenta graus acima do horizonte |
| Elevação 90° | Vertical |
| Direção negativa | Esquerda da frente da máquina |
| Direção positiva | Direita da frente da máquina |

Exemplo: **canhão 1: 30° / +10°; canhão 2: 60° / 0°**. Digite o grau inteiro ou use os botões de cinco em cinco. Elevação aceita 0°–90°; direção aceita −90°–+90°.

Aplicar guarda a mira; Ativar morteiro também aplica antes de armar. Forneça **6 Balas de Canhão e 6.000 FE**, deixe ligado e ative no painel ou com a subida de um sinal de redstone. A salva sai após **dois segundos**. A mira fica bloqueada durante a contagem, é salva no mundo e move visualmente os tubos. Os projéteis mantêm suas explosões.

## Receitas novas

“—” significa espaço vazio. Bobina e Placa são componentes do mod.

| Resultado | Linha superior | Linha central | Linha inferior |
|---|---|---|---|
| Controle remoto | — / Fragmento de ametista / Luneta | Bobina / Módulo de Controle / Bobina | — / Placa de Ferro / — |
| 16 Balas do Explorador | — / Pepita de ferro / — | Cobre / Pólvora / Cobre | — / Cobre / — |

O catálogo agora tem **73 itens obtíveis: 58 objetos colocáveis e 15 itens/componentes**. Os aparelhos anteriores permanecem. Consulte `GUIA.md` e `VALIDACAO.md`.
