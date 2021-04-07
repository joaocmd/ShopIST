// import Item from '../../services/Item'
import express from "express"

const handler = (req: express.Request, res: express.Response) => {
    try {
        // Item.create(req.params.id, req.body.name)
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