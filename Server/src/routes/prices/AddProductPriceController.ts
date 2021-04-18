import express from "express"
import ProductPriceService from "../../services/ProductPriceService"
import Location from "../../model/Location"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body as AddPriceRequest
        ProductPriceService.addProduct(body.barcode, body.location, body.price)
        res.status(200).send({ status: 200 })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/add/'
}

export default {
    handler,
    config
}

type AddPriceRequest = {
	barcode: string,
	location: Location,
	price: number
}