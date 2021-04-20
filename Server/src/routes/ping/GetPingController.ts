import express from "express"

const handler = (req: express.Request, res: express.Response) => {
    res.status(200).send({status: 200})
}

const config = {
    method: 'GET',
    path: '/'
}

export default {
    handler,
    config
}
