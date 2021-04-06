import Pantry from '@/domain/Pantry'

const handler = (req, res) => {
    try {
		let body = req.body
        Pantry.create(body.pantry, body.products, body.stores)
        res.status(200).send({ status: 200 })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'POST',
    path: '/:id'
}

export default {
    handler,
    config
}