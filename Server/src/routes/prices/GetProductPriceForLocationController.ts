import express from "express"
import ProductPriceService from "../../services/ProductPriceService"
import Location from "../../model/Location"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body as PriceRequestByLocation
        let price = ProductPriceService.getProductsPrice(body.barcodes, body.location)
		if (price === null) {
			res.status(400).send({ status: 400 })
		} else {
			res.status(200).send(price)
		}
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/location/'
}

export default {
    handler,
    config
}

type PriceRequestByLocation = {
	barcodes: string[],
	location: Location
}