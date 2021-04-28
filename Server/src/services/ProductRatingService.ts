import Rating, { RatingResponse } from '../model/Rating'

const ratings: Record<string, Rating> = {}

export default class {

    static submitRating(barcode: string, rating: number, userId: string) {
        if (!ratings[barcode]) {
            ratings[barcode] = new Rating()
        }
        ratings[barcode].submitRating(rating, userId)
    }

    static getRatings(barcode: string, userId: string): RatingResponse {
        if (!ratings[barcode]) {
            ratings[barcode] = new Rating()
        }
        return ratings[barcode].getRating(userId)
    }
}