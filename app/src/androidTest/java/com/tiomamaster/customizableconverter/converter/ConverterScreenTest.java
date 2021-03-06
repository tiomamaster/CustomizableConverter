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
import android.support.v4.util.Pair;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Adapter;

import com.tiomamaster.customizableconverter.R;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withSpinnerText;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.google.common.base.Preconditions.checkNotNull;
import static junit.framework.Assert.assertEquals;
import static junit.framework.TestCase.assertFalse;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertTrue;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class ConverterScreenTest {

    @Rule public ActivityTestRule<ConverterActivity> mActivityRule = new ActivityTestRule<>(
            ConverterActivity.class);

    private ConverterActivity mActivity;

    private ConverterFragment mFragment;

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
        mFragment = (ConverterFragment) mActivity.getSupportFragmentManager().
                findFragmentByTag(ConverterActivity.CONVERTER_FRAGMENT_TAG);
    }

    @Test
    public void rotate_CheckSpinnerConvertersState() {
        // spinner converter types prepare for rotate
        Adapter adapter = mActivity.mSpinConverterTypes.getAdapter();
        String selectionText = adapter.getItem(
                adapter.getCount() - 1).toString();
        onView(withId(R.id.spinner_converter_types)).perform(click());
        onData(hasToString(selectionText)).perform(click());

        rotateScreenAndCheckIt();

        // Check state
        onView(withId(R.id.spinner_converter_types))
                .check(matches(withSpinnerText(selectionText)));
    }

    @Test
    public void rotate_CheckRecyclerViewState() {
        // spinner units prepare for rotate
        Adapter adapter = mFragment.mSpinnerUnits.getAdapter();
        String selectionText = adapter.getItem(
                adapter.getCount() - 1).toString();
        onView(withId(R.id.spinner_units)).perform(click());
        onData(hasToString(selectionText)).perform(click());

        // quantity edit text type text
        String typedText = "9999.879";
        onView(withId(R.id.quantity)).perform(clearText()).perform(typeText(typedText));

        // recycler view
        int count = mFragment.mConversionResult.getAdapter().getItemCount();

        rotateScreenAndCheckIt();

        // check state
        // spinner
        onView(withId(R.id.spinner_units)).check(matches(withSpinnerText(selectionText)));

        // edit text
        onView(withId(R.id.quantity)).check(matches(withText(typedText)));

        // recycler view
        assertEquals(count, mFragment.mConversionResult.getAdapter().getItemCount());
    }

    @Test
    public void chooseConverter_ShowIt() {
        // select all converters in spinner
        Adapter adapter = mActivity.mSpinConverterTypes.getAdapter();
        for (int j = 0; j < adapter.getCount(); j++) {
            String selectionText = adapter.getItem(j).toString();
            onView(withId(R.id.spinner_converter_types)).perform(click());
            onData(hasToString(selectionText)).perform(click());

            // check content in spinner units
            List<String> expected = ((ConverterPresenter) mFragment.mActionsListener)
                    .mCurConverter.getEnabledUnitsName();
            onView(withId(R.id.spinner_units)).perform(click());
            for (int i = 0; i < expected.size(); i++) {
                onData(is(instanceOf(String.class))).
                        atPosition(i).
                        check(matches(withText(expected.get(i))));
            }
            onData(hasToString(expected.get(0))).perform(click());
        }
    }

    @Test
    public void convert_CheckResult() {
        double quantity = 998.2217;
        String from = mFragment.mSpinnerUnits.getSelectedItem().toString();

        // type text into EditText
        onView(withId(R.id.quantity)).perform(clearText()).perform(typeText(String.valueOf(quantity)));

        // get the result from the converter
        List<Pair<String, String>> expected = ((ConverterPresenter) mFragment.mActionsListener)
                .mCurConverter.convertAll(quantity, from);

        checkRecyclerView(expected);

        from = mFragment.mSpinnerUnits.getAdapter().getItem(5).toString();

        expected = ((ConverterPresenter) mFragment.mActionsListener)
                .mCurConverter.convertAll(quantity, from);

        // select another unit
        onView(withId(R.id.spinner_units)).perform(click());
        onData(hasToString(from)).perform(click());

        checkRecyclerView(expected);
    }

    @Test
    public void scroll_CloseSoftKeyboard() {
        // input text to show keyboard
        onView(withId(R.id.quantity)).perform(typeText("900"));

        // check soft keyboard is being shown
        assertTrue(mFragment.mImm.isActive());

        // scroll
        onView(withId(R.id.conversion_result)).perform(RecyclerViewActions.actionOnItemAtPosition(7, click()));

        // check soft keyboard is hidden
        assertFalse(mFragment.mImm.isActive());
    }

    @Test
    public void changeConverter_SaveConverterState() {
        int converterPos = 2;
        int anotherConverterPos = 3;
        int unitPosition = 3;
        String quantityText = "54.02164";

        // select converter
        ViewInteraction spinnerConverterTypes = onView(withId(R.id.spinner_converter_types));
        spinnerConverterTypes.perform(click());
        onData(is(instanceOf(String.class))).atPosition(converterPos).perform(click());

        // select unit
        onView(withId(R.id.spinner_units)).perform(click());
        onData(is(instanceOf(String.class))).atPosition(unitPosition).perform(click());

        // input quantity
        onView(withId(R.id.quantity)).perform(clearText()).perform(typeText(quantityText));

        // select another converter
        spinnerConverterTypes.perform(click());
        onData(is(instanceOf(String.class))).atPosition(anotherConverterPos).perform(click());

        // select converter again and check it state
        spinnerConverterTypes.perform(click());
        onData(is(instanceOf(String.class))).atPosition(converterPos).perform(click());

        // spinner units state
        assertEquals(unitPosition, mFragment.mSpinnerUnits.getSelectedItemPosition());

        // quantity edit text state
        onView(withId(R.id.quantity)).check(matches(withText(quantityText)));

        // check text view result is visible
        onView(withId(R.id.resultText)).
                check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));

        // result is shown
        assertTrue(mFragment.mConversionResult.getAdapter().getItemCount() > 1);
    }

    @Test
    public void changeConverter_HideSoftInput() {
        // select converter
        ViewInteraction spinnerConverterTypes = onView(withId(R.id.spinner_converter_types));
        spinnerConverterTypes.perform(click());
        onData(is(instanceOf(String.class))).atPosition(0).perform(click());

        // touch edit text to show soft input
        onView(withId(R.id.quantity)).perform(typeText("1"));

        // check the keyboard is visible
        assertTrue(mFragment.mImm.isActive());

        // select another converter
        spinnerConverterTypes.perform(click());
        onData(is(instanceOf(String.class))).atPosition(1).perform(click());

        // check the keyboard is invisible
        assertFalse(mFragment.mImm.isActive());
    }

    @Test
    public void clickSettingsMenuItem_OpenSettingsUi() {

        // Click on settings option
        onView(withId(R.id.settings)).perform(click());

        // check that settings text in toolbar is visible
        onView(withText(R.string.title_fragment_settings)).check(matches(isDisplayed()));
    }

    private void checkRecyclerView(List<Pair<String, String>> expected) {
        // Check the result in RecyclerView
        ViewInteraction viewInteraction = onView(withId(R.id.conversion_result));
        for (int i = 0; i < expected.size(); i++) {
            viewInteraction.perform(RecyclerViewActions.scrollToPosition(i+1));
            viewInteraction.check(matches(atPosition(i+1, allOf(
                    withChild(withText(is(expected.get(i).first))),
                    withChild(withText(is(expected.get(i).second)))))));
        }
        viewInteraction.perform(RecyclerViewActions.scrollToPosition(0));
    }

    private void rotateScreenAndCheckIt() {
        mActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        assertEquals(mActivity.getRequestedOrientation(),
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
                // has no item on such position
                return viewHolder != null && itemMatcher.matches(viewHolder.itemView);
            }
        };
    }
}