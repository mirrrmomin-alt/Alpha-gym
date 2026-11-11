package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import androidx.compose.ui.test.performTextClearance
import androidx.compose.ui.test.performTextInput
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createAndroidComposeRule<MainActivity>()

  @Test
  fun `complete secure authentication and role based access control flow`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Alpha Gym", appName)

    composeTestRule.waitForIdle()

    // 1. Verify 3D Startup Logo, Central Login Page & Complete Removal of Google Sign-In
    composeTestRule.onNodeWithTag("alpha_gym_3d_startup_logo").assertIsDisplayed()
    composeTestRule.onNodeWithText("ALPHA GYM CENTRE").assertIsDisplayed()
    composeTestRule.onNodeWithText("ALPHA • A UNISEX GYM").assertIsDisplayed()
    composeTestRule.onNodeWithText("Push Your Limits. Build Your Legacy.").assertIsDisplayed()
    composeTestRule.onNodeWithText("Welcome back").assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_remember_me_checkbox").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_forgot_password_btn").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_submit_btn").performScrollTo().assertIsDisplayed()
    composeTestRule.onNodeWithTag("open_member_registration_button").performScrollTo().assertIsDisplayed()
    assertTrue(
      "Google sign-in button must be completely removed",
      composeTestRule.onAllNodesWithText("Continue with Google").fetchSemanticsNodes().isEmpty()
    )

    // 2. Owner admin email with wrong password fails
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().performTextInput("alphagym@yahoo.com")
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextInput("wrongpass123")
    composeTestRule.onNodeWithTag("login_submit_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("auth_error_banner").performScrollTo().assertIsDisplayed()

    // 3. Ordinary member credentials on Admin tab cannot access Admin Dashboard
    composeTestRule.onNodeWithTag("auth_tab_admin").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().performTextInput("member@alphagym.com")
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextInput("member123")
    composeTestRule.onNodeWithTag("login_submit_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("auth_error_banner").performScrollTo().assertIsDisplayed()

    // 4. Forgot password modal works and closes on submit
    composeTestRule.onNodeWithTag("login_forgot_password_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithTag("forgot_password_email_input").performTextClearance()
    composeTestRule.onNodeWithTag("forgot_password_email_input").performTextInput("member@alphagym.com")
    composeTestRule.onNodeWithTag("forgot_password_submit_btn").performClick()
    composeTestRule.waitForIdle()

    // 5. New member registers -> receives role = member & status = pending
    composeTestRule.onNodeWithTag("open_member_registration_button").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("member_registration_list")
      .performScrollToNode(hasTestTag("reg_full_name_input"))
    composeTestRule.onNodeWithTag("reg_full_name_input").performTextInput("Vikram Singh")
    composeTestRule.onNodeWithTag("reg_email_input").performTextInput("vikram@alphagym.com")
    composeTestRule.onNodeWithTag("reg_phone_input").performTextInput("+1555771122")
    composeTestRule.onNodeWithTag("reg_address_input").performTextInput("900 Red Crest Ave")
    composeTestRule.onNodeWithTag("reg_password_input").performTextInput("member123")
    composeTestRule.onNodeWithTag("reg_confirm_password_input").performTextInput("member123")

    composeTestRule.onNodeWithTag("member_registration_list")
      .performScrollToNode(hasTestTag("submit_member_registration_btn"))
    composeTestRule.onNodeWithTag("submit_member_registration_btn").performClick()
    composeTestRule.waitForIdle()

    // 6. Pending member sees "Your account is waiting for admin approval." and cannot access Member Dashboard
    composeTestRule.onNodeWithTag("member_pending_approval_screen").assertIsDisplayed()
    assertTrue(
      composeTestRule.onAllNodesWithText("Your account is waiting for admin approval.").fetchSemanticsNodes().isNotEmpty()
    )

    // Sign out from pending screen back to central Login Page
    composeTestRule.onNodeWithTag("member_portal_logout_icon_btn").performClick()
    composeTestRule.waitForIdle()

    // 7. Owner admin logs in with alphagym@yahoo.com and alpha97970@q -> opens Admin Dashboard
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().performTextInput("alphagym@yahoo.com")
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextInput("alpha97970@q")
    composeTestRule.onNodeWithTag("login_submit_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Navigate to Approvals tab in Admin Dashboard
    composeTestRule.onNodeWithTag("nav_tab_approvals").performClick()
    composeTestRule.waitForIdle()
    composeTestRule.onNodeWithText("Member Approvals & Portal Sync").assertIsDisplayed()
    composeTestRule.onNodeWithTag("admin_approvals_list").performScrollToIndex(2)
    composeTestRule.waitForIdle()
    assertTrue(composeTestRule.onAllNodesWithText("Vikram Singh").fetchSemanticsNodes().isNotEmpty())

    // 8. Admin approves Vikram Singh -> status becomes "active"
    composeTestRule.onAllNodesWithText("Approve Member")[0].performClick()
    composeTestRule.waitForIdle()

    // Admin logs out
    composeTestRule.onNodeWithTag("sign_in_top_button").performClick()
    composeTestRule.waitForIdle()

    // 9. Approved member logs in -> Mandatory Gmail + Phone OTP verification step opens -> Member Dashboard opens
    composeTestRule.onNodeWithTag("auth_tab_member").performScrollTo().performClick()
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("login_identifier_input").performScrollTo().performTextInput("+1555771122")
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextClearance()
    composeTestRule.onNodeWithTag("login_password_input").performScrollTo().performTextInput("member123")
    composeTestRule.onNodeWithTag("login_submit_btn").performScrollTo().performClick()
    composeTestRule.waitForIdle()

    // Verify Mandatory Phone OTP & Gmail Dialog is displayed and complete verification
    composeTestRule.onNodeWithTag("member_otp_verification_dialog").assertIsDisplayed()
    composeTestRule.onNodeWithTag("member_gmail_verified_badge").assertIsDisplayed()
    composeTestRule.onNodeWithTag("login_verify_otp_submit_btn").performClick()
    composeTestRule.waitForIdle()

    composeTestRule.onNodeWithTag("member_dashboard_overview_list").assertIsDisplayed()
    composeTestRule.onNodeWithText("Welcome back, Vikram Singh!").assertIsDisplayed()
    composeTestRule.onNodeWithTag("member_dashboard_overview_list")
      .performScrollToNode(hasTestTag("member_promo_banners_section"))
    composeTestRule.onNodeWithTag("member_promo_banners_section").assertIsDisplayed()
  }
}
