import { v4 as uuidv4 } from 'uuid'

type ProductImages = Record<string, string>
const images: ProductImages = {}
const productImages: Record<string, string[]> = {}

export class ImageProductService {
	static create(barcode: string, image: string, id: string): string {
		if (productImages[barcode] === undefined) {
			productImages[barcode] = []
		}
		productImages[barcode].push(id)
		images[id] = image
		return id
	}

	static generateId(): string {
		return uuidv4()
	}

	static getImage(id: string): string {
		if (images[id] !== undefined) {
			return images[id]
		}
		throw "Can't find image"
	}

	static getAllImages(barcode: string): string[] {
		if (productImages[barcode] !== undefined) {
			return productImages[barcode]
		}
		throw "Can't find product"
	}
}