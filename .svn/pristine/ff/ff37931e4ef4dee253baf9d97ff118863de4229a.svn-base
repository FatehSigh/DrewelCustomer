package com.os.drewel

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import com.os.drewel.activity.SignUpActivity
import org.junit.Rule
import org.junit.Test

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class SignUpTest {
   /* @Rule
    public val activityRule = ActivityTestRule<SignUpActivity>(
            SignUpActivity::class.java)   // launchActivity.
*/

    @get:Rule
    public val mActivityRule: ActivityTestRule<SignUpActivity> = ActivityTestRule( SignUpActivity::class.java)

    @Test
    @Throws(InterruptedException::class)
    fun changeText_sameActivity() {
        // Type text and then press the button.
       /* onView(withId(R.id.login_tiet_mobile_number))
                .perform(typeText("testoctal@gmail.com"), closeSoftKeyboard())

        onView(withId(R.id.login_tiet_password))
                .perform(typeText("test123"), closeSoftKeyboard())*/
        onView(withId(R.id.signUpButton)).perform(click())
        Thread.sleep(1000)
    }
}