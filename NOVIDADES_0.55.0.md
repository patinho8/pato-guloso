# Novidades da 0.55.0 — 🥊 Ringue de Galos e batalhas estilo Pokémon

### Ringue de Galos (aba ⚽ Esportes)
- Ringue 5x5 com lona, quatro postes (dois vermelhos e dois azuis) e três cordas.
- Receita: lã vermelha, linha e lã azul em cima, três lãs brancas embaixo.
- **Batalha entre jogadores**:
  1. Com a sua galinha por perto (até 12 blocos), clique no ringue.
  2. Seu amigo faz o mesmo com a galinha dele.
  3. As duas galinhas vão para os cantos e a batalha começa.
- **Treino sozinho**: **agache + clique** no ringue e você luta contra uma galinha do computador, de raça surpresa e nível parecido com o da sua.

### Batalha por turnos
- Abre uma tela com as duas galinhas, as barras de vida, o que está acontecendo e os **4 golpes** da sua galinha.
  - Passe o mouse em cima de um golpe para ver o tipo, o poder, a precisão e o efeito.
- Cada um escolhe o golpe. Ataca primeiro a galinha **mais rápida**; a **Bicada Rápida** sempre vai antes.
- Você tem 40 segundos por turno. Se demorar, o golpe é escolhido na sorte.
- Contam:
  - tipo (**super efetivo** ×2 / pouco efetivo ×½)
  - **bônus de mesmo tipo** (×1,5)
  - **acerto crítico**
  - precisão (o golpe pode errar)
- Efeitos:
  - **queimadura** (perde vida todo turno e o ataque cai)
  - **paralisia** (fica lenta e às vezes não se mexe)
  - **congelamento** (não ataca até descongelar)
  - **susto** (perde a vez)
  - **buffs e debuffs** de ataque, defesa e velocidade
  - **cura**, **roubo de vida** e **dano de recuo**
- **Desistir** dá a vitória para o outro. A galinha que perde fica nocauteada e a vencedora ganha XP.

### 9 raças, cada uma com sua cor e seu tipo
| Raça | Tipo | Ponto forte |
|---|---|---|
| Galo de Briga | Normal | equilibrado |
| Galo Brasa | Fogo | ataque e velocidade |
| Galo Maré | Água | vida e defesa |
| Galo Folha | Planta | vida |
| Galo Faísca | Elétrico | o mais rápido |
| Galo Geada | Gelo | equilibrado |
| Galo Rocha | Pedra | defesa, mas lento |
| Galo Ventania | Vento | muito rápido |
| Galo Sombrio | Sombra | ataque |

- **Ovo Surpresa de Galo** (a receita antiga) choca uma raça aleatória.
- **Ovo de cada raça**: Ovo Surpresa + um item:
  - trigo (Normal)
  - creme de magma (Fogo)
  - fragmento de prismarinho (Água)
  - bloco de musgo (Planta)
  - redstone (Elétrico)
  - bola de neve (Gelo)
  - pedregulho (Pedra)
  - membrana de phantom (Vento)
  - bolsa de tinta (Sombra)

### Tabela de tipos (quem ganha de quem)
- **Fogo** ganha de Planta e Gelo, e perde de Água e Pedra.
- **Água** ganha de Fogo e Pedra, e perde de Planta.
- **Planta** ganha de Água e Pedra, e perde de Fogo e Vento.
- **Elétrico** ganha de Água e Vento, e quase não faz efeito em Pedra e Planta.
- **Gelo** ganha de Planta e Vento, e perde de Fogo.
- **Pedra** ganha de Fogo, Gelo e Vento, e perde de Planta.
- **Vento** ganha de Planta, e perde de Elétrico e Pedra.
- **Sombra** ganha de Sombra e Elétrico, e pouco efeito em Normal.

### 31 golpes
- **Dano**: Bicada, Voadora, Bico em Brasa, Crista Flamejante, Jato d'Água, Onda de Penas, Chicote de Capim, Bicada Choque, Raio do Galo, Granizo, Nevasca, Pedrada, Avalanche, Rajada, Furacão de Asas, Garra Sombria, Pesadelo…
- **Buffs**: Canto de Guerra, Penas Eriçadas, Dança do Sol, Carga Elétrica, Casca Dura, Voo Alto.
- **Debuffs**: Olhar Bravo, Medo, Bafo Gelado, Esporos (paralisa).
- **Cura**: Descanso no Ninho, Chuva Calma, Semente Sugadora.
- Cada galinha começa com 2 golpes e aprende mais um nos níveis 3, 5, 7 e 9. Na batalha ela usa os 4 mais novos.
- Clique na sua galinha com o **Apito** para ver a raça, o tipo e os golpes dela.

## 0.55.1 — golpes exclusivos de cada raça
Agora cada raça tem os seus 6 golpes, todos do próprio tipo. A Bicada, a Voadora e os outros golpes Normais ficaram só para o Galo de Briga. Cada raça ganhou um **golpe final**, que ela aprende no nível 9, e quatro tipos ganharam um golpe que **sempre ataca primeiro**.

Na tabela, a coluna "Começa com" são os 2 golpes do nível 1; os outros são aprendidos nos níveis 3, 5, 7 e 9.

| Raça | Começa com | Nível 3 | Nível 5 | Nível 7 | Nível 9 (golpe final) |
|---|---|---|---|---|---|
| Galo de Briga (Normal) | Bicada, Voadora | Canto de Guerra | Bicada Rápida ⚡ | Olhar Bravo | Descanso no Ninho |
| Galo Brasa (Fogo) | Fagulha, Bico em Brasa | Dança do Sol | Rasante Flamejante | Crista Flamejante | **Ovo Infernal** |
| Galo Maré (Água) | Bolhas, Jato d'Água | Chuva Calma | Cascata | Onda de Penas | **Tsunami do Galinheiro** |
| Galo Folha (Planta) | Chicote de Capim, Folha Navalha | Esporos | Semente Sugadora | Raízes | **Tempestade de Pétalas** |
| Galo Faísca (Elétrico) | Faísca, Choque Rápido ⚡ | Carga Elétrica | Bicada Choque | Raio do Galo | **Trovoada** |
| Galo Geada (Gelo) | Granizo, Bicada de Gelo | Bafo Gelado | Armadura de Gelo | Nevasca | **Era do Gelo** |
| Galo Rocha (Pedra) | Cascalho, Pedrada | Casca Dura | Cabeçada de Pedra | Avalanche | **Terremoto** |
| Galo Ventania (Vento) | Corte de Vento ⚡, Rajada | Voo Alto | Mergulho Aéreo | Furacão de Asas | **Ciclone** |
| Galo Sombrio (Sombra) | Sombra Furtiva ⚡, Garra Sombria | Medo | Maldição | Pesadelo | **Eclipse** |

⚡ = golpe que sempre ataca primeiro.
