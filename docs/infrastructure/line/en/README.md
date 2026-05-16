# LINE Platform Setup Procedure

This document describes the procedure for setting up an application on the LINE Platform.

## 1. Prerequisites

Before starting, please ensure the following preparations are complete:

- **LINE Business ID:** A LINE Business ID has been created.
- **Admin Consoles:** Understand the roles of the following two admin consoles:
  - **[LINE Official Account Manager](https://manager.line.biz/):** Used for managing LINE Official Accounts and message distribution settings.
  - **[Line Developer Console](https://developers.line.biz/console/):** Used for developer settings such as creating providers and channels, and configuring the Messaging API.

## 2. Create a Provider

> **Note:**
> Please skip this step if you are using an existing provider or if one has already been created.
> It is possible to link multiple channels to a single provider.

1. Access the **[Line Developer Console](https://developers.line.biz/console/)**.
2. Click on "Create a provider".
3. Enter a provider name and click "Create".

## 3. Create a Channel

1. Access the **[LINE Official Account Manager](https://manager.line.biz/)**.
2. From the "Accounts" section, click "Create".
3. Follow the on-screen instructions and enter the required information.
    - **Industry/Purpose of Use:** Select "Other".
    - **Primary Use:** Select "For message distribution".
    - **Connection method with Business Manager organization:** Select "Select a Business Manager organization" and choose the provider created in step 2.
4. After entering all information, create the channel.

## 4. Account Settings

1. In the **[LINE Official Account Manager](https://manager.line.biz/)**, select the created account and go to the "Settings" screen.
2. **Messaging API Settings:**
    - Select the "Messaging API" tab.
    - Click "Enable Messaging API".
    - Select the provider to use.
3. **Account Settings:**
    - Go to the "Account settings" tab.
    - **Joining group chats:** Change to "Allow bot to be invited to group and multi-person chats".
4. **Response Settings:**
    - Go to the "Response settings" tab.
    - **Greeting message:** Set to OFF.
    - **Auto-response messages:** Set to OFF.

## 5. Enable and Configure Messaging API

1. Access the **[Line Developer Console](https://developers.line.biz/console/)** and select the target provider.
2. Confirm that the created channel is registered with the provider.
3. **Webhook Settings:**
    - Open the "Webhook settings" tab.
    - **Webhook URL:** Enter your application's webhook endpoint URL.
    - Click the "Verify" button and confirm that a 200 status code is returned.
    - Enable "Use webhook".
4. **Channel Access Token:**
    - Go to the "Channel access token" section.
    - Click the "Issue" button to issue a new access token.
