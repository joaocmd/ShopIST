package pt.ulisboa.tecnico.cmov.shopist.domain.ratings

import java.util.*

class GetProductRatingDto(val barcode: String, val userId: UUID)
class GetProductRatingResponseDto(val ratings: HashMap<Int, Int>, val personalRating: Int?)

class SubmitProductRatingDto(val barcode: String, val userId: UUID, val rating: Int?)
