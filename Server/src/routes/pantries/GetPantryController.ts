import express from "express"
import { PantryService } from "../../services/PantryService"
import { ProductService } from "../../services/ProductService"
import { StoreService } from "../../services/StoreService"


const handler = (req: express.Request, res: express.Response) => {
    try {
        const pantry = PantryService.get(req.params.id)
        const products = Object.values(pantry.items).map(it => ProductService.get(it.productUUID))
		const storeIds = Array.from(new Set(([] as string[]).concat(...products.map(p => p.stores))))
        const stores = storeIds.map(id => StoreService.get(id))
        res.send({ pantry, products, stores })
    } catch (error) {
		// TODO: Verify if not found
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