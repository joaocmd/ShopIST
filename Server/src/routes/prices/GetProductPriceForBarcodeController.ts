import express from "express"
import ProductPriceService from "../../services/ProductPriceService"
import Location from "../../model/Location"

const handler = (req: express.Request, res: express.Response) => {
    try {
        let body = req.body as PriceRequestByBarcode
        let price = ProductPriceService.getProductPriceStore(body.barcode, body.locations)
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
    path: '/barcode/'
}

export default {
    handler,
    config
}

type PriceRequestByBarcode = {
	barcode: string,
	locations: Location[]
}