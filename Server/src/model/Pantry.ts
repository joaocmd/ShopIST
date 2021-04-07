import Item from "./Item"

export default class {
	uuid: string
	name: string
	location: string
	items: Record<string, Item>

    constructor(uuid: string, name: string, location: string, items: Record<string, Item>) {
        this.uuid = uuid
        this.name = name
        this.location = location
        this.items = items
    }

    addItem(id: string, item: Item) {
        if (this.items[item.productUUID]) {
            throw 'item-already-present'
        }
        this.items[id] = item
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