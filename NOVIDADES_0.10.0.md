# Maré Elétrica 0.10.0 — Minecraft 1.21.1 / NeoForge

## Torneira e dutos de água

Os dutos agora aceitam água ao redor sem serem destruídos. Monte entrada azul → dutos → saída laranja; a entrada deve tocar uma fonte de água. Clique na face de um bocal para apontá-lo. Para uma torneira, deixe a saída horizontal ou voltada para baixo, com espaço livre para o jato cair.

Cada entrega move uma fonte real. O jato estreito cai até o chão e a água bombeada preenche cavidades fechadas de baixo para cima, uma posição por entrega. Quando a água chega à borda, pode transbordar. A boca precisa continuar livre e a entrada precisa de fontes. O jato desaparece pouco depois de interromper o fornecimento; a água acumulada permanece.

Para manter o custo previsível, a busca de uma camada verifica até 2.048 posições, num raio de 24 blocos; a queda alcança 128 blocos. A rede mantém seu limite de 64 blocos e 256 posições. Não carrega chunks e não substitui blocos sólidos.

## Exterminadores

O guardião normal tem corpo humanoide esguio, espada em uma mão e metralhadora na outra. À distância, dispara três vezes por segundo; de perto usa a espada. Continua atacando apenas mobs hostis e precisa de energia limpa na base.

A transformação consome 20 cilindros e devolve 20 vazios na saída da base. A aranha funciona com esse combustível por 20 minutos de tempo do mundo, sem o antigo consumo contínuo de 50.000 FE/s. Ela ataca hostis próximos, não empurra quem passa embaixo e desaparece ao terminar o tempo. A base desliga: ligue novamente para criar outro guardião normal. Desligar a base pausa os ataques, mas não o prazo da transformação.

## Projetor

Clique em **Esticar**: aparecem setas vermelha X, verde Y e azul Z. Segure a ponta de uma seta e puxe; solte para aplicar o tamanho. Ainda é possível digitar as dimensões. Botão direito gira a construção e a roda aproxima, facilitando alcançar cada eixo.

Escolha uma cor e **segure o botão esquerdo**, passando pelos blocos para pintar. Use camadas para chegar aos blocos internos. Shift + clique continua pintando o mesmo material. A pintura é enviada em grupos e preservada ao salvar o mundo. Os limites anteriores de tamanho permanecem.

## Avião e lancha

O avião ficou menor, com cabine arredondada em degraus, asas compactas e pequenos motores. O barco virou uma lancha compacta, com proa, para-brisa, assentos e motor. Os controles continuam: G alterna voo; I abre a carga; Espaço/Shift ajustam altitude; R sai.

Ao abastecer o avião, o cilindro vazio vai para os três espaços livres de carga. Se estiverem cheios, cai como item ao lado do veículo. O gás restante continua salvo.

## Central de Recarga de Gás — 5×5×5

Deixe uma área livre de 5×5×5, coloque a central e conecte energia limpa. Abra, coloque **Cilindro de Gás Vazio** na entrada e retire o gás cheio da saída. Cada recarga leva 5 segundos e custa 25.000 FE. Até dez cilindros da entrada aparecem nas prateleiras da máquina. Saída cheia ou falta de energia pausa a recarga.

A broca agora deixa um cilindro vazio em cada espaço de gás consumido; retire-o antes de reabastecer. O exterminador devolve os vazios em sua saída. Isso fecha o ciclo sem precisar fabricar outro cilindro a cada uso.

## Controle de Mira — holofote e morteiro

1. Com o controle na mão, segure Shift e clique com o botão direito no holofote ou em uma parte do morteiro.
2. Use o controle no ar para começar. A máquina liga e sua cabeça acompanha a direção em que você olha, com giro horizontal completo.
3. No morteiro, a roda escolhe todos os canhões ou um canhão específico. Clique esquerdo arma o disparo; a espera de dois segundos e o consumo de seis balas de canhão continuam.
4. Use novamente o botão direito no ar para sair e deixar a mira naquela posição.

O controle funciona até 256 blocos, na mesma dimensão e com a máquina carregada. O holofote aponta para cima ou para baixo; o morteiro vai do horizonte ao alto. A tela antiga do morteiro continua permitindo ângulos individuais, agora com giro horizontal completo. Para iluminar, mantenha energia no holofote; para disparar, abasteça o morteiro com balas de canhão e energia.

## Instalação

Feche o jogo. Retire o JAR anterior do Maré Elétrica e coloque somente `INSTALAR/mare-eletrica-0.10.0.jar` na pasta `mods`. Use Minecraft 1.21.1, NeoForge 21.1.172 ou posterior da linha 21.1 e Java 21. Atualize também o servidor quando jogar em multiplayer. Não precisa recompilar.

O DMT fica para uma próxima versão.
