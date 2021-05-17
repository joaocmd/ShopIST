import express from "express"
import BeaconService from "../../services/BeaconService"

const handler = (req: express.Request, res: express.Response) => {
    try {
        const body = req.body
        const result = BeaconService.getTimeEstimates(body.stores)
        res.send(result)
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/estimates/'
}

export default {
    handler,
    config
}