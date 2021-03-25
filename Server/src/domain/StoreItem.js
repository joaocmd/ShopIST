import StoreItem from '@/model/StoreItem';

var items = {}

export default class {

    static barcodeSet(item, barcode) {
        // item already registered by someone
        if (items[barcode]) {
            item.prependImages(items[barcode].images)
        } else {
            items[barcode] = new StoreItem(barcode)
        }
    }

    static addImage(barcode, blob) {
        if (!items[barcode]) {
            throw 'no-such-store-item'
        }
        items[barcode].addImage(blob)
    }
}