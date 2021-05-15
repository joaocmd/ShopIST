import express from 'express'
import DrivingTimeService from '../../services/DrivingTimeService'

const handler = async (req: express.Request, res: express.Response) => {
    try {
        let params = req.query
        let text = await DrivingTimeService.getDrivingTime(params.orig as string, params.dest as string)
        res.status(200).send({ duration: text })
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'GET',
    path: '/'
}

export default {
    handler,
    config
}
