import express from "express"
import ProductRatingService from "../../services/ProductRatingService"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body
        const ratings = ProductRatingService.getRating(body.barcode, body.userId)
        res.status(200).send(ratings)
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/'
}

export default {
    handler,
    config
}
