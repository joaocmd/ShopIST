import express from "express"
import BeaconService from "../../services/BeaconService"

const handler = (req: express.Request, res: express.Response) => {
    try {
		let body = req.body
        BeaconService.removePersonFromBeacon(body.token, req.params.id)
        res.status(200).send({ status: 200 })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/leave/:id'
}

export default {
    handler,
    config
}