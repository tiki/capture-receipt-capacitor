/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

package com.mytiki.sdk.capture.receipt.capacitor

import android.content.Context
import android.os.Build
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.getcapacitor.JSObject
import com.getcapacitor.PluginCall
import com.microblink.core.ScanResults
import com.microblink.linking.*
import com.mytiki.sdk.capture.receipt.capacitor.req.ReqInitialize
import com.mytiki.sdk.capture.receipt.capacitor.rsp.RspScan
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.async

/**
 * This class represents the Retailer functionality for account linking and order retrieval.
 */
class Retailer {
    @OptIn(ExperimentalCoroutinesApi::class)
    private lateinit var client: AccountLinkingClient

    /**
     * Initializes the Retailer SDK.
     *
     * @param req The initialization request.
     * @param context The Android application context.
     * @param onError A callback to handle initialization errors.
     * @return A [CompletableDeferred] indicating whether the initialization was successful.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun initialize(
        req: ReqInitialize,
        context: Context,
        onError: (msg: String?, data: JSObject) -> Unit,
    ): CompletableDeferred<Unit> {
        val isLinkInitialized = CompletableDeferred<Unit>()
        BlinkReceiptLinkingSdk.licenseKey = req.licenseKey
        BlinkReceiptLinkingSdk.productIntelligenceKey = req.productKey
        BlinkReceiptLinkingSdk.initialize(context, OnInitialize(isLinkInitialized, onError))
        client = client(context)
        return isLinkInitialized
    }

    /**
     * Logs in a user account.
     *
     * @param call The plugin call.
     * @param account The user's account.
     * @param context The Android application context.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun login(call: PluginCall, account: Account, context: Context) {
        val mbAccount = Account(
            RetailerEnum.fromString(account.accountCommon.source).toMbInt(),
            PasswordCredentials(account.username, account.password!!)
        )
        val client = client(context)
        client.link(mbAccount)
            .addOnSuccessListener {
                if (it) {
                    verify(mbAccount, context, call)
                } else {
                    call.reject("login failed")
                }
            }
            .addOnFailureListener {
                call.reject(it.message)
            }
    }

    /**
     * Removes a user account.
     *
     * @param call The plugin call.
     * @param accountCommon The common account details.
     * @param context The Android application context.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun remove(call: PluginCall, accountCommon: AccountCommon, context: Context) {
        val client = client(context)
        client.accounts().addOnSuccessListener { accounts ->
            val mbAccount = accounts?.firstOrNull {
                it.retailerId == RetailerEnum.fromString(accountCommon.source).toMbInt()
            }
            if (mbAccount != null) {
                client.unlink(mbAccount).addOnSuccessListener {
                    call.resolve()
                }.addOnFailureListener {
                    call.reject(it.message)
                }
            } else {
                call.reject("Account not found")
            }
        }
    }

    /**
     * Flushes the order history.
     *
     * @param call The plugin call.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun flush(call: PluginCall) {
        client.resetHistory().addOnSuccessListener {
            call.resolve()
        }.addOnFailureListener {
            call.reject(it.message)
        }
    }

    /**
     * Retrieves orders for all verified accounts.
     *
     * @param call The plugin call.
     * @param context The Android application context.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun orders(call: PluginCall, context: Context) {
        val client: AccountLinkingClient = client(context)
        MainScope().async {
            val accounts = accounts(context).await()
            accounts.forEach {
                if (it.isVerified!!) {
                    val retailer = RetailerEnum.fromString(it.accountCommon.source).toMbInt()
                    val ordersSuccessCallback =
                        { _: Int, results: ScanResults?, remaining: Int, _: String ->
                            if (results != null) {
                                if (remaining == 0) {
                                    val rsp = RspScan(results, it, false)
                                    call.resolve(JSObject(rsp.toJson().toString()))
                                } else {
                                    val rsp = RspScan(results, it)
                                    call.resolve(JSObject(rsp.toJson().toString()))
                                }
                            } else {
                                call.reject("no result")
                            }
                        }
                    val ordersFailureCallback = { _: Int, exception: AccountLinkingException ->
                        call.reject(exception.message)
                    }
                    client.orders(
                        retailer,
                        ordersSuccessCallback,
                        ordersFailureCallback,
                    )
                }
            }
        }
    }

    /**
     * Retrieves orders for a specific user account.
     *
     * @param call The plugin call.
     * @param account The user's account.
     * @param context The Android application context.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun orders(call: PluginCall, account: Account, context: Context) {
        val client: AccountLinkingClient = client(context)
        MainScope().async {
            val mbAccount = mbAccounts(context).await().first {
                account.username === it.credentials.username() && account.accountCommon.source === RetailerEnum.fromMbInt(
                    it.retailerId
                ).name
            }
            account.isVerified = verify(mbAccount, context).await()
            if (account.isVerified!!) {
                val retailer = RetailerEnum.fromString(account.accountCommon.source).toMbInt()
                val ordersSuccessCallback =
                    { _: Int, results: ScanResults?, _: Int, _: String ->
                        if (results != null) {
                            val rsp = RspScan(results, account, false)
                            call.resolve(JSObject(rsp.toJson().toString()))
                        } else {
                            call.reject("no result")
                        }
                    }
                val ordersFailureCallback = { _: Int, exception: AccountLinkingException ->
                    call.reject(exception.message)
                }
                client.orders(
                    retailer,
                    ordersSuccessCallback,
                    ordersFailureCallback,
                )
            }
        }
    }

    /**
     * Retrieves a list of user accounts.
     *
     * @param context The Android application context.
     * @return A [CompletableDeferred] containing the list of user accounts.
     */
    fun accounts(context: Context): CompletableDeferred<List<Account>> {
        val accounts = CompletableDeferred<List<Account>>()
        val list = mutableListOf<Account>()
        MainScope().async {
            mbAccounts(context).await().map { mbAccount ->
                val account = Account.fromRetailerAccount(mbAccount)
                account.isVerified = verify(mbAccount, context).await()
                list.add(account)
            }
            accounts.complete(list)
        }
        return accounts
    }

