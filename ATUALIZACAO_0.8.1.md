# Maré Elétrica 0.8.1 — câmera remota

- Conecta a visão somente quando o carrinho e seu chunk estão carregados.
- Deixa de reinstalar a câmera e reiniciar seus efeitos a cada tick.
- Corrige o terreno vazio à distância: a área desenhada acompanha o carrinho, sem mover o personagem.
- R/Esc restauram a visão imediatamente; o servidor libera a sessão e manda o carrinho retornar.
- Recupera a perspectiva anterior, incluindo terceira pessoa.
- Limpa a sessão ao desconectar ou trocar de mundo/personagem e evita enviar jogador ausente aos hooks de renderização.
- Se o carrinho deixar de estar carregado, restaura a câmera do jogador durante a reconexão.

Todos os outros aparelhos, receitas, modelos e texturas da 0.8.0 permanecem. Use o JAR da pasta `INSTALAR`, substituindo o antigo.

O crash específico do usuário não foi reproduzido nem confirmado por um relatório. Esta versão corrige problemas encontrados no ciclo da câmera; não é possível garantir a resolução de um conflito com outros mods sem o registro da queda. Consulte `VALIDACAO.md` para os testes executados.
