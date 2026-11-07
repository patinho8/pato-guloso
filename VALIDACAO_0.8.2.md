# Validação — 0.8.2

## Falha identificada

O relatório fornecido pelo usuário confirmou o conflito entre `mareeletrica.mixins.json:client.LevelRendererMixin` e `org.embeddedt.embeddium.impl.mixin.core.render.world.WorldRendererMixin`, ambos com prioridade 1000, durante a preparação de injeções em `LevelRenderer.setupRender`.

## Compilação

Java 21.0.2, Gradle 8.10.2, ModDevGradle 2.0.107 e NeoForge 21.1.172. Compilação de produção concluída. Confirmadas a presença da seleção de compatibilidade e a ausência da classe do teste gráfico no JAR distribuído.

## Teste gráfico com Embeddium — aprovado

Executado com Minecraft 1.21.1, NeoForge 21.1.172, Maré Elétrica 0.8.2 e Embeddium 1.0.15+mc1.21.1, em um cliente gráfico real com servidor integrado. A lista de mods confirmou o carregamento do Embeddium, e o plugin registrou a seleção de seu renderizador.

- O jogo abriu e criou um mundo de sobrevivência descartável.
- Uma base completa de 3×3×3 foi montada, com o carrinho a aproximadamente 186 blocos do jogador, além da distância de renderização de 4 chunks.
- O controle entrou na câmera pela interação normal do item.
- O carrinho foi dirigido por 70 ticks e atravessou um limite de chunk. O terreno permaneceu visível e o corpo do jogador permaneceu parado, com verificação no cliente e no servidor.
- A saída restaurou a câmera e a perspectiva anterior. A reentrada e a desconexão com a câmera ativa também passaram.
- O teste registrou PASS e terminou com BUILD SUCCESSFUL, código de saída 0.

As 119 classes de produção usadas nesse teste foram comparadas com as do JAR distribuído e eram idênticas. A classe de teste não está no JAR. Ao recuperar o pacote para a entrega, o SHA-256 abaixo foi novamente conferido.

## Teste gráfico sem Embeddium — parcial

O jogo abriu, criou o mundo e permitiu entrar na câmera, dirigir com terreno visível, sair e entrar novamente. A execução ficou parada ao encerrar o mundo, durante o salvamento, e foi interrompida. Não houve resultado final PASS nesse teste. A causa dessa demora não foi determinada; a validação de encerramento sem Embeddium permanece inconclusiva.

## Registro dos resultados

Este documento resume os resultados observados nas execuções da 0.8.2. Os logs e a captura de tela originais dessa execução não foram preservados no pacote. Os arquivos antigos em `tests/evidence` pertencem ao teste da 0.8.1, conforme o README dessa pasta, e não devem ser apresentados como evidência da 0.8.2.

## Limites

O modpack Reinos completo e seus shaders não foram reproduzidos. O teste focaliza o conflito relatado e a câmera; não testa novamente todos os aparelhos. O ambiente de compilação usa um ajuste local para fornecer o caminho do Java ao NeoFormRuntime quando ProcessHandle não o informa. Esse ajuste não faz parte do JAR.

SHA-256 do JAR: `b451a92d0638334a1f1426cbb186ff26219002b716b4b7f8dfb0f02322277cf9`
