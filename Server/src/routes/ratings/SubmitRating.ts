import express from "express"
import ProductRatingService from "../../services/ProductRatingService"

const handler = (req: express.Request, res: express.Response) => {
    try {
        const body = req.body
        ProductRatingService.submitRating(body.barcode, body.rating, body.userId)
        res.status(200).send({ status: 200 })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/submit/'
}

export default {
    handler,
    config
}
