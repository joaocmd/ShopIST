import Location from './Location'
import regression, { DataPoint } from 'regression'

type EnterRecord = {
    itemsAhead: number,
    enterTime: Date
}

type RegressionData = {
    itemsAhead: number,
    timeTaken: number
}

export default class {
    uuid: string
    location: Location

    #currentPeople: Record<string, EnterRecord> = {}
    #regressionData: RegressionData[] = []

    constructor(uuid: string, location: Location) {
        this.uuid = uuid
        this.location = location
    }

    addPerson(token: string, nrItems: number) {
        if (!!this.#currentPeople[token]) {
            throw 'person-already-in-beacon'
        }
        if (nrItems <= 0) {
            throw 'no-items'
        }
        const record: EnterRecord = { itemsAhead: this.itemsInLine() + nrItems, enterTime: new Date() }
        this.#currentPeople[token] = record
    }

    removePerson(token: string) {
        if (!this.#currentPeople[token]) {
            throw 'person-not-in-beacon'
        }
        const entrance = this.#currentPeople[token]
        delete this.#currentPeople[token]
        const record: RegressionData = { itemsAhead: entrance.itemsAhead, timeTaken: new Date().getTime() - entrance.enterTime.getTime() }
        // only account for valid cases (person might leave the queue because she forgot an item)
        if (record.timeTaken > 10000) {
            this.#regressionData.push(record)
        }
    }

    estimateCurrentTime(): number | null {
        // converts { peopleAhead: x, timeTaken: y} to [x, y]
        // TODO: Check if it is calculating well
        const formattedData = this.#regressionData.map(r => [r.itemsAhead, r.timeTaken] as DataPoint)

        const result = regression.linear(formattedData)
        if (result.equation[1] == NaN) {
            return null
        } else {
            // TODO: Verify with teacher if we need to divide by two
            return result.predict(this.itemsInLine())[1]
        }
    }

    itemsInLine(): number {
        return Object.values(this.#currentPeople).map(r => r.itemsAhead).reduce((acc, val) => acc + val, 0)
    }
}