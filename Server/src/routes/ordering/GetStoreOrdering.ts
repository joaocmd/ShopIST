import express from "express"
import StoreSortService from "../../services/StoreSortService"
import Location from '../../model/Location'

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body as Request
        const order = StoreSortService.getOrder(new Location(body.location), body.order)
        if (order) {
            res.status(200).send(order)
        } else {
            res.status(400).send({ status: 400, error: 'store-not-found' })
        }
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/'
}

export default {
    handler,
    config
}

type Request = {
    order: string[],
    location: Location
}
