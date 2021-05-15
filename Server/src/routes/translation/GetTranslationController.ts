import express from "express"
import TranslationService from "../../services/TranslationService"

const handler = async (req: express.Request, res: express.Response) => {
    try {
        let params = req.query
        let text = await TranslationService.getTranslation(params.source as string, params.target as string, params.q as string)
        res.status(200).send(text)
    } catch (error) {
        res.status(400).send({ status: 400, error })
    }
}

const config = {
    method: 'GET',
    path: ''
}

export default {
    handler,
    config
}
