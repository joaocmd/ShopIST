// Crowd Sourced Item
export default class {
    constructor(barcode) {
        this.id = barcode
        this.images = []
    }

    addImage(blob) {
        this.images.push(blob)
    }

    hashString() {
        return this.id
    }
}