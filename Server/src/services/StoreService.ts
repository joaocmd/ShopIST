import Store from '../model/Store'
import BeaconService from './BeaconService'

const stores: Record<string, Store> = {}

const MAX_DISTANCE = 100

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

    static getTimeEstimate(uuid: string): number | null {
        const store = stores[uuid]
        if (!store || !store.location) {
            return null
        }

        const beacon = BeaconService.getBeacons()
            .map(beacon => ({ beacon, distance: beacon.location.getDistance(store.location) }))
            .filter(b => b.distance <= MAX_DISTANCE)
            .sort((a, b) => a.distance - b.distance)[0]

        return (!!beacon) ? beacon.beacon.estimateCurrentTime() : null
    }

    static getTimeEstimates(uuids: string[]): Record<string, number> {
        return uuids.reduce((acc: Record<string, number>, uuid) => {
            const estimate = this.getTimeEstimate(uuid)
            if (estimate) {
                acc[uuid] = estimate
            }
            return acc
        }, {})
    }
}