// import Item from '@/domain/Item'
import express from "express"

const handler = (req: express.Request, res: express.Response) => {
    try {
        // res.send(Item.get(req.params.id))
    } catch (error) {
        // res.status(400).send({ status: 400, error })
    }
	res.status(200).send({ status: 200 })
}

const config = {
    method: 'GET',
    path: '/:id'
}

export default {
    handler,
    config
}