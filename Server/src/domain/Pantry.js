const pantries = []

export default class Pantry {
    static get() {
        return pantries
    }

    static add() {
        pantries.push(pantries.length)
    }
}