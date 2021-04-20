import express from "express"
import { ImageProductService } from "../../services/ImageProductService"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body
        let id = ImageProductService.create(req.params.id, body.image)
        res.status(200).send(id)
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/:id/add/'
}

export default {
    handler,
    config
}
