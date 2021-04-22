import express from "express"
import StoreSortService from "../../services/StoreSortService"


const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body
        const order = StoreSortService.getOrder(body.location, body.barcodes)
        if (order) {
            res.status(200).send({ order })
        } else {
            res.status(400).send({ status: 400, 'store-not-found' })
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