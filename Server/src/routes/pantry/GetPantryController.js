import Pantry from '@/domain/Pantry'

const handler = (req, res) => {
    res.send(Pantry.get())
}

const config = {
    method: 'GET',
    path: ''
}

export default {
    handler,
    config
}