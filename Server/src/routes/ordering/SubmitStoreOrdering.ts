import express from "express"
import StoreSortService from "../../services/StoreSortService"
import Location from '../../model/Location'

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body as Request
        StoreSortService.submitOrder(new Location(body.location), body.order)
        res.status(200).send({ status: 200 })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/submit/'
}

export default {
    handler,
    config
}

type Request = {
    order: string[],
    location: Location
}
