import Rating from '../model/Rating'

const ratings: Record<string, Rating> = {}

export default class {

    static submitRating(barcode: string, rating: number, userId: string) {
        if (!ratings[barcode]) {
            ratings[barcode] = new Rating()
        }
        ratings[barcode].submitRating(rating, userId)
    }

    static getRating(barcode: string): number | undefined {
        return ratings[barcode]?.getRating()
    }

    static getRatings(barcodes: string[]) {
        return barcodes.reduce((acc: Record<string, number>, barcode) => {
            const rating = this.getRating(barcode)
            if (rating !== undefined) {
                acc[barcode] = rating
            }
            return acc
        }, {})
    }
}