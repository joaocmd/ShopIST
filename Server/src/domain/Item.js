import Item from '@/model/Item'
import StoreItem from '@/domain/StoreItem'

var items = {}

export default class {
    static create(id, name) {
        if (items[id]) {
            throw 'item-already-exists'
        }
        items[id] = new Item(id, name)
    }

    static addImage(id, blob) {
        if (!items[id]) {
            throw 'no-such-item'
        }
        items[id].addImage(blob)
        if (items[id].barcode) {
            StoreItem.addImage(items[id].barcode, blob)
        }
    }

    static setBarcode(id, barcode) {
        if (!items[id]) {
            throw 'no-such-item'
        }
        items[id].setBarcode(barcode)
        StoreItem.barcodeSet(items[id], barcode)
    }

    static get(id) {
        if (!items[id]) {
            throw 'no-such-item'
        }
        return items[id]
    }
}