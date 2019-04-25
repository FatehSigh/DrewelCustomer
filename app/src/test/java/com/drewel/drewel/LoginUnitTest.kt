package com.drewel.drewel

import com.drewel.drewel.activity.LoginActivity
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class LoginUnitTest {
    @Test
    fun addition_isCorrect() {

        val loginActivity=LoginActivity()

        loginActivity.callLoginApi()

    }

}
