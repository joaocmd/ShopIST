import Beacon from '../model/Beacon'

const beacons: Record<string, Beacon> = {}

export default class BeaconService {

    static addBeacon(beacon: Beacon) {
        beacons[beacon.uuid] = new Beacon(beacon.uuid, beacon.location)
    }

    static removeBeacon(uuid: string) {
        delete beacons[uuid]
    }

    static addPersonToBeacon(token: string, uuid: string) {
        beacons[uuid].addPerson(token)
    }

    static removePersonFromBeacon(token: string, uuid: string) {
        beacons[uuid].removePerson(token)
    }

    static getBeacons() {
        return Object.values(beacons)
    }
}