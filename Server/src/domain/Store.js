import Store from '@/model/Store'

var stores = {}

export default class {

    static create(store) {
        stores[store.uuid] = new Store(store.uuid, store.name, store.location)
    }

    static get(id) {
        if (!stores[id]) {
            throw 'no-such-store'
        }
        return stores[id].toObject()
    }
}