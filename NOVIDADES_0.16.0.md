# Maré Elétrica 0.16.0

Minecraft 1.21.1 · NeoForge 21.1.172 · Java 21

## Instalação

Extraia o ZIP. Coloque `MareEletrica/INSTALAR/mare-eletrica-0.16.0.jar` na pasta `mods`, retirando o JAR anterior do Maré Elétrica. Cliente e servidor usam a mesma versão. Nenhuma biblioteca extra é exigida.

## Sniper modular

Modelo preto novo, com cano longo, coronha vazada, ferrolho, luneta e acessórios visíveis. O painel abre com **J** (configurável em Controles) ou **Shift + M2** enquanto estiver em pé.

| Controle | Função |
|---|---|
| Segurar M2 | Mirar; aparece apenas um ponto vermelho dentro da luneta |
| M1 | Disparar uma vez; a arma sai da mira e dá recuo |
| Soltar e apertar M2 novamente | Voltar à mira após o intervalo do tiro |
| J | Abrir os acessórios e a calibração |
| Shift duas vezes rapidamente | Alternar entre deitar e levantar |

O tiro mantém 20 de dano base, abaixo de 25% da vida de um golem de ferro. Usa cartuchos de precisão do painel primeiro e depois do inventário. Intervalo de 1,75 segundo. Som próprio forte, fumaça no cano e recuo da câmera e da arma.

### Cinco espaços no painel

1. **Mira:** dez lunetas intercambiáveis, de **2×, 3×, 4×, 6×, 8×, 10×, 12×, 16×, 20× e 24×**. Sem módulo, usa a luneta básica de 5×.
2. **Silenciador:** cinco modelos — Compacto, Alongado, Tático, Carbono e Elite. Cada nível reduz mais o som e um pouco do recuo. O tiro ainda pode ser ouvido perto da arma.
3. **Bipé:** deite no chão e fique parado para apoiar. O HUD mostra **BIPÉ APOIADO**. Andar tira a estabilidade.
4. **Adesivo:** seis desenhos — Raio, Estrela, Coração, Seta, Cruz e Maré. Aparecem na coronha.
5. **Munição:** reserva de até 64 cartuchos de precisão.

Os acessórios, ajustes e cartuchos ficam dentro da própria sniper ao guardá-la ou jogá-la no chão. Cada espaço aceita apenas o item correspondente. Todos os módulos têm receitas de bancada.

### Calibrar a mira

Ao colocar ou trocar uma luneta, os três ajustes voltam a **0**. O painel permite regular:

- **Distância:** de 0 a 512 blocos, em passos de 16. Compensa a pequena queda do projétil na distância escolhida.
- **Horizontal / Vertical:** ajustes finos de 0,02° por passo, até 30 passos para cada lado.
- **Zerar ajustes:** retorna os três valores a 0.

Use um alvo fixo no jogo para conferir os ajustes. A posição do ponto vermelho permanece no centro; a regulagem altera a direção do disparo. Deitado sem bipé apoiado, há dispersão, então a calibração não elimina essa instabilidade.

### Visão distante

Ao mirar, a luneta aumenta temporariamente a distância de renderização **somente se o alcance previsto pelo zoom superar a configuração atual**, até o limite de 32 chunks. Ao sair da mira, trocar de item ou desconectar, a distância anterior é restaurada. A distância de simulação não é aumentada.

Se o Minecraft estiver iniciado com menos de 1 GB de memória máxima, esse aumento é limitado a 16 chunks.

No mundo individual, o servidor integrado acompanha a distância de visão. Em servidor multiplayer, continua valendo também o limite de visão configurado pelo servidor. Chunks novos precisam de tempo para carregar; uma luneta não revela instantaneamente terreno ainda não recebido.

## Deitar

Dê dois toques no Shift em até aproximadamente 0,35 segundo. O personagem fica deitado, com câmera e caixa de colisão baixas, e rasteja devagar. Dois toques novamente levantam, desde que haja espaço acima.

É possível disparar deitado. **Sem bipé, ou em movimento, a sniper perde precisão.** Com bipé e parado no chão, fica estável e o recuo diminui. Entrar na água, montar ou voar com élitros encerra a postura.

## Revólver

Nova arma 3D com tambor de seis tiros. **M1 dispara**; **R ou M2 recarrega** usando cartuchos de revólver do inventário. A tecla R pode ser alterada em Controles. A recarga leva dois segundos e pode preencher parcialmente o tambor se houver pouca munição. Causa 12 de dano base, com projétil rápido, som próprio, fumaça e recuo.

## Abas do criativo

A aba Armaduras deixa de copiar armaduras do Minecraft/outros mods e não inclui o DMT. Como o Maré Elétrica ainda não possui conjuntos próprios de armadura, ela fica vazia e o Minecraft pode ocultá-la.

A aba Itens de guerra reúne morteiro, DMT, sniper e o novo revólver, com suas munições e acessórios. Exterminador, torreta, carrinho e munição do carrinho ficam na aba principal de máquinas.

## Encaixes dos chips

As máquinas compatíveis têm agora um painel frontal com três encaixes expostos. O painel tem área de seleção própria: mire no encaixe e use o chip correspondente. Isso também vale nas peças de máquinas grandes e nas quatro orientações.

As ligações existentes são preservadas. Os chips já instalados aparecem nos novos encaixes. Shift + clique com a placa no núcleo vincula as placas; na máquina, retira a placa do encaixe.
