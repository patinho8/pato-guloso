# Validação — Maré Elétrica 0.19.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2. Cliente gráfico real, servidor integrado, mundo descartável, renderizador vanilla. Execução final com código 0 e BUILD SUCCESSFUL.

## Conferido nesta atualização

- Compilação Java e validação de modelos, UVs, referências de texturas, idiomas e receitas.
- Tiro real atravessou quatro blocos permitidos. O quinto barrou o disparo. Os blocos permaneceram intactos; limite também conferido em vários ticks.
- Uma única bala atravessou vaca, zumbi e esqueleto alinhados. Iron Golem recebeu os 20 de dano anteriores e barrou a bala; alvo atrás dele intacto.
- SMG causou 3 de dano por bala sem armadura. Velocidade inicial real da sniper: 40 blocos por tick.
- Campo de visão medido no renderizador: mira 9× cerca de 8,9 graus; 48× cerca de 1,67 grau com FOV normal 70. Diferença maior que cinco vezes. Somente HUD da arma desenhado.
- Vaca existente a 224 blocos começou fora do rastreamento. A mira recebeu o mob e o terreno, permitiu renderizá-lo e um tiro real o atingiu fora da distância de simulação normal.
- Configuração de visão global do servidor e opção de renderização do usuário permaneceram iguais. Terreno adicional permaneceu ao baixar a mira; abrir inventário limpou o estado do cliente e liberou tickets do servidor. Limite por observador conferido.
- Velocidade ao deitar: 55% da caminhada normal. Campo de visão normal preservado.
- Dez novas skins da sniper registradas e exibidas no painel real; pintura própria da SMG com silenciador exibida. Prévia do painel conferida após trocar diretamente de sniper para SMG: usa a arma sincronizada e o dano informado é 3. Capturas revisadas em tests/evidence_0.19.0.

O JAR contém as 276 classes de produção e 3346 recursos da execução final, com hashes conferidos após empacotar e classes de teste excluídas.

## Limites

Teste feito com um cliente e servidor integrado, sem o modpack completo do usuário, shaders, Embeddium ou duas conexões em servidor dedicado. A configuração global foi verificada; isolamento entre duas conexões não foi reproduzido. A mira carrega mobs existentes; não cria mobs ou força a simulação completa distante. Os limites de 32 chunks de alcance e 512 chunks retidos por observador evitam carregamento ilimitado. O envio é gradual.

As dez referências de skins da submetralhadora ainda não foram enviadas; as seis pinturas existentes foram diferenciadas nesta versão. Skins da sniper são releituras das referências, não cópias exatas dos arquivos originais.

SHA-256: `6356535ba124f817bb7c6ff2d684f95a5d139afcdd7617ceb50e841f5f0cc5ed`
