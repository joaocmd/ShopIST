import express from "express"
import BeaconService from "../../services/BeaconService"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body
        BeaconService.addPersonToBeacon(body.token, body.nrItems, req.params.id)
        res.status(200).send({ status: 200 })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/enter/:id'
}

export default {
    handler,
    config
}