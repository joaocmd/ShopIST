import Pantry from '@/domain/Pantry'

const handler = (req, res) => {
    try {
        res.send(Pantry.get(req.params.id))
    } catch (error) {
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