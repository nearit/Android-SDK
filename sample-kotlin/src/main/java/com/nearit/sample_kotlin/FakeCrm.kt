package com.nearit.sample_kotlin

class FakeCrm {

    fun logout() {
        // call to my crm server
    }

    fun login(username: String, password: String, listener: LoginListener) {
        // call to my server and fetch UserData
        val userData = UserData()
        listener.onLogin(userData)
    }

    fun saveProfileId(nearProfileId: String) {
        // add the near profile to the user data and sync to my server
    }

    interface LoginListener {
        fun onLogin(userData: UserData)
    }

    inner class UserData {
        internal var username: String? = null
        internal var nearProfileId: String? = null
    }

}