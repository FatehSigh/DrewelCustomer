package com.os.drewel

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.contrib.RecyclerViewActions
import android.support.test.espresso.matcher.ViewMatchers
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.rule.ActivityTestRule
import android.support.v7.widget.RecyclerView
import com.os.drewel.activity.ProductActivity
import com.os.drewel.activity.SignUpActivity
import org.junit.Rule
import org.junit.Test
import android.app.Activity
import android.app.Instrumentation.ActivityResult
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra
import android.support.test.espresso.intent.matcher.IntentMatchers.toPackage
import org.hamcrest.CoreMatchers.allOf
import org.junit.Before



/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class CategoryTest {

    private val ITEM_BELOW_THE_FOLD = 40
    @get:Rule
    public val mActivityRule: ActivityTestRule<ProductActivity> = ActivityTestRule( ProductActivity::class.java)

    @Before
    fun stubAllExternalIntents() {
        // By default Espresso Intents does not stub any Intents. Stubbing needs to be setup before
        // every test run. In this case all external Intents will be blocked.
        intended(allOf(
                toPackage("com.your.package.name"),
                hasExtra("MY_EXTRA", "MY EXTRA VALUE")
        ));
    }

    @Test
    @Throws(InterruptedException::class)
    fun changeText_sameActivity() {
        // Type text and then press the button.
       /* onView(ViewMatchers.withId(R.id.brandRecyclerView))
                .perform(RecyclerViewActions.actionOnItemAtPosition<RecyclerView.ViewHolder>(ITEM_BELOW_THE_FOLD, click()))
*/
    }
}