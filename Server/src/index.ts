import { createServer } from "http"
import express, { Application } from 'express'
import morgan from 'morgan'
import fs from 'fs'
import WebSocket from 'ws'

const app = express()
const port = 3000
const ALIVE_TIMEOUT = 10000

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

const server = createServer(app)

const wss = new WebSocket.Server({ server })
wss.on('connection', (ws: WebSocket) => {
	(ws as any).isAlive = true

	ws.on('pong', () => {
		(ws as any).isAlive = true
	})

	ws.on('message', (message: string) => {

		const broadcastRegex = /^broadcast\:/
		if (broadcastRegex.test(message)) {
			message = message.replace(broadcastRegex, '')

			wss.clients
				.forEach(client => {
					if (client != ws) {
						client.send(`Hello, broadcast message -> ${message}`)
					} else {
						client.send(`Sent message -> ${message}`)
					}
				})
		} else {
			ws.send(`Hello, you sent -> ${message}`)
		}
	})

	//send immediatly a feedback to the incoming connection    
	ws.send('You\'re connected!')
})

const interval = setInterval(() => {
	wss.clients.forEach((ws) => {
		
		if (!(ws as any).isAlive) return ws.terminate();
		
		(ws as any).isAlive = false
		ws.ping(null, false, () => {})
	})
}, ALIVE_TIMEOUT)

wss.on('close', () => {
	clearInterval(interval);
})

server.listen(port, () => console.log(`Listening on port ${port}`))

export default app