# Maré Elétrica 0.12.0

Minecraft 1.21.1 • NeoForge 21.1.172 • Java 21

## Roda-d’água gigante de madeira

A roda ganhou aros, pás e raios de madeira com reforços metálicos. A armação e o piso foram retirados; ficam a roda e o eixo. O visual foi criado para este mod, sem exigir Create.

Coloque o item em uma área livre de 5×5×5. A área reservada e os identificadores da estrutura anterior foram preservados para atualizar rodas já colocadas. Encoste água na parte inferior da roda: água parada funciona, inclusive dentro da estrutura. A água pode entrar e atravessar a roda. A geração começa automaticamente; a roda não abre painel. A geração máxima continua em 64.000 FE/s.

## Haste de transmissão

Coloque hastes em sequência a partir da ponta visível do eixo. Elas se conectam entre si e também aos cabos e máquinas do mod. Leve a linha até um núcleo; se a energia vier de um bioma oceânico, passe primeiro pelo purificador de energia.

A haste transporta até 128.000 FE por ciclo de dez ticks, compartilhados entre as máquinas que usam o trecho. Quebrar um trecho interrompe esse caminho. As hastes aceitam água sem quebrar. Receita: ferro em cima, graveto no meio e ferro embaixo, rendendo oito hastes.

## Turbina aquática

A antiga Turbina de Correnteza agora se chama Turbina Aquática. O item mantém o mesmo identificador, preservando as turbinas existentes. Ela gera em água parada ou corrente, em contato pela frente, por trás ou dentro da estrutura. A água atravessa a turbina. O tamanho continua 3×3×3 e o máximo é 52.000 FE/s.

As condições das turbinas de queda-d’água e de pressão profunda continuam como na 0.11.0.

## DMT: impulso pela mira e balanço na corda

Equipe o DMT na mão secundária e carregue gás com R. Z e X lançam os ganchos; C solta ambos. Mire na direção em que deseja voar e segure Espaço com pelo menos um gancho preso. A e D ajudam a manobrar.

O gás impulsiona na direção da sua visão. A corda mantém o comprimento que tinha ao prender o gancho; ela não é recolhida automaticamente pelo impulso. Ao alcançar esse limite, a componente do movimento que afastaria você é limitada e o movimento lateral permite balançar ou orbitar o ponto de fixação. Para contornar o gancho, vá ajustando a mira para o lado. Olhar diretamente para fora da corda esticada não aumenta seu alcance.

O limite inicial de lançamento continua em 24 blocos. O gancho não se solta porque você tentou avançar além do comprimento da corda. Soltura manual, remoção do bloco/criatura, troca de dimensão e retirada do DMT continuam encerrando o gancho. O gás continua sendo gasto apenas ao impulsionar com um gancho preso.

## Cama de descanso com LED

Coloque a cama como uma cama comum, deixando dois blocos livres. Os LEDs alternam entre acesos e apagados a cada meio segundo, iluminando o entorno quando acesos.

Durma nela durante a noite. Ao completar o sono e acordar pela manhã, você recebe **Pressa I e Velocidade I por cinco minutos**. Levantar antes de completar a noite não dá os bônus. As regras normais de segurança e de dimensões das camas continuam valendo.

Receita: pó de glowstone na posição superior central; duas bobinas de cobre, uma de cada lado de uma cama roxa, na linha central; três tábuas de carvalho na linha inferior.

## Instalação

Feche o jogo, retire o JAR anterior do Maré Elétrica e copie `INSTALAR/mare-eletrica-0.12.0.jar` para a pasta `mods`. Em multiplayer, atualize cliente e servidor. Consulte `VALIDACAO.md` para os testes realizados.
