import Item from '../model/Item';
import Product from '../model/Product';
import StoreItem from '../model/StoreItem';

var products: Record<string, Product> = {}

export class StoreItemService {

    static barcodeSet(product: Product, barcode: string) {
        // item already registered by someone
        if (products[barcode]) {
            // item.prependImages(items[barcode].images)
        } else {
            // items[barcode] = new StoreItem(barcode)
        }
    }

    static addImage(barcode: string, blob: any) {
        // if (!items[barcode]) {
        //     throw 'no-such-store-item'
        // }
        // items[barcode].addImage(blob)
    }
}