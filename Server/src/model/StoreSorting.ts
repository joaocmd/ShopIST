import Location from './Location'

export default class {
    location: Location
    mat: Record<string, Record<string, number>> = {}

    constructor(location: Location) {
        this.location = location
    }

    private fillNew(barcodes: string[]) {
        for (const newBarcode of barcodes) {
            if (this.mat[newBarcode]) {
                // if the barcode already exists in the matrix
                continue
            }
            // fill old entries with 0
            for (const barcode of Object.keys(this.mat)) {
                this.mat[barcode][newBarcode] = 0
            }
            // create new row with all in zeroes
            this.mat[newBarcode] = {}
            for (const barcode of Object.keys(this.mat)) {
                this.mat[newBarcode][barcode] = 0
            }
        }
    }

    submitOrder(barcodes: string[]) {
        this.fillNew(barcodes)

        // FIXME: why can't I use for i in barcodes?
        for (let i = 0; i < barcodes.length; i++) {
            for (let j = i + 1; j < barcodes.length; j++) {
                const [a, b] = [barcodes[i], barcodes[j]]
                this.mat[a][b] += 1
            }
        }
    }

    /**
     * @param itemIds { uuid: barcode }
     */
    getSorted(barcodes: string[]): string[] {
        this.fillNew(barcodes)

        // Can't use regular sort because this relation is not transitive
        // FIXME: why can't I use for i in barcodes?
        for (let i = 0; i < barcodes.length; i++) {
            for (let j = i + 1; j < barcodes.length; j++) {
                const a = barcodes[i]
                const b = barcodes[j]
                if (this.mat[a][b] < this.mat[b][a]) {
                    const aux = barcodes[i]
                    barcodes[i] = b
                    barcodes[j] = aux
                } else if (this.mat[b][a] === this.mat[a][b]) {
                    if (a.localeCompare(b) > 0) {
                        const aux = barcodes[i]
                        barcodes[i] = b
                        barcodes[j] = aux
                    }
                }
            }
        }
        return barcodes
    }
}