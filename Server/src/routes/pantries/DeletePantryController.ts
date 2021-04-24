import express from "express"
import { PantryService } from "../../services/PantryService"


const handler = (req: express.Request, res: express.Response) => {
    try {
        PantryService.delete(req.params.id)
        res.status(200).send({ status: 200 })
    } catch (error) {
		// TODO: Verify if not found
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'DELETE',
    path: '/:id'
}

export default {
    handler,
    config
}