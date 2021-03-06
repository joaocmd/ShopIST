import express from "express"
import { ProductService } from "../../services/ProductService"


const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body
        ProductService.create(body)
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