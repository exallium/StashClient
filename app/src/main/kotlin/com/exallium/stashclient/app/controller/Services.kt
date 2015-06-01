package com.exallium.stashclient.app.controller

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.AccountManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.IBinder

public class AuthService : Service() {

    var authenticator: AbstractAccountAuthenticator? = null
        get() {
            if ($authenticator == null)
                authenticator = StashAuthenticator(getApplicationContext())
            return authenticator
        }

    override fun onBind(intent: Intent): IBinder? {
        return if (AccountManager.ACTION_AUTHENTICATOR_INTENT.equals(intent.getAction())) {
            authenticator?.getIBinder()
        } else {
            null
        }
    }


    private class StashAuthenticator(val context: Context) : AbstractAccountAuthenticator(context) {
        override fun addAccount(response: AccountAuthenticatorResponse?, accountType: String?, authTokenType: String?, requiredFeatures: Array<out String>?, options: Bundle?): Bundle? {
            val reply = Bundle()

            val i = Intent(context, javaClass<RouterActivity>())
            i.setAction("com.exallium.stashclient.app.LOGIN")
            i.putExtra(AccountManager.KEY_ACCOUNT_AUTHENTICATOR_RESPONSE, response)
            reply.putParcelable(AccountManager.KEY_INTENT, i);

            return reply;
        }

        override fun getAuthToken(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle? {
            return null
        }

        override fun hasFeatures(response: AccountAuthenticatorResponse?, account: Account?, features: Array<out String>?): Bundle? {
            return null
        }

        override fun getAuthTokenLabel(authTokenType: String?): String? {
            return null
        }

        override fun confirmCredentials(response: AccountAuthenticatorResponse?, account: Account?, options: Bundle?): Bundle? {
            return null
        }

        override fun editProperties(response: AccountAuthenticatorResponse?, accountType: String?): Bundle? {
            return null
        }

        override fun updateCredentials(response: AccountAuthenticatorResponse?, account: Account?, authTokenType: String?, options: Bundle?): Bundle? {
            return null
        }

    }

}
