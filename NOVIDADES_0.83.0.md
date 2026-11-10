# Maré Elétrica 0.83.0 — 💣 Modo Bomba (estilo Counter-Strike)

Um modo de jogo completo, com mapa, times, rounds, bomba e bots. Fica na aba **💣 Explosivos e defesa**.

## 🗺 Mapa "Deserto"
- O item **Mapa Modo Bomba: Deserto** constrói a arena (80 × 60 blocos) no lugar onde você clica. **Ela substitui tudo o que estiver na área.**
- O mapa tem:
  - spawn dos **TR** (chão amarelo) e dos **CT** (chão azul);
  - **bombsite A** e **bombsite B**, com a letra pintada no chão;
  - **meio**, **passarela** do meio até o A, **janela** do meio até o B, **long A** e **túneis B**;
  - caixas de madeira para se esconder, paredes de arenito e lanternas.
- O **Terminal do Modo Bomba** fica no spawn TR.

## 🎮 Como jogar
- **Clique no terminal com a mão vazia** para escolher o time (CT ou TR). Clicando com algum item na mão, muda o tamanho da partida (1×1 até 5×5).
- **Shift + clique** começa a partida. Os jogadores perto do terminal entram, e os lugares que sobram em cada time são preenchidos por **bots**. Shift + clique de novo encerra.
- Cada round:
  - **5 s de espera** (ninguém se mexe) e depois **1:55** de round.
  - Kit automático. TR: AK-47, Glock, karambit e pentes. CT: M4A1, USP, butterfly, pentes e **tesoura (kit de desarme)**. Cada time ganha um colete na cor dele.
  - Um TR recebe a **bomba**. Segure o botão direito por **3 s** dentro do bombsite A ou B para plantar. Se quem estiver com a bomba morrer, ela passa para outro TR.
  - Bomba plantada: **40 s** para explodir. Os CT desarmam segurando o botão direito nela: **5 s com tesoura** e 10 s sem.
  - A explosão da bomba mata quem estiver perto, mas não destrói o mapa.
- Como cada time ganha o round:
  - **TR:** a bomba explode, ou todos os CT morrem.
  - **CT:** a bomba é desarmada, todos os TR morrem antes de plantar, ou o tempo acaba.
- Quem morre fica **assistindo** (espectador) até o próximo round, sem perder nada do inventário.
- Os itens do kit somem no fim da partida. O resto do inventário não é mexido.
- **Primeiro a 7 rounds vence.**
- Uma barra no topo da tela mostra o placar, o tempo, a bomba e quantos estão vivos de cada lado. O chat mostra quem matou quem.

## 🤖 Bots
- Os bots parecem jogadores (skins padrão), usam o colete do time e andam com fuzil ou pistola.
- **Bots TR:** vão para o bombsite do round (alguns empurram pelo meio), e quem está com a bomba planta e defende.
- **Bots CT:** se dividem entre o A e o B e correm para a bomba quando ela é plantada. Eles desarmam (o bot fica agachado mexendo na bomba).
- Eles atiram em quem enxergam, com mira imperfeita: erram mais de longe, quando estão andando e quando o alvo está agachado. Tiro na cabeça dá mais dano.
- Não existe fogo amigo, nem entre jogadores do mesmo time nem entre bots do mesmo time.
