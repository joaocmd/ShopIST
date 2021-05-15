import fetch from 'cross-fetch'

const TRANSLATION_API_KEY = process.env.TRANSLATION_API_KEY
const TRANSLATION_API_URL = "https://translation.googleapis.com/language/translate/v2"

const isTranslationAvailable = true
const translations: Record<string, Record<string, Record<string, string>>> = {}
/* Example:
	translations = {
		"pt": {
			"en": {
				"Arroz Doce": "Sweet Rice"
			}
		},
		"en": {
			"pt": {
				"Sweet Rice": "Arroz Doce"
			}
		}
	}
*/

export default class TranslationService {
	static async getTranslation(originLang: string, targetLang: string, originText: string): Promise<string> {
		originText = this.titlecase(originText)
		let localTranslation = this.getLocalTranslation(originLang, targetLang, originText)
		if (localTranslation != null) {
			return localTranslation
		} else {
			let googleTranslation = await this.getGoogleTranslation(originLang, targetLang, originText)
			this.storeTranslation(originLang, targetLang, originText, googleTranslation)
			return this.titlecase(googleTranslation)
		}
	}

	private static getLocalTranslation(originLang: string, targetLang: string, originText: string): string | null {
		if (translations[originLang] !== undefined) {
			let originLangDic = translations[originLang]

			if (originLangDic[targetLang] !== undefined) {
				let targetLangDic = originLangDic[targetLang]

				if (targetLangDic[originText] !== undefined) {
					return targetLangDic[originText]
				}
			}
		}
		return null
	}

	private static async getGoogleTranslation(originLang: string, targetLang: string, originText: string): Promise<string> {
		if (!isTranslationAvailable) {
			return originText
		}

		let response = await fetch(`${TRANSLATION_API_URL}?q=${originText}&source=${originLang}&target=${targetLang}&key=${TRANSLATION_API_KEY}`)
		let json = await response.json()
		let targetText = this.titlecase(json.data.translations[0].translatedText)
		console.log(`GET google/translation - ${originText} -> ${targetText}` )
		return targetText
	}

	private static storeTranslation(originLang: string, targetLang: string, originText: string, targetText: string) {
		if (translations[originLang] == undefined) {
			translations[originLang] = {}
			translations[originLang][targetLang] = {}
		}
		if (translations[targetLang] == undefined) {
			translations[targetLang] = {}
			translations[targetLang][originLang] = {}
		}

		translations[originLang][targetLang][originText] = targetText
		translations[targetLang][originLang][targetText] = originText
	}

	static titlecase(text: string): string {
		return text
			.toLowerCase()
			.split(' ')
			.map(word => word.charAt(0).toUpperCase() + word.slice(1))
			.join(' ')
	}
}