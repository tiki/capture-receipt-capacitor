/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.sdk.capture.receipt.capacitor.rsp

import com.microblink.linking.Account
import com.mytiki.sdk.capture.receipt.capacitor.RetailerEnum
import org.json.JSONObject

class RspRetailerRmAccount(
    private val account: Account,
    private val isRemoved: Boolean
) : Rsp {
    override fun toJson(): JSONObject =
        JSONObject()
            .put("username", account.credentials.username())
            .put("retailer", RetailerEnum.fromValue(account.retailerId).toString())
            .put("isRemoved", isRemoved)
}
