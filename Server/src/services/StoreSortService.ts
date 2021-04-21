import Sorting from '../model/StoreSorting'
import Location from '../model/Location'

const orders: Sorting[] = []

// TODO: Set this as global for every distance calculation
const MAX_DISTANCE = 100

export default class {

    static submitOrder(location: Location, order: string[]) {
        let sorting = this.findStore(location)
        if (!sorting) {
            sorting = new Sorting(location)
            orders.push(sorting)
        }

        sorting.submitOrder(order)
    }

    static getOrder(location: Location, barcodes: string[]): string[] | undefined {
        return this.findStore(location)?.getSorted(barcodes)
    }

    static findStore(location: Location): Sorting | undefined {
        return orders
            .map(sorting => ({ sorting, distance: sorting.location.getDistance(location) }))
            .filter(b => b.distance <= MAX_DISTANCE)
            .sort((a, b) => a.distance - b.distance)[0]?.sorting
    }
}