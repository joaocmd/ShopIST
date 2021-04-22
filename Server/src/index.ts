import { createServer } from "https"
import express, { Application } from 'express'
import morgan from 'morgan'
import fs from 'fs'
import bodyParser from 'body-parser'

const app = express()
const port = 3000

app.use(bodyParser.json({limit: '50mb'})); // support json encoded bodies
app.use(bodyParser.urlencoded({ limit: '50mb', extended: true })); // support encoded bodies

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

// HTTP version on port 3000
// app.listen(port, () => console.log(`Listening on port ${port}`))

const privateKey = fs.readFileSync( '../../private.pem')
const certificate = fs.readFileSync( '../../certificate.pem' )

createServer({
	key: privateKey,
	cert: certificate
}, app).listen(443, () => {
	console.log("Server started on port 443")
})

export default app