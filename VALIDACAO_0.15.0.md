# Validação — Maré Elétrica 0.15.0

Minecraft 1.21.1, NeoForge 21.1.172, Java 21.0.2. Cliente gráfico real com servidor integrado, mundo descartável e renderizador vanilla. Execução em 2026-10-18, código de saída 0 e BUILD SUCCESSFUL.

## Verificado nesta atualização

- Roda nova 7×7×7 colocada por interação; outra roda alinhada e haste central. Energia suja atravessa ambas até o purificador. Água parada natural gera energia suja. Roda antiga de 3×3×3 mantém tamanho e FE durante a execução.
- Núcleo quântico 3×3×3 com 100 milhões de FE. Três tipos de chip instalados nos encaixes; lugar incorreto não consome item. Transferência conservada, limite compartilhado e vínculo preservado em NBT.
- Geotérmico: ciclo produz 10.000 FE. Broca: caminhos reais de saída para detritos ancestrais e molde; resultado pendente preparado para testar os dois tipos sem depender de sorte.
- Balde real enche roda e coloca fonte. Fonte e fluxo são reconhecidos como artificiais e não energizam a roda. Origem serializada e marca removida ao substituir a água.
- H abre painel de cinco espaços; M2 prende gancho direito; scroll regula 0,25 bloco em cada direção; U desativa comandos, solta e permite reativar.
- Lâminas: dano combinado e uma unidade de durabilidade por ataque. Arma de gás: um disparo atinge alvo distante com 18 de dano, usa cartucho/gás. Lança: consumida, voa, impacta e detona. Criatura leve puxada pela corda recolhida sem Shift.
- Sniper: M2 abre mira, evento real de M1 dispara mesmo usando a luneta, consome uma bala e tira 20 de vida de um golem a distância.
- Devorador: 20 cilindros transformam e devolvem 20 vazios; dimensões 5×5×5, prazo de 6.000 ticks, navegação até alvo hostil e ataque, animal passivo preservado. Prazo encurtado no teste para verificar remoção/limpeza da base sem aguardar cinco minutos reais.
- Cama registrada como lar, aldeão dormindo por IA, iluminação do mundo nível 12. Ciclo de cores percorre 1.260 ticks com transições pequenas e sem branco/apagado.
- Capturas reais do painel, armas, mira, roda, núcleo, devorador e cama. Verificação estrutural de receitas, modelos, texturas e traduções.

O JAR usa exatamente as 230 classes de produção e os 2618 recursos da execução. Hashes comparados após empacotar; testes e instrumentação foram excluídos. Evidências em tests/evidence_0.15.0.

## Limites

Não foi reproduzido o modpack completo do usuário, shaders, Embeddium, servidor dedicado nem uma sessão com dois jogadores. Os testes de versões anteriores estão preservados como histórico e não representam uma nova execução dessas versões. Nenhuma sessão longa de cinco minutos foi necessária para conferir o prazo do devorador: valor inicial e encerramento foram verificados separadamente. As chances da broca são pesos de sorteio, não garantia por leva. A origem de água anterior à 0.15 não pode ser recuperada retroativamente. A água natural pode alimentar máquinas, mesmo sem correnteza.

SHA-256: `9bd9dd279275049489be8a6dfcb702846553a8074b66831d7c712da6f1d92014`
