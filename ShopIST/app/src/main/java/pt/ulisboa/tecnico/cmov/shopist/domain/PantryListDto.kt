package pt.ulisboa.tecnico.cmov.shopist.domain

import java.util.*

data class PantryListDto(val uuid: UUID, val title: String, val items: MutableList<ItemDto>) {

    constructor(p: PantryList) : this(
        p.uuid,
        p.title,
        p.items.map { i -> ItemDto(i) }.toMutableList()
    )
}