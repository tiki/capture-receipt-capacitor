/*
 * Copyright (c) TIKI Inc.
 * MIT license. See LICENSE file in the root directory.
 */

/**
 A struct representing an account's response data for the ReceiptCapture plugin.
 
 This struct is used to encapsulate account information for use in plugin responses. It contains details such as the username, source, and verification status of an account.
 
 - Note: This struct is typically used to construct response data for account-related plugin calls.
 */

import Foundation
import Capacitor


struct RspAccount{    
    var requestId: String
    var event: PluginEvent
    
    /// The username associated with the account.
    private let username: String
    
    /// The source or type of the account.
    private let source: String
    
    /// A boolean flag indicating whether the account is verified.
    private let isVerified: Bool
    
    /**
     Initializes an `RspAccount` object with the provided account details.
     
     - Parameter account: An `Account` object containing the account information.
     */
    public init(requestId: String, event: PluginEvent, account: Account) {
        self.requestId = requestId
        self.event = event
        self.username = account.user
        self.source = account.accountType.source
        self.isVerified = account.isVerified ?? false
    }
    
    /**
     Converts the `RspAccount` object into a dictionary suitable for use in plugin response data.
     
     - Returns: A dictionary representing the account data in a format suitable for a Capacitor plugin call result.
     */
    func toPluginCallResultData() -> PluginCallResultData {
        let payload = [
            "username" : username,
            "id" : source,
            "verified" : isVerified
        ] as [String : Any]
        return [
            "requestId" : requestId,
            "event" : event.rawValue,
            "payload" : payload
        ]
    }
}
