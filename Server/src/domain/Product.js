import Product from '@/model/Product'

var products = {}

export default class {
    static create(product) {
        products[product.uuid] = new Product(product.uuid, product.name, product.stores)
    }

    static addImage(id, blob) {
        if (!products[id]) {
            throw 'no-such-product'
        }
        products[id].addImage(blob)
        if (products[id].barcode) {
            StoreItem.addImage(products[id].barcode, blob)
        }
    }

    static setBarcode(id, barcode) {
        if (!products[id]) {
            throw 'no-such-product'
        }
        products[id].setBarcode(barcode)
        StoreItem.barcodeSet(products[id], barcode)
    }

    static get(id) {
        if (!products[id]) {
            throw 'no-such-product'
        }
        return products[id].toObject()
    }
}