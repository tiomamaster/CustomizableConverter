package com.tiomamaster.customizableconverter.converter;


import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.filters.LargeTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Adapter;

import com.tiomamaster.customizableconverter.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.matcher.ViewMatchers.hasImeAction;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anyOf;
import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Created by Artyom on 19.07.2016.
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConverterScreenTest {

    @Rule
    public ActivityTestRule<ConverterActivity> mActivityRule = new ActivityTestRule<>(
            ConverterActivity.class);

    private ConverterActivity mActivity;

    private ConverterFragment mFragment;

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
        mFragment = (ConverterFragment) mActivity.getSupportFragmentManager().
                findFragmentByTag(mActivity.mConverterFragmentTag);
    }

    @Test
    public void checkSpinnerConvertersStateAfterRotate() {
        // Spinner Converter Types prepare for rotate
        Adapter adapterConv = mActivity.mSpinConvTypes.getAdapter();
        String selectionText = adapterConv.getItem(
                adapterConv.getCount() - 1).toString();
        onView(withId(R.id.spinner_conv_types)).perform(click());
        onData(hasToString(selectionText)).perform(click());

        rotateScreenAndCheckIt();

        // Check state
        onView(withId(R.id.spinner_conv_types))
                .check(matches(withSpinnerText(selectionText)));
    }

    @Test
    public void checkRecyclerViewStateAfterRotate() {
        // spinner units prepare for rotate
        Adapter adapter = mFragment.mSpinnerUnits.getAdapter();
        String selectionText = adapter.getItem(
                adapter.getCount() - 1).toString();
        onView(withId(R.id.spinner_units)).perform(click());
        onData(hasToString(selectionText)).perform(click());

        // quantity edit text type text
        String typedText = "9999.879";
        onView(withId(R.id.quantity)).perform(typeText(typedText));

        // recycler view
        int count = mFragment.mConversionResult.getAdapter().getItemCount();

        rotateScreenAndCheckIt();

        // check state
        // spinner
        onView(withId(R.id.spinner_units)).check(matches(withSpinnerText(selectionText)));

        // edit text
        onView(withId(R.id.quantity)).check(matches(withText(typedText)));

        // recycler view
        Assert.assertEquals(count, mFragment.mConversionResult.getAdapter().getItemCount());
    }

    @Test
    public void chooseConverterAndShowIt() {
        // Select all converters in Spinner
        Adapter adapter = mActivity.mSpinConvTypes.getAdapter();
        for (int j = 0; j < adapter.getCount(); j++) {
            String selectionText = adapter.getItem(j).toString();
            onView(withId(R.id.spinner_conv_types)).perform(click());
            onData(hasToString(selectionText)).perform(click());

            // Check content in Spinner Units
            String[] expected = mFragment.mCurConverter.getAllUnitsName();
            onView(withId(R.id.spinner_units)).perform(click());
            for (int i = 0; i < expected.length; i++) {
                onData(is(instanceOf(String.class))).
                        atPosition(i).
                        check(matches(withText(expected[i])));
            }
            onData(hasToString(expected[0])).perform(click());
        }
    }

    @Test
    public void conversionTest() {
        double quantity = 998.2217;
        String from = mFragment.mSpinnerUnits.getSelectedItem().toString();

        // Type text into EditText
        onView(withId(R.id.quantity)).perform(typeText(String.valueOf(quantity)));

        // Get the result from the converter
        String[][] expected = mFragment.mCurConverter.convertAllExt(quantity, from);

        checkRecyclerView(expected);

        from = mFragment.mSpinnerUnits.getAdapter().getItem(1).toString();

        expected = mFragment.mCurConverter.convertAllExt(quantity, from);

        // Select another unit
        onView(withId(R.id.spinner_units)).perform(click());
        onData(hasToString(from)).perform(click());

        checkRecyclerView(expected);
    }

    @Test
    public void textViewResultVisibilityTest() {
        // check text invisible
        onView(withId(R.id.resultText)).
                check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        // type text for conversion
        onView(withId(R.id.quantity)).perform(typeText("1"));

        // check text visible
        onView(withId(R.id.resultText)).
                check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
    }

    @Test
    public void selectAnotherConverterTest() {
        // type text to show result
        onView(withId(R.id.quantity)).perform(typeText("987.987"));

        // select item on spinner units above 0
        onView(withId(R.id.spinner_units)).perform(click());
        onData(is(instanceOf(String.class))).atPosition(1).perform(click());

        // select another type of converter
        onView(withId(R.id.spinner_conv_types)).perform(click());
        onData(is(instanceOf(String.class))).atPosition(2).perform(click());

        // check spinner units selected 0 item(or saved item - not impl yet)
        Assert.assertEquals(0, mFragment.mSpinnerUnits.getSelectedItemPosition());

        // check edit text is empty(or have saved text - not impl yet)
        onView(withId(R.id.quantity)).check(matches(withText("")));

        // check text view result is invisible(or no if state was saved - not impl yet)
        onView(withId(R.id.resultText)).
                check(matches(withEffectiveVisibility(ViewMatchers.Visibility.INVISIBLE)));

        // check recycler result is empty, only header view presented(or load saved result - no impl yet)
        Assert.assertEquals(1, mFragment.mConversionResult.getAdapter().getItemCount());
    }

    @Test
    public void closeSoftKeyboardWhenScrollTest() {
        // input text to show keyboard
        onView(withId(R.id.quantity)).perform(typeText("900"));

        // check soft keyboard is being shown
        Assert.assertTrue(mFragment.imm.isActive());

        // scroll
        onView(withId(R.id.conversion_result)).perform(RecyclerViewActions.actionOnItemAtPosition(7, click()));

        // check soft keyboard is hidden
        Assert.assertFalse(mFragment.imm.isActive());
    }

    private void checkRecyclerView(String[][] expected) {
        // Check the result in RecyclerView
        ViewInteraction viewInteraction = onView(withId(R.id.conversion_result));
        for (int i = 0; i < expected.length; i++) {
            viewInteraction.perform(RecyclerViewActions.scrollToPosition(i+1));
            viewInteraction.check(matches(atPosition(i+1, allOf(
                    withChild(withText(is(expected[i][0]))),
                    withChild(withText(is(expected[i][1])))))));
        }
        viewInteraction.perform(RecyclerViewActions.scrollToPosition(0));
    }

    private void rotateScreenAndCheckIt() {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        Assert.assertEquals(mActivity.getRequestedOrientation(),
                ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
    }

    private static Matcher<View> atPosition(final int position, @NonNull final Matcher<View> itemMatcher) {
        checkNotNull(itemMatcher);
        return new BoundedMatcher<View, RecyclerView>(RecyclerView.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("has item at position " + position + ": ");
                itemMatcher.describeTo(description);
            }

            @Override
            protected boolean matchesSafely(final RecyclerView view) {
                RecyclerView.ViewHolder viewHolder = view.findViewHolderForAdapterPosition(position);
                if (viewHolder == null) {
                    // has no item on such position
                    return false;
                }
                return itemMatcher.matches(viewHolder.itemView);
            }
        };
    }
}
