import express from "express"
import { PantryService } from "../../services/PantryService"


const handler = (req: express.Request, res: express.Response) => {
    try {
		let body = req.body
        PantryService.create(body.pantry, body.products, body.stores)
        res.status(200).send({ status: 200 })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/:id'
}

export default {
    handler,
    config
}