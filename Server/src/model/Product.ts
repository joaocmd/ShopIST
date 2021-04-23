import Store from "./Store"

export default class {
    uuid: string
    name: string
    stores: string[]
	barcode: string | null

    constructor(uuid: string, name: string, stores: string[], barcode: string | null) {
        this.uuid = uuid
        this.name = name
        this.stores = stores
		this.barcode = barcode
    }

    // setBarcode(barcode: string) {
    //     if (barcode) {
    //         throw 'barcode-already-set'
    //     }
    //     this.barcode = barcode
    // }

    // addImage(blob: any) {
    //     this.images.push(blob)
    // }

    // prependImages(blobs: any[]) {
    //     this.images = blobs.concat(this.images)
    // }

    hashString() {
        return this.uuid
    }

    toObject() {
        return {
            uuid: this.uuid,
            name: this.name,
            stores: this.stores,
            isShared: true,
			barcode: this.barcode
        }
    }
}