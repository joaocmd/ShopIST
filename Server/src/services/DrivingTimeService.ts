import fetch from 'cross-fetch'

const BING_MAPS_API_KEY = process.env.BING_MAPS_API_KEY
const BING_MAPS_API_URL = 'https://dev.virtualearth.net/REST/V1/Routes'

export default class {
	static async getDrivingTime(orig: any, dest: any): Promise<number | undefined> {
		const url = `${BING_MAPS_API_URL}/Driving?wp.0=${orig}&wp.1=${dest}&key=${BING_MAPS_API_KEY}`
		const json: any = await fetch(url).then(r => r.json())
		return json?.resourceSets?.[0]?.resources?.[0]?.travelDuration
	}
}