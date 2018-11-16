package com.boyue.booyuedentifyimage.bean

/**
 * Created by Tianluhua on 2018\11\16 0016.
 *
 */
data class ResultResponseBean(var has_more: Boolean, var log_id: Long, var result_num: Int, var result: List<ResultBean>) {

    data class ResultBean(var score: Double, val brief: String, var cont_sign: String)
}