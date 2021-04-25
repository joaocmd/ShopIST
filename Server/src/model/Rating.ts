export default class {
    ratings: Record<string, number> = {}

    submitRating(val: number, userId: string) {
        if (val < 0 || val > 5) {
            throw 'bad-rating'
        }
        if (val === 0 && this.ratings[userId]) {
            delete this.ratings[userId]
        } else if (val !== 0) {
            this.ratings[userId] = val
        }
    }

    getRating(userId: string): RatingResponse {
        const ratings = Object.values(this.ratings)
        let rating = null
        if (ratings.length > 0) {
            rating = ratings.reduce((a, b) => a + b) / ratings.length
        }
        return { rating, personalRating: this.ratings[userId] ?? null }
    }
}

export type RatingResponse = {
    rating: number | null,
    personalRating: number | null
}