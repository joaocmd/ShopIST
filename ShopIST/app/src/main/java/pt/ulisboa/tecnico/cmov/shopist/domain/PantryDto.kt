package pt.ulisboa.tecnico.cmov.shopist.domain

data class PantryDto(val title: String, val items: MutableList<ItemDto>) {

    constructor(p: PantryList) : this(p.title,
            p.items.map { i -> ItemDto(i) }.toMutableList()) {
    }
}