import Location from "./Location"
import Product from "./Product"

export default class {
    uuid: string
    name: string
    location: Location

    constructor(uuid: string, name: string, location: Location) {
        this.uuid = uuid
        this.name = name
        this.location = location
    }

    toObject() {
        return {
            uuid: this.uuid,
            name: this.name,
            location: this.location,
            isShared: true
        }
    }
}