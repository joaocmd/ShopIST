import Pantry from '../model/Pantry'
import Product from '../model/Product'
import Store from '../model/Store'
import { ProductService } from './ProductService'
import { StoreService } from './StoreService'

var pantries: Record<string, Pantry> = {}

export class PantryService {

    static create(list: Pantry, products: Product[], stores: Store[]) {
        // if (pantries[id]) {
        //     throw 'pantry-already-exists'
        // }
        const pantry = new Pantry(list.uuid, list.name, list.location, list.items)
        products.forEach((p) => {
            ProductService.create(p)
        })
		
        stores.forEach((s) => {
            StoreService.create(s)
        })
		
        pantries[list.uuid] = pantry
    }

    static addItem(id: string, itemId: string) {
        // if (!pantries[id]) {
        //     throw 'no-such-pantry'
        // }
        // const item = Item.get(itemId)
        // pantries[id].addItem(item)
    }

    static get(id: string) {
        if (!pantries[id]) {
            throw 'no-such-pantry'
        }
        return pantries[id].toObject()
    }
}