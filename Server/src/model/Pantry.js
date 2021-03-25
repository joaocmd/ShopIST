export default class {

    constructor(id, name) {
        this.id = id
        this.name = name
        this.items = {}
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
}