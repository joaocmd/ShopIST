export default class {
    constructor(id, name) {
        this.id = id
        this.name = name
        this.barcode = null
        this.images = []
    }

    setBarcode(barcode) {
        if (barcode) {
            throw 'barcode-already-set'
        }
        this.barcode = barcode
    }

    addImage(blob) {
        this.images.push(blob)
    }

    prependImages(blobs) {
        this.images = blobs.concat(this.images)
    }

    hashString() {
        return this.id
    }

    toObject() {
        return {
            id: this.id,
            name: this.name,
            barcode: this.barcode,
            images: this.images
        }
    }
}