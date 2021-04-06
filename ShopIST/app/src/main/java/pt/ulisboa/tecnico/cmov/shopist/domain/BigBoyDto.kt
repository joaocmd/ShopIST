package pt.ulisboa.tecnico.cmov.shopist.domain

class BigBoyDto(pantryList: PantryList) {
    var pantry: PantryListDto = PantryListDto(pantryList)
    var products: MutableList<ProductDto> = mutableListOf()
    var stores: List<StoreDto>

    init {
        val products = pantryList.items.map { i -> i.product }
        val setStores = mutableSetOf<Store>()
        products.forEach { p ->
            p.stores.forEach { s ->
                setStores.add(s)
            }
            this.products.add( ProductDto(p) )
        }
        this.stores = setStores.toList().map { s -> StoreDto(s) }
    }
}