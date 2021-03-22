package pt.ulisboa.tecnico.cmov.shopist.domain

class PantryList {
    var title: String = ""
    // TODO: Get a location
    var location: String = ""
    // FIXME:
    var products: MutableList<PantryList> = mutableListOf()
}