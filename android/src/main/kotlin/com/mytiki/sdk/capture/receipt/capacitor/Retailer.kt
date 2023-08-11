/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.sdk.capture.receipt.capacitor

import android.content.Context
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.microblink.core.ScanResults
import com.microblink.core.Timberland
import com.microblink.digital.ProviderSetupResults
import com.microblink.linking.*
import com.mytiki.sdk.capture.receipt.capacitor.req.ReqRetailerAccount
import com.mytiki.sdk.capture.receipt.capacitor.req.ReqInitialize
import com.mytiki.sdk.capture.receipt.capacitor.rsp.RspLogin
import com.mytiki.sdk.capture.receipt.capacitor.rsp.RspRetailerAccount
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi

class Retailer {
    private lateinit var client: AccountLinkingClient

    @OptIn(ExperimentalCoroutinesApi::class)
    fun initialize(
        req: ReqInitialize,
        context: Context,
        onError: (msg: String?, data: JSObject) -> Unit,
    ): CompletableDeferred<Unit> {
        val isLinkInitialized = CompletableDeferred<Unit>()
        BlinkReceiptLinkingSdk.licenseKey = req.licenseKey!!
        BlinkReceiptLinkingSdk.productIntelligenceKey = req.productKey!!
        BlinkReceiptLinkingSdk.initialize(context, OnInitialize(isLinkInitialized, onError))
        client = client(context)
        return isLinkInitialized
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun client(
        context: Context,
        dayCutoff: Int = 15,
        latestOrdersOnly: Boolean = false,
        countryCode: String = "US",
    ): AccountLinkingClient{
        val client = AccountLinkingClient(context)
        client.dayCutoff = dayCutoff
        client.latestOrdersOnly = latestOrdersOnly
        client.countryCode = countryCode

        return client
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun account(call: PluginCall): CompletableDeferred<Boolean> {
        val req = ReqRetailerAccount(call.data)
        val account = Account(
            req.retailerId.value,
            PasswordCredentials(req.username, req.password)
        )
        val isAccountLinked = CompletableDeferred<Boolean>()
        client.link(account)
            .addOnSuccessListener {
                clientVerification(call){
                    val rsp = RspRetailerAccount(req.username, req.retailerId)
                    call.resolve(JSObject.fromJSONObject(rsp.toJson()))
                    isAccountLinked.complete(it)
                }
            }
            .addOnFailureListener {
                isAccountLinked.completeExceptionally(it)
            }
        return isAccountLinked
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun clientVerification(
        call: PluginCall,
        onSuccess: () -> Unit
    ) {
        val req = ReqRetailerAccount(call.data)
        val account = Account(
            req.retailerId.value,
            PasswordCredentials(req.username, req.password)
        )
        client.verify(account.retailerId,
            success = { success: Boolean, uuid: String ->
                if (success){
                    onSuccess()
                } else {
                    call.reject("Client Verification Failed")
                }
            },
            failure = { exception ->
//                TODO: Handle exceptions
//                when (exception.code){
//                    INTERNAL_ERROR -> call.reject("Internal Error")
//                    INVALID_CREDENTIALS -> call.reject("Invalid Credentials")
//                    PARSING_FAILURE -> Timberland.d("Parsing Failure")
//                    USER_INPUT_COMPLETED -> call.reject("User Input Completed")
//                    JS_CORE_LOAD_FAILURE -> call.reject("JS Core Load Failure")
//                    JS_INVALID_DATA -> call.reject("JS Invalid Data")
//                    VERIFICATION_NEEDED -> call.reject("Verification Needed")
//                    MISSING_CREDENTIALS -> call.reject("Missing Credentials")
//                    else -> call.reject("Unknown Error")
//                }
//                 TODO: Show WebView of verification needed
//                if (exception.code == VERIFICATION_NEEDED) {
//                    //in this case, the exception.view will be != null, so you can show it in your app
//                    //and the user can resolve the needed verification, i.e.:
//                    if (exception.view != null) {
//                        binding.webViewContainer.addView(exception.view)
//                    }
//                }
            }
        )
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    fun orders(
        call: PluginCall
    ): CompletableDeferred<MutableList<ScanResults>> {
        val req = ReqRetailerAccount(call.data)
        val account = Account(
            req.retailerId.value,
            PasswordCredentials(req.username, req.password)
        )

        val orders = CompletableDeferred<MutableList<ScanResults>>()
        val allOrders = mutableListOf<ScanResults>()
        clientVerification(call){
            client.orders(
                req.retailerId.value,
                success = { retailerId: Int, results: ScanResults?, remaining: Int, uuid: String ->
                    if (results != null) {
                        allOrders.add(results)
                    }
                    if (remaining == 0) {
                        orders.complete(allOrders)
                    }
                },
                failure = { retailerId: Int, exception: AccountLinkingException ->
//                    TODO: Show WebView of verification needed
//                    if (exception.code == VERIFICATION_NEEDED) {
//                        orders.completeExceptionally(exception)
//                        if (exception.view != null) {
//                            binding.webViewContainer.addView(exception.view)
//                        }
//                    }
                },
            )
        }
        return orders
    }



}
