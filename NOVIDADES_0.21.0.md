# Maré Elétrica 0.21.0 — Arsenal e recarga

Minecraft **1.21.1**, **NeoForge**, **Java 21**. O JAR pronto fica em `INSTALAR`; não precisa compilar para jogar. Troque o JAR antigo nos clientes e no servidor.

## Como usar

| Ação | Comando |
|---|---|
| Disparar | M1; segure nas armas automáticas |
| Mirar | Segurar M2 |
| Trocar o pente | R |
| Abrir a oficina da arma | J, ou Shift + M2 em pé |
| Abastecer um pente | Segure o pente e aperte M2, com os cartuchos correspondentes no inventário |
| Deitar ou levantar | Shift duas vezes |

Os comandos R e J podem ser alterados nas configurações de controles.

Cada arma usa seu próprio pente. Coloque um pente abastecido no espaço **Pente** da oficina ou no inventário; R inicia a troca. A munição entra na arma ao terminar a animação. O pente retirado conserva as balas restantes e volta para a reserva, inventário ou chão se não houver espaço. Trocar de arma ou abrir uma tela interrompe a troca e devolve o pente que seria inserido.

| Arma | Capacidade | Cartucho para abastecer |
|---|---:|---|
| Desert Eagle | 7 | Cartucho da Desert Eagle (antigo cartucho de revólver) |
| USP | 12 | Cartucho USP |
| M4A1 | 30 | Cartucho de fuzil |
| AK-47 | 30 | Cartucho de fuzil |
| M870 | 6 | Cartucho de escopeta |
| MP5 | 30 | Cartucho de SMG |
| SMG Vetor | 30 | Cartucho de SMG |
| Sniper | 5 | Cartucho de precisão |

A M870 desta versão do Minecraft usa um carregador destacável para acompanhar a nova recarga. Mesmo quando duas armas usam o mesmo cartucho, seus pentes e acessórios são diferentes.

## Aparência e acessórios

- Oito modelos com pintura por face, densidade de textura consistente e peças mecânicas separadas. A pintura preserva borrachas, parafusos e partes funcionais.
- Desert Eagle branca: empunhadura com uma mão e recuo forte. A outra mão entra durante a troca de pente. Inclui acabamentos dourado gravado e azul com detalhes dourados.
- Seis pinturas próprias por arma: **48 pinturas** na aba Camuflagem.
- Três miras e três silenciadores próprios por arma: **24 miras e 24 silenciadores**. A USP tem um supressor alongado. Os acessórios têm o nome da arma compatível.
- Sem acessório óptico, a mira usa somente a alça e a massa de mira do modelo. Com óptica, o ponto acompanha o movimento da arma, sem desenhar anéis fora da lente.
- As miras e as mãos acompanham o movimento ao andar; a recarga mostra a retirada e a inserção do pente.
- A coronha continua no modelo completo, com enquadramento próprio na visão em primeira pessoa.
- O silenciador abafa o disparo sem remover todo o som. A sniper mantém calibração, bipé e suas miras de longo alcance.

## Equilíbrio e atualização de mundos

A M870 passa de 2,5 para 4,5 de dano por projétil, mantendo oito projéteis por cartucho. SMG e MP5 têm mais dispersão, especialmente sem mirar ou andando. O recuo da Desert foi aumentado de 4,5 para 18 na configuração da arma.

As armas existentes mantêm os disparos carregados. Os cartuchos soltos guardados no painel antigo viram um pente; o excedente volta ao inventário. Acessórios já instalados são adaptados à família da arma. Para adaptar acessórios antigos guardados fora da arma, combine **um acessório antigo + um pente vazio da arma desejada** na mesa de trabalho. Esse processo consome o pente usado como adaptador.

As armas, pentes e acessórios funcionais ficam em **Itens de guerra**; pinturas e roupas ficam em **Camuflagem**. As máquinas de energia, veículos e DMT continuam no mod.

Punchy é opcional e não vem no pacote. A versão Punchy 2.8g para 1.21.1 exige NeoForge 21.1.229 ou superior. Sem Punchy, a base usada pelo projeto é NeoForge 21.1.172.
