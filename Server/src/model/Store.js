export default class {

    constructor(uuid, name, location) {
        this.uuid = uuid
        this.name = name
        this.location = location
    }

    addProduct(uuid, product) {
    }

    toObject() {
        return {
            uuid: this.uuid,
            name: this.name,
            location: this.location,
        }
    }
}