export default class {
	productUUID: string
	pantryQuantity: number
	needingQuantity: number
	cartQuantity: number

	constructor(uuid: string, pQ: number, nQ: number, cQ: number) {
		this.productUUID = uuid
		this.pantryQuantity = pQ
		this.needingQuantity = nQ
		this.cartQuantity = cQ
	}
}