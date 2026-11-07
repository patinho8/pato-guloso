# Maré Elétrica 0.18.0 — Arsenal Vórtice

Minecraft 1.21.1 · NeoForge 21.1.172 · Java 21

## Sniper

- Dano base de **60** nos alvos comuns, três vezes os 20 anteriores. Iron Golem, Wither, Ender Dragon (inclusive suas partes) e entidades classificadas pela tag de chefes `c:bosses` continuam com **20**. Armadura, resistência e regras próprias do alvo ainda se aplicam.
- Para um chefe de outro mod que não use essa classificação, um datapack pode acrescentar seu ID à tag `mareeletrica:sniper_base_damage`. Não existe uma identificação universal de chefes não classificados pelos outros mods.
- Tiros atravessam até **8 blocos permitidos por bala**: tábuas e peças de madeira, troncos, vidro comum/colorido/escurecido e painéis de vidro, lã, slime, terra e variantes, lama, terreno arado, caminho e areia. Os blocos ficam intactos; pedra, metal e materiais fora da lista barram a bala. Acerta o primeiro alvo no caminho.
- Estampido mais alto e mais longo. Os cinco silenciadores reduzem o estrondo, mas conservam o estalo e o som mecânico do disparo.
- Oito novas lunetas: **7×, 9×, 14×, 18×, 28×, 32×, 40× e 48×**. São 18 lunetas instaláveis, além da mira padrão. Trocar a luneta continua zerando a regulagem.
- Seis pinturas refeitas com relevos e desenhos mais detalhados: Tempestade Imperial, Dragão Carmesim, Sakura Neon, Circuito Espectral, Eclipse Dourado e Abismo Astral. Cobrem corpo e acessórios, preservando partes funcionais escuras.

## Submetralhadora Vórtice

| Controle | Função |
|---|---|
| Segurar M1 | Disparo automático, até 10 tiros por segundo |
| Segurar M2 | Mirar |
| R | Recarregar em 1,5 segundo |
| J ou Shift + M2, em pé | Abrir o painel de acessórios |
| Shift duas vezes | Deitar ou levantar |

- Carregador de **30 tiros**, dano base de **5 por bala**, cartucho próprio. R usa primeiro a munição guardada no painel e depois a do inventário.
- Painel com **4 espaços**: mira, silenciador, adesivo e reserva de munição. Não aceita bipé. Os cinco silenciadores da sniper também servem nela.
- Quatro miras compactas, de **1× a 4×**, com aro e ponto vermelhos inspirados na referência enviada.
- Seis adesivos próprios, combinando com as pinturas da sniper.
- Duas mãos seguram a arma. Movimento ao levantar para mirar, recuo, ferrolho móvel, clarão, estojo e fumaça discreta durante os disparos; carregador se move ao recarregar.
- Soltar M1, abrir um menu, trocar de arma ou perder o foco interrompe a rajada. Sem munição não dispara. A cadência é controlada pelo servidor.

## Visão e poses

- Mirando em primeira pessoa com sniper ou SMG, aparecem apenas a retícula e os dados da arma. Vida, fome, sede, inventário rápido e demais camadas do HUD ficam escondidos e voltam ao sair da mira.
- F5 mantém a câmera externa e mostra a pose do personagem com a arma.
- O revólver agora usa as duas mãos, em primeira e terceira pessoa.
- A mira não aumenta a distância de renderização nem muda as configurações dos demais jogadores.

## Instalação

Retire o JAR antigo do Maré Elétrica da pasta `mods` e coloque apenas `INSTALAR/mare-eletrica-0.18.0.jar`. Em multiplayer, servidor e jogadores precisam da mesma versão. As receitas novas aparecem no livro de receitas após obter ferro, e os equipamentos ficam na aba Itens de guerra.

O alcance visual continua limitado pelos chunks já carregados. Escopos muito fortes aproximam o que existe nessa distância; não criam terreno distante.
