export default class {
	productUUID: string
	pantryQuantity: number
	needingQuantity: number
	cartQuantity: number
	opType: number

	constructor(uuid: string, pQ: number, nQ: number, cQ: number) {
		this.productUUID = uuid
		this.pantryQuantity = pQ
		this.needingQuantity = nQ
		this.cartQuantity = 0
		this.opType = 1
	}
}