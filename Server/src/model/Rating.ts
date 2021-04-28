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
        const ratings: any = { 1: 0, 2: 0, 3: 0, 4: 0, 5: 0 }
        Object.values(this.ratings).forEach(val => {
            ratings[val] += 1
        });
        return { ratings, personalRating: this.ratings[userId] ?? null }
    }
}

export type RatingResponse = {
    ratings: any,
    personalRating: number | null
}