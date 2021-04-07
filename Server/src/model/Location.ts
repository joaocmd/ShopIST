import { getDistance as distanceBetween } from 'geolib'

export default class Location {
	latitude: number
	longitude: number

	constructor(coords: { latitude: number, longitude: number }) {
		this.latitude = coords.latitude
		this.longitude = coords.longitude
	}

	getDistance(other: Location) {
		return distanceBetween(
			{ latitude: this.latitude, longitude: this.longitude },
			{ latitude: other.latitude, longitude: other.longitude }
		)
	}
}