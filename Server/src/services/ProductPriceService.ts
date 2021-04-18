import ProductPrice from "../model/ProductPrice";
import Location from "../model/Location"

const products: Record<string, ProductPrice[]> = {}

// TODO: Set this as global for every distance calculation
const MAX_DISTANCE = 100

export default class ProductPriceService {

	static addProduct(barcode: string, location: Location, price: Number) {
		let oldPrice = this.getProductPrice(barcode, location)
		if (oldPrice !== null) {
			oldPrice.price = price
		} else {
			products[barcode].push(new ProductPrice(location, price))
		}
    }

	static getProductPrices(barcode: string) {
        return Object.values(products[barcode])
    }

	static getProductPrice(barcode: string, location: Location): ProductPrice | null {
		const prices = ProductPriceService.getProductPrices(barcode)
            .map(product => ({ product: product, distance: product.location.getDistance(location) }))
            .filter(b => b.distance <= MAX_DISTANCE)
            .sort((a, b) => a.distance - b.distance)

		if (prices.length > 0) {
			return prices[0].product
		} else {
			return null
		}
	}

	static getProductsPrice(barcodes: string[], location: Location): Record<string, Number> {
		let res: Record<string, Number> = {}
		barcodes.forEach( (barcode) => {
			let price = this.getProductPrice(barcode, location)
			if (price !== null) {
				res[barcode] = price.price
			}
		})
		return res
	}

	static removeProduct(barcode: string) {
        delete products[barcode]
    }

	static removeProductPrice(barcode: string, location: Location) {
        delete products[barcode]
    }

}