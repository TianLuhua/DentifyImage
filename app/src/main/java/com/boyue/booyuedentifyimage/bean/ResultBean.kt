package com.boyue.booyuedentifyimage.bean

/**
 * Created by Tianluhua on 2018\11\16 0016.
 *
 * {
 * "has_more": false,
 * "log_id": 2896827775265419631,
 * "result_num": 1,
 * "result":
 *          [{"score": 0.54658565460129,
 *          "brief": "火火火火兔",
 *          "cont_sign": "2657851221,2311767958"}]
 * }
 *
 */
data class ResultResponseBean(var has_more: Boolean, var log_id: Long, var result_num: Int, var result: List<ResultBean>) {

    data class ResultBean(var score: Double, val brief: String, var cont_sign: String)
}