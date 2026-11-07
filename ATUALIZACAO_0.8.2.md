# Maré Elétrica 0.8.2 — compatibilidade com Embeddium

O relatório de 08/10/2026 às 17:41:45 confirmou uma `InvalidInjectionException` na inicialização. O `client.LevelRendererMixin` do Maré Elétrica tentava modificar `LevelRenderer.setupRender`, método substituído pelo `WorldRendererMixin` do Embeddium.

O parâmetro `require=0` usado anteriormente tolera pontos de injeção ausentes, mas não evita a verificação de conflito com um método sobrescrito por outro mixin.

O novo `MareMixinPlugin` consulta os mods descobertos pelo NeoForge antes de aplicar o ajuste de terreno vanilla. Quando Embeddium está presente, esse ajuste não é aplicado: o renderizador do Embeddium já acompanha a posição da câmera. Sem Embeddium, o ajuste de terreno da 0.8.1 continua ativo.

A câmera remota, os controles, os aparelhos, modelos, receitas e dados salvos foram preservados. Embeddium continua opcional.

Substitua o JAR anterior pelo arquivo `INSTALAR/mare-eletrica-0.8.2.jar`. Deixe apenas uma versão do Maré Elétrica na pasta `mods`. Não é necessário retirar Embeddium para aplicar a correção. Consulte `VALIDACAO.md` para os testes e limites.
