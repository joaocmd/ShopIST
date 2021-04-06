export default class {
    constructor(id, name, stores) {
        this.uuid = id
        this.name = name
        this.stores = stores
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
        return this.uuid
    }

    toObject() {
        return {
            id: this.uuid,
            name: this.name,
			stores: this.stores,
            barcode: this.barcode,
            images: this.images
        }
    }
}