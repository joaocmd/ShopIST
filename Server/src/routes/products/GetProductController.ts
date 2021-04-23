import express from "express"
import { ProductService } from "../../services/ProductService"


const handler = (req: express.Request, res: express.Response) => {
    try {
        let product = ProductService.get(req.params.id)
        res.status(200).send(product)
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'GET',
    path: '/:id'
}

export default {
    handler,
    config
}