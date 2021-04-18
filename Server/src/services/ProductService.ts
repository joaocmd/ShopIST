import Product from '../model/Product'

const products: Record<string, Product> = {}

export class ProductService {
    static create(product: Product) {
        products[product.uuid] = new Product(product.uuid, product.name, product.stores)
    }

    // static addImage(id: string, blob: any) {
    //     if (!products[id]) {
    //         throw 'no-such-product'
    //     }
    //     products[id].addImage(blob)
    //     if (products[id].barcode) {
    //         StoreItemService.addImage(products[id].barcode, blob)
    //     }
    // }

    // static setBarcode(id: string, barcode: string) {
    //     if (!products[id]) {
    //         throw 'no-such-product'
    //     }
    //     products[id].setBarcode(barcode)
    //     StoreItemService.barcodeSet(products[id], barcode)
    // }

    static get(id: string) {
        if (!products[id]) {
            throw 'no-such-product'
        }
        return products[id].toObject()
    }
}