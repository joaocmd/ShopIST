import express, { Application } from 'express'
import morgan from 'morgan'
import fs from 'fs'

const app = express()
const port = 3000

function registerEndpoints(app: Application, prefix = __dirname + '/routes') {
	const registerAux = (currPath = '') => {
		const files = fs.readdirSync(prefix + currPath)
		files.forEach(file => {
			const fullPath = prefix + currPath + '/' + file
			if (fs.lstatSync(fullPath).isDirectory()) {
				registerAux(currPath + '/' + file)
			} else {
				const { handler, config } = require(fullPath).default
				const endpoint = currPath + (config.path ?? '')
				console.log(`Registering endpoint: ${config.method.toUpperCase()} ${endpoint}`)
				// @ts-ignore
				app[config.method.toLowerCase() as string](currPath + config.path, handler)
			}
		})
	}
	registerAux()
}

app.use(morgan('tiny'))
app.use(express.json())
registerEndpoints(app)
app.listen(port, () => {
    console.log(`Listening on port ${port}`)
})

export default app