import express from "express"
import { ImageProductService } from "../../services/ImageProductService"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let images = ImageProductService.getAllImages(req.params.id)
        res.status(200).send(images)
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'GET',
    path: '/:id/images'
}

export default {
    handler,
    config
}
