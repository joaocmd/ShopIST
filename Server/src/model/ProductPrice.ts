import Location from "../model/Location"

export default class ProductPrice {
	location: Location
	price: Number

	constructor(location: Location, price: Number) {
		this.location = location
		this.price = price
	}
}