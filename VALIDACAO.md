# Validação — Maré Elétrica 0.20.0

Java 21.0.2, Minecraft 1.21.1. Cliente gráfico real e servidor integrado em dois testes:

- NeoForge 21.1.172, sem Punchy.
- NeoForge 21.1.229 com Punchy 2.8g, usando as mesmas classes de produção, conferidas por SHA-256.

Ambos terminaram com código 0 e BUILD SUCCESSFUL. Resultados de execução e uma seleção de capturas em tests/evidence_0.20.0.

Conferido: seis armas, munição compatível, consumo e recarga da reserva, cooldowns, quantidade/velocidade dos projéteis, dano por impacto real, persistência de acessórios, migração do revólver, transferência pelo menu e bloqueio do espaço da arma. Disparo automático e parada também testados.

Renderizados: as seis armas em primeira pessoa, mira de ferro, disparo/recarga, painel, silenciador e mira instalada; oito camuflagens em terceira pessoa; mira da sniper anterior. HUD normal oculto enquanto mira.

Abas conferidas: armas portáteis fora da Hidrelétrica; novas armas na aba guerra; 264 roupas e 22 pinturas em Camuflagem; somente nossas roupas em Armaduras. Todas as 264 receitas carregaram; 64 biomas e 2 cavernas extras cobertos.

Punchy carregado e integração ativa nas duas mãos das armas. Itens comuns continuam habilitados. Capturas revisadas para duplicação.

583 modelos novos/alterados com referências de textura, geometria e UV válidos; 66 texturas de camuflagem presentes no atlas do inventário; receitas únicas e traduções. JAR com 293 classes de produção e 4269 recursos da execução testada, sem classes de teste nem mods de terceiros; hashes conferidos.

Limites: testes com um cliente e servidor integrado, sem o modpack completo do usuário, shaders ou servidor público. A compatibilidade foi verificada especificamente com Punchy 2.8g; outras versões não foram testadas.

SHA-256: `a688c05ddb70c0f138383cea29dbeb786aeea1ce1d2a8a362f1527eebc20b3a1`
