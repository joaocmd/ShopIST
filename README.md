# ShopIST

## Setup

### Requirements

For backend (situated at [/Server](/Server)):
- Node.js >= v14.16.1 (also works pretty well with >= v12.21.0)

For frontend (situated at [/ShopIST](/ShopIST)):
- Android SDK >= 24
- Gradle >= 6.5

### How to run

Backend:
1. Go to folder [`Server/`](Server/)
2. Copy the file [`.env.example`](Server/.env.example), name it [`.env`](Server/.env), and insert your Google Translation API key and Bing Maps API key.
3. Insert your keys (private and public) in [/keys/](/keys) (or change the path in [`index.ts`](Server/src/index.ts) on lines 42 and 43 for the private and public keys, accordingly)
4. Run `npm i --also=dev`
5. Run `npx ts-node src/index.ts` (or `npm start`)

Frontend:
1. Go to folder [`ShopIST/`](ShopIST/)
2. Copy the file [`google_maps_api_example.xml`](ShopIST/app/src/main/res/values/google_maps_api_example.xml), name it [`google_maps_api.xml`](ShopIST/app/src/main/res/values/google_maps_api.xml), and insert your Google Maps API key.
3. Change the api_base_url in [`api.xml`](ShopIST/app/src/main/res/values/api.xml) to your URL.
4. When ran on Android Studio, just hit the button `Run`.
