import express from "express"
import { ImageProductService } from "../../services/ImageProductService"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let image = ImageProductService.getImage(req.params.id)
        res.status(200).send({ image })
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
