# Validação — Maré Elétrica 0.18.0

Minecraft 1.21.1, NeoForge 21.1.172 e Java 21.0.2. Cliente gráfico real, servidor integrado, mundo descartável, renderizador vanilla. Execução final encerrada com código 0 e BUILD SUCCESSFUL.

## Conferido

- Compilação Java e verificação estrutural de modelos, UVs, referências de texturas, idiomas e receitas.
- Balas reais atravessaram 24 exemplos de materiais permitidos e uma parede mista de sete camadas, causando 60 de dano num alvo comum com 100 de vida. Blocos permaneceram intactos. Pedra, vidro seguido de pedra e nove camadas consecutivas barraram o disparo.
- Iron Golem sofreu os 20 anteriores em tiro real, em pé e deitado. Wither, Ender Dragon e partes do dragão conferidos na seleção de dano base. Tags usam o padrão NeoForge c:bosses e uma tag adicional configurável.
- 18 lunetas registradas, módulos persistentes, calibração reiniciada ao trocar e espaços protegidos contra itens inválidos e troca da própria arma.
- Mira de 48× manteve a distância de renderização do cliente e a distância de visão do servidor. Foram comparadas as configurações, sem segunda conexão.
- Recuo forte e saída da mira da sniper; disparos sem mirar têm dispersão maior. F5, postura deitada, bipé e teto bloqueando levantar verificados.
- SMG abriu painel de quatro espaços, recusou bipé, conservou módulos e recarregou exatamente 30 cartuchos consumindo a reserva. Recarga parcial verificada.
- Rajada real da SMG: aproximadamente 10 tiros/s, consumo por tiro, dano repetido no alvo e manutenção da mira. Soltar parou a rajada; troca de arma cancelou a sessão. O teste automatizou sinais de gatilho no servidor, sem manter mouse físico pressionado.
- Durante quadros reais com mira da sniper/SMG ativa, somente a camada da arma foi desenhada. Vida, fome, sede, hotbar e demais camadas não foram desenhadas.
- Capturas revisadas: pintura, ícones, duas mãos, mira, painel, recuo, disparo e terceira pessoa. Corrigidos aro duplicado na SMG e carregador deslocado no painel.
- Áudio original mais encorpado: RMS da sniper 0.1171; silenciada 0.0523, mantendo o transiente. Volume e alcance de propagação aumentados. Som carregado em saída virtual, sem avaliação em alto-falantes.
- Regressões: revólver, chips/painéis em várias orientações e DMT sem voo preso em mobs.

O JAR contém exatamente as 264 classes de produção e os 3005 recursos da execução final. Hashes conferidos após empacotar; classes de teste removidas. Evidências em tests/evidence_0.18.0.

## Limites

Não foi reproduzido o modpack completo do usuário, shaders, Embeddium ou dois clientes em servidor dedicado. Chefes de mods que não se classificam em c:bosses precisam entrar em mareeletrica:sniper_base_damage via datapack. Armadura, imunidades e regras próprias dos mobs continuam valendo. Vidros de outros mods que usam as tags comuns são reconhecidos; nem todos os blocos externos foram testados.

SHA-256: `5f6290d3e975404dee73c9bcbe6e5840aacb127c8fa9d5609eb4421dfc90de9d`
