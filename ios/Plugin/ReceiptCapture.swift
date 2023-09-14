/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in root directory.
 */

import Foundation
import Capacitor
import BlinkReceipt

public class ReceiptCapture: NSObject {
    
    var email: Email? = nil
    var physical: Physical? = nil
    var retailer: Retailer? = nil
    
    public static var pendingScanCall: CAPPluginCall?
    
    public func initialize(_ call: CAPPluginCall) {
        let reqInit = ReqInitialize(call)
        let licenseKey = reqInit.licenseKey
        let productKey = reqInit.productKey
        let scanManager = BRScanManager.shared()
        scanManager.licenseKey = licenseKey
        scanManager.prodIntelKey = productKey
        physical = Physical()
        email = Email()
        retailer = Retailer(licenseKey, productKey)
    }
    
   public func login(_ call: CAPPluginCall) {
        let reqLogin = ReqLogin(data: call)
        guard let accountType = AccountCommon.defaults[reqLogin.source] else {
            call.reject("Invalid source: \(reqLogin.source)")
            return
        }
        let account = Account.init(accountType: accountType, user: reqLogin.username, password: reqLogin.password, isVerified: false)
        switch account.accountType.type {
        case .email :
            guard let email = email else {
                call.reject("Email not initialized. Did you call .initialize()?")
                return
            }
            email.login(account, call)
            break
        case .retailer :
            guard let retailer = retailer else {
                call.reject("Retailer not initialized. Did you call .initialize()?")
                return
            }
            retailer.login(account, call)
            break
        }
    }
    
    public func logout(_ call: CAPPluginCall) {
        let reqLogout = ReqLogin(data: call)
        if(reqLogout.source == ""){
            if(reqLogout.username != "" || reqLogout.password != ""){
                call.reject("Error: Invalid logout arguments. If you want delete all accounts, don't send username of password")
                return
            }else{
                Email().logout(call, nil)
                Retailer().logout(call,  Account(retailer: "", username: "", password: ""))
                return
            }

        }
        guard let accountType = AccountCommon.defaults[reqLogout.source] else {
            call.reject("Invalid source: \(reqLogout.source)")
            return
        }
        let account = Account.init(accountType: AccountCommon.defaults[reqLogout.source]!, user: reqLogout.username, password: reqLogout.password, isVerified: false)
        switch account.accountType.type {
        case .email :
            guard let email = email else {
                call.reject("Email not initialized. Did you call .initialize()?")
                return
            }
            email.logout(call, account)
            break
        case .retailer :
            guard let retailer = retailer else {
                call.reject("Retailer not initialized. Did you call .initialize()?")
                return
            }
            retailer.logout(call, account)
            break
        }
    }
    
    public func accounts(_ call: CAPPluginCall) {
        guard let retailer = retailer else {
            call.reject("Retailer not initialized. Did you call .initialize()?")
            return
        }
        let retailers = retailer.accounts()
        call.resolve(RspAccountList(accounts: retailers).toPluginCallResultData())
    }
    
    public func scan(_ call: CAPPluginCall) {
        let req = ReqScan(data: call)
        if req.account == nil {
            switch req.scanType {
            case .EMAIL:
                guard let email = email else {
                    call.reject("Email not initialized. Did you call .initialize()?")
                    return
                }
                email.scan(call, req.account)
                break
            case .RETAILER:
                guard let retailer = retailer else {
                    call.reject("Retailer not initialized. Did you call .initialize()?")
                    return
                }
                retailer.orders(req.account, call)
                break
            case .PHYSICAL:
                guard let physical = physical else {
                    call.reject("Physical not initialized. Did you call .initialize()?")
                    return
                }
                ReceiptCapture.pendingScanCall = call
                physical.scan()
                break
            case .ONLINE:
                Email().scan(call, req.account)
                Retailer().orders(req.account, call)
                break
            default:
                call.reject("invalid scan type for account")
            }
        } else {
            switch req.scanType {
            case .EMAIL:
                Email().scan(call, req.account)
                break
            case .RETAILER:
                Retailer("","").orders(req.account, call)
                break
            default:
                call.reject("invalid scan type for account")
            }
        }

    }
    
}