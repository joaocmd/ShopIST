import Pantry from '@/domain/Pantry'
import Product from '@/domain/Product'
import Store from '@/domain/Store'

const handler = (req, res) => {
    try {
        const pantry = Pantry.get(req.params.id)
        const products = pantry.items.map(it => Product.get(it.productUUID))
        const stores = Array.from(new Set(products.map(p => p.stores).flat())).map(id => Store.get(id))
        res.send({ pantry, products, stores })
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