    /**
     * Retrieves a list of user accounts from the Retailer SDK.
     *
     * @param context The Android application context.
     * @return A [CompletableDeferred] containing the list of user accounts.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    fun mbAccounts(context: Context): CompletableDeferred<List<com.microblink.linking.Account>> {
        val client: AccountLinkingClient = client(context)
        val mbAccounts = CompletableDeferred<List<com.microblink.linking.Account>>()
        client.accounts()
            .addOnSuccessListener { mbAccountList ->
                MainScope().async {
                    if (mbAccountList != null) {
                        mbAccounts.complete(mbAccountList)
                    } else {
                        mbAccounts.complete(mutableListOf())
                    }
                }
            }
            .addOnFailureListener {
                mbAccounts.completeExceptionally(it)
            }
        return mbAccounts
    }

    /**
     * Verifies a user account.
     *
     * @param mbAccount The user's account in the Retailer SDK.
     * @param context The Android application context.
     * @param call The plugin call (optional).
     * @return A [CompletableDeferred] indicating whether the account verification was successful.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun verify(
        mbAccount: com.microblink.linking.Account,
        context: Context,
        call: PluginCall? = null
    ): CompletableDeferred<Boolean> {
        val client: AccountLinkingClient = client(context)
        val verifyCompletable = CompletableDeferred<Boolean>()
        val account = Account.fromRetailerAccount(mbAccount)
        client.verify(
            RetailerEnum.fromString(account.accountCommon.source).value,
            success = { isVerified: Boolean, _: String ->
                if (isVerified) {
                    account.isVerified = true
                    call?.resolve(account.toRsp())
                    verifyCompletable.complete(true)
                } else {
                    client.unlink(mbAccount)
                    call?.reject("login failed")
                    verifyCompletable.complete(false)
                }
            },
            failure = { exception ->
                if (call == null) {
                    verifyCompletable.complete(false)
                    client.close()
                } else if (exception.code == VERIFICATION_NEEDED && exception.view != null) {
                    exception.view!!.isFocusableInTouchMode = true
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        exception.view!!.focusable = View.FOCUSABLE
                    }
                    val builder: AlertDialog.Builder = AlertDialog.Builder(context)
                    builder.setTitle("Verify your account")
                    builder.setView(exception.view)
                    val dialog: AlertDialog = builder.create()
                    dialog.show()
                } else {
                    when (exception.code) {
                        INTERNAL_ERROR -> call.reject("Login failed: Internal Error")
                        INVALID_CREDENTIALS -> call.reject("Login failed: Invalid Credentials")
                        PARSING_FAILURE -> call.reject("Login failed: Parsing Failure")
                        USER_INPUT_COMPLETED -> call.reject("Login failed: User Input Completed")
                        JS_CORE_LOAD_FAILURE -> call.reject("Login failed: JS Core Load Failure")
                        JS_INVALID_DATA -> call.reject("Login failed: JS Invalid Data")
                        MISSING_CREDENTIALS -> call.reject("Login failed: Missing Credentials")
                        else -> call.reject("Login failed: Unknown Error")
                    }
                    client.unlink(mbAccount)
                }
            }
        )
        return verifyCompletable
    }

    /**
     * Creates an instance of the AccountLinkingClient with optional configuration parameters.
     *
     * @param context The Android application context.
     * @param dayCutoff The day cutoff for order retrieval (optional, default is 500).
     * @param latestOrdersOnly Indicates whether to retrieve only the latest orders (optional, default is false).
     * @param countryCode The country code for order retrieval (optional, default is "US").
     * @return An instance of the [AccountLinkingClient].
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private fun client(
        context: Context,
        dayCutoff: Int = 500,
        latestOrdersOnly: Boolean = false,
        countryCode: String = "US",
    ): AccountLinkingClient {
        val client = AccountLinkingClient(context)
        client.dayCutoff = dayCutoff
        client.latestOrdersOnly = latestOrdersOnly
        client.countryCode = countryCode
        return client
    }
}
