import Beacon from '../model/Beacon'
import Location from '../model/Location'

const beacons: Record<string, Beacon> = {}
beacons["ShopIST-001"] = new Beacon("ShopIST-001", new Location({ latitude: 38.73361076643277, longitude: -9.142712429165842 }))
beacons["ShopIST-002"] = new Beacon("ShopIST-002", new Location({ latitude: 38.73595121972168, longitude: -9.141665026545525 }))

// TODO: Set this as global for every distance calculation
const MAX_DISTANCE = 100

export default class BeaconService {

    static addBeacon(beacon: Beacon) {
        beacons[beacon.uuid] = new Beacon(beacon.uuid, beacon.location)
    }

    static removeBeacon(uuid: string) {
        delete beacons[uuid]
    }

    static addPersonToBeacon(token: string, nrItems: number, beaconUuid: string) {
        beacons[beaconUuid].addPerson(token, nrItems)
    }

    static removePersonFromBeacon(token: string, beaconUuid: string) {
        beacons[beaconUuid].removePerson(token)
    }

    static getBeacons() {
        return Object.values(beacons)
    }

    static getTimeEstimate(location: Location): number | null {
        return BeaconService.getBeacons()
            .map(beacon => ({ beacon, distance: beacon.location.getDistance(location) }))
            .filter(b => b.distance <= MAX_DISTANCE)
            .sort((a, b) => a.distance - b.distance)[0]?.beacon.estimateCurrentTime()
    }

    static getTimeEstimates(stores: Record<string, Location>): Record<string, number> {
        return Object.entries(stores).reduce((acc: Record<string, number>, store) => {
            const [uuid, location] = store
            const estimate = this.getTimeEstimate(location)
            if (estimate !== null) {
                acc[uuid] = estimate
            }
            return acc
        }, {})
    }
}