import Store from '../model/Store'

var stores: Record<string, Store> = {}

export class StoreService {

    static create(store: Store) {
        stores[store.uuid] = new Store(store.uuid, store.name, store.location)
    }

    static get(id: string) {
        if (!stores[id]) {
            throw 'no-such-store'
        }
        return stores[id].toObject()
    }
}