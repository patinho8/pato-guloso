# Maré Elétrica 0.80.0 — Bala por bala, miras e inventário

## 🔫 Recarga bala por bala
- **Python .357, M870 e SPAS-12** agora são carregadas **uma bala (ou cartucho) por vez**, em vez de trocar um pente inteiro.
  - A Python abre o tambor, empurra o extrator e recebe cada bala na mão, uma de cada vez, depois fecha.
  - As escopetas recebem cada cartucho pela janela de baixo e terminam com a bombeada.
- A recarga coloca só o que falta e só o que houver no inventário: primeiro a munição solta (balas .357 / cartuchos), depois as balas dos pentes daquela arma.
- **Atirar interrompe a recarga** e mantém o que já entrou.
- A munição na mão agora é um modelo 3D pequeno (bala de latão com ponta de cobre / cartucho vermelho com base de latão), no lugar do ícone achatado.
- As balas não aparecem mais desenhadas no tambor da Python.

## 🎯 Miras
- Só as snipers (Sniper, Barrett M82 e Dragunov SVD) usam a tela de luneta.
- Todas as outras armas miram do mesmo jeito, olhando pela mira da própria arma, com um zoom leve que muda pouco de uma mira para outra: mira de ferro 1,15×, red dot 1,25×, holográfica 1,5×, prisma 1,75×, 4× → 1,95×, 6× → 2,2× (pistolas um pouco menos).

## 🎒 Inventário
- O ícone de cada arma foi ajustado para caber dentro de um quadrado do inventário (antes as armas compridas saíam do quadrado).
