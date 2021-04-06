import Pantry from '@/model/Pantry'
import Product from '@/domain/Product'
import Store from '@/domain/Store'

var pantries = {}

export default class {

    static create(list, products, stores) {
        // if (pantries[id]) {
        //     throw 'pantry-already-exists'
        // }
        const pantry = new Pantry(list.uuid, list.name, list.location, list.items)
        products.forEach((p) => {
            Product.create(p)
        })
		
        stores.forEach((s) => {
            Store.create(s)
        })
		
        pantries[list.uuid] = pantry
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
        return pantries[id].toObject()
    }
}