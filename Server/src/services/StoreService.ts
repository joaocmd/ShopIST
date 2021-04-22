import Store from '../model/Store'
import BeaconService from './BeaconService'

const stores: Record<string, Store> = {}

export class StoreService {

    static create(store: Store) {
        stores[store.uuid] = new Store(store.uuid, store.name, store.location)
    }

    static get(uuid: string) {
        if (!stores[uuid]) {
            throw 'no-such-store'
        }
        return stores[uuid].toObject()
    }
}