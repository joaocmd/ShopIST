import ProductPrice from "../model/ProductPrice";
import Location from "../model/Location"

const products: Record<string, ProductPrice[]> = {}

const MAX_DISTANCE = 100

export default class ProductPriceService {

	static addProduct(barcode: string, location: Location, price: Number) {
		let oldPrice = this.getProductPrice(barcode, location)
		if (oldPrice !== null) {
			oldPrice.price = price
		} else {
			if (products[barcode] == undefined) {
				products[barcode] = []	
			}
			products[barcode].push(new ProductPrice(new Location(location), price))
		}
	}

	static getProductPrices(barcode: string): ProductPrice[] | null {
		if (products[barcode] !== undefined) {
			return Object.values(products[barcode])
		} else {
			return null
		}
	}

	static getProductPrice(barcode: string, location: Location): ProductPrice | null {
		const previousPrices = ProductPriceService.getProductPrices(barcode)
		if (previousPrices === null) return null
		const prices = previousPrices
			.map(product => ({ product, distance: product.location.getDistance(location) }))
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
		barcodes.forEach((barcode) => {
			let price = this.getProductPrice(barcode, location)
			if (price !== null) {
				res[barcode] = price.price
			}
		})
		return res
	}

	static getProductPriceStore(barcode: string, locations: Location[]): ProductPrice[] {
		let res: ProductPrice[] = []
		locations.forEach((location) => {
			let price = this.getProductPrice(barcode, location)
			if (price !== null) {
				res.push(price)
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