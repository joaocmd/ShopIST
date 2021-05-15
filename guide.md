# Checkpoint

## Notas

Joca tens de apagar o raio do croissant e do boneco para não ser duplicado

- Price não está a ser guardado no servidor

- Carregar num item para passar para compras
- Search tool with barcode
- Colocar o número do carrinho logo igual ao da needing
- Subtrair assim que se coloca no carrinho
- O carrinho tem de ser visível por todos
- Alterar ícone de quantidade necessária
- Localização mais rápido com low accuracy
- Fazer proxy no servidor para aceder à api do google e bing
- Fazer cache das traduções no servidor
- Rating 0 estrelas?
- Cache na pasta certa
- Fazer preloading da cache quando se tem WiFi


## Guião

Passo 1:
- Criamos uma pantry: Party's Pantry
	- Definir localização atual
	- Reentrar para demonstrar que a localização é usado para abrir a pantry
- Adicionar Boneco (2) e Croissant (1) -> Croissant para o Bom Dia
- Criar um produto: Filipinos (Bom Dia)
- Adicionar barcode
- Adicionar Filipinos, 2 em casa.
- Alterar quantidade que se quer de Filipinos para 5, Boneco para 2, Croissant para 4

Passo 2:
- Vamos à shopping lists, verificamos que no Bom Dia está lá, mas que no Pingo Doce não
- Adicionar ao carrinho 1 Filipinos
- Adicionar ao carrinho Croissant de modo a que não preencha  ()
- Corrigir as quantidades do Croissant de modo a preencher as duas pantries
- Adicionar 1 Boneco
- Fazer a cena do beacon
	- Aproximar do beacon
	- Afastar do beacon
- Fazer checkout
- Verificar que pantry aumentou de quantidade de Filipinos e Croissant

Passo 3:
- Share da lista
- João entra ->
	- Abre pantry
	- Muda quantidade de filipinos
	- Verificar checkout time do Bom Dia

- Adicionar uma imagem aos filipinos
	- João verifica
- Adicionar preço (1.80) no Bom Dia
	- João verifica

- **TODO:** Testar route time em movimento
	- Colocar dispositivo em movimento e verificar que os números mudam :)

- **TODO:** Testar cache
	- Carregar bué imagens

- Mostrar wireshark com SSL

- Ratings:
	- Adicionar um rating aos Filipinos (5*)
	- João verifica e adiciona 4*
	- Verifico e o João que dá 4.5*

- Social Sharing:
	- Partilhar filipinos no facebook (Fake)

- Localization:
	- Mudar de língua no sistema para Inglês e verificar a tradução

- Smart Sorting
	- João verificar ordem nova (filipinos, boneco, croissant)


# Termite Commands

`cd D:\dani_\Documents\GitHub\CMov\lab04\Termite-Cli`
`.\termite.bat`
`load D:\dani_\Documents\GitHub\CMov-Project\Termite-scripts\checkpoint_first_checkout.termite`