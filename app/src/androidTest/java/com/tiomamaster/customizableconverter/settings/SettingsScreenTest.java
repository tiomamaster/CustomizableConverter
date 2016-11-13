package com.tiomamaster.customizableconverter.settings;

import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.preference.PreferenceManager;
import android.widget.ImageButton;
import android.widget.TextView;

import com.tiomamaster.customizableconverter.R;
import com.tiomamaster.customizableconverter.converter.ConverterActivity;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isClickable;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withParent;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.core.AllOf.allOf;
import static org.hamcrest.core.IsInstanceOf.instanceOf;
import static org.hamcrest.core.IsNot.not;
import static org.junit.Assert.*;

/**
 * Created by Artyom on 31.10.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class SettingsScreenTest {

    @Rule
    public ActivityTestRule<SettingsActivity> mActivityRule = new ActivityTestRule<>(
            SettingsActivity.class);

    @Test
    public void toolbarTitlesTest() {
        // check title is settings
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.settings)));

        // go to editor
        onView(withText(R.string.pref_title_customize)).perform(click());

        // check title
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(isDisplayed()));

        // back to settings
        onView(allOf(instanceOf(ImageButton.class), withParent(withId(R.id.toolbar))))
                .perform(click());

        // check title
        onView(allOf(instanceOf(TextView.class), withParent(withId(R.id.toolbar))))
                .check(matches(withText(R.string.settings)));
    }

    @Test
    public void grSizeOptionEnableTest() {

        boolean isStandardForm = PreferenceManager.getDefaultSharedPreferences(
                mActivityRule.getActivity()).getBoolean("pref_standard_form", false);

        if (!isStandardForm) {

            onView(withText(R.string.pref_title_grouping_size)).check(matches(isEnabled()));

            // enable standard form
            onView(withText(R.string.pref_title_standard_form)).perform(click());

            // check grouping size option is disabled
            onView(withText(R.string.pref_title_grouping_size)).check(matches(not(isEnabled())));
        } else {

            onView(withText(R.string.pref_title_grouping_size)).check(matches(not(isEnabled())));

            // disable standard form
            onView(withText(R.string.pref_title_standard_form)).perform(click());

            // check grouping size option is enabled
            onView(withText(R.string.pref_title_grouping_size)).check(matches(isEnabled()));
        }
    }
}