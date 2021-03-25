import Pantry from '@/model/Pantry'
import Item from '@/domain/Item'

var pantries = {}

export default class {

    static create(id, name) {
        if (pantries[id]) {
            throw 'pantry-already-exists'
        }
        pantries[id] = new Pantry(id, name)
    }

    static addItem(id, itemId) {
        if (!pantries[id]) {
            throw 'no-such-pantry'
        }
        const item = Item.get(itemId)
        pantries[id].addItem(item)
    }

    static get(id) {
        if (!pantries[id]) {
            throw 'no-such-pantry'
        }
        return pantries[id]
    }
}