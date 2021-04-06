export default class {

    constructor(id, name, location, items) {
        this.uuid = id
        this.name = name
        this.location = location
        this.items = items
    }

    addItem(id, item) {
        if (this.items[item.id]) {
            throw 'item-already-present'
        }
        this.items[id] = {
            name: item.name,
            pantry: 0,
            need: 0,
            cart: 0
        }
    }

    toObject() {
        return {
            uuid: this.uuid,
            name: this.name,
            location: this.location,
            items: this.items
        }
    }
}