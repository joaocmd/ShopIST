import Pantry from '@/domain/Pantry'

const handler = (req, res) => {
    Pantry.add()
    res.status(200).send()
}

const config = {
    method: 'POST',
    path: ''
}

export default {
    handler,
    config
}