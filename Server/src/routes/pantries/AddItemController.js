import Pantry from '@/domain/Pantry'

const handler = (req, res) => {
    Pantry.addItem(req.params.id, req.params.itemId)
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