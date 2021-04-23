export default class {
    ratings: Record<string, number> = {}

    submitRating(val: number, voterId: string) {
        if (val <= 0 || val > 5) {
            throw 'bad-rating'
        }
        this.ratings[voterId] = val
    }

    getRating() {
        const ratings = Object.values(this.ratings)
        return ratings.reduce((a, b) => a + b) / ratings.length
    }
}