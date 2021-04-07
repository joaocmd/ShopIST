import Location from './Location'
import regression, { DataPoint } from 'regression'

type EnterRecord = {
    peopleAhead: number,
    enterTime: Date
}

type RegressionData = {
    peopleAhead: number,
    timeTaken: number
}

export default class {
    uuid: string
    location: Location

    #currentPeople: Record<string, EnterRecord>
    #regressionData: RegressionData[]

    constructor(uuid: string, location: Location) {
        this.uuid = uuid
        this.location = location
        this.#currentPeople = {}
        this.#regressionData = []
    }

    addPerson(token: string) {
        if (!!this.#currentPeople[token]) {
            throw 'person-already-in-beacon'
        }
        const record: EnterRecord = { peopleAhead: Object.keys(this.#currentPeople).length, enterTime: new Date() }
        this.#currentPeople[token] = record
    }

    removePerson(token: string) {
        if (!this.#currentPeople[token]) {
            throw 'person-not-in-beacon'
        }
        const entrance = this.#currentPeople[token]
        delete this.#currentPeople[token]
        const record: RegressionData = { peopleAhead: entrance.peopleAhead, timeTaken: new Date().getTime() - entrance.enterTime.getTime() }
        this.#regressionData.push(record)
    }

    estimateCurrentTime() {
        const peopleInLine = Object.keys(this.#currentPeople).length
        // converts to { peopleAhead: x, timeTaken: y} => [x, y]
        const formattedData = this.#regressionData.map(r => [r.peopleAhead, r.timeTaken] as DataPoint)

        const result = regression.linear(formattedData)
        return result.predict(peopleInLine)[1]
    }
}