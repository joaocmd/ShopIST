import express from "express"
import { PantryService } from "../../services/PantryService"


const handler = (req: express.Request, res: express.Response) => {
    PantryService.addItem(req.params.id, req.params.itemId)
    res.status(200).send({ status: 200 })
}

const config = {
    method: 'POST',
    path: '/:id/items/:itemId'
}

export default {
    handler,
    config
}