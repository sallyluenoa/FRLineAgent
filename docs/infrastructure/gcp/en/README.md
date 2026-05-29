# Google Cloud Platform (GCP) Setup Guide

This document outlines the infrastructure setup procedures required to run this project on Google Cloud Platform.

## Table of Contents
1.  [Initial Setup](#1-initial-setup)
    -   [Registering a Billing Account](#registering-a-billing-account)
    -   [Creating a New Project](#creating-a-new-project)
2.  [API Enablement and Secret Management](#2-api-enablement-and-secret-management)
    -   [Enabling APIs](#enabling-apis)
    -   [Creating Secrets](#creating-secrets)
3.  [Service Accounts and Authentication](#3-service-accounts-and-authentication)
    -   [Creating a Service Account](#creating-a-service-account)
    -   [Issuing a JSON Key (for Initial Setup)](#issuing-a-json-key-for-initial-setup)
    -   [Storing the Downloaded JSON Key in Secret Manager](#storing-the-downloaded-json-key-in-secret-manager)
    -   [(Optional) Granting Access to Google Sheets](#optional-granting-access-to-google-sheets)
4.  [Integrating GitHub Actions and GCP (Workload Identity)](#4-integrating-github-actions-and-gcp-workload-identity)
    -   [Launching Cloud Shell and Setting Environment Variables](#launching-cloud-shell-and-setting-environment-variables)
    -   [Creating a Workload Identity Pool and Provider](#creating-a-workload-identity-pool-and-provider)
    -   [Configuring Service Account Permissions](#configuring-service-account-permissions)
    -   [Confirming Values for GitHub Actions Configuration](#confirming-values-for-github-actions-configuration)
5.  [Container Image Storage (Artifact Registry)](#5-container-image-storage-artifact-registry)
    -   [Creating an Artifact Registry Repository](#creating-an-artifact-registry-repository)
    -   [Granting Additional Permissions to the Service Account](#granting-additional-permissions-to-the-service-account)
6.  [Final Configuration Checks](#6-final-configuration-checks)
    -   [Verifying the Artifact Registry Repository](#verifying-the-artifact-registry-repository)
    -   [Verifying Service Account Roles (Project-Level)](#verifying-service-account-roles-project-level)
    -   [Verifying Service Account Roles (On the Service Account Itself)](#verifying-service-account-roles-on-the-service-account-itself)
    -   [Verifying Workload Identity Pool Status](#verifying-workload-identity-pool-status)
7.  [Post-Deployment Configuration](#7-post-deployment-configuration)
    -   [Allowing Public Access](#71-allowing-public-access)
    -   [Updating the Service Account](#72-updating-the-service-account)

---

## 1. Initial Setup

### Registering a Billing Account
To use most Google Cloud APIs (including Secret Manager), you need to link an active billing account to your project.

1.  Go to the **[Google Cloud Console Billing Manager](https://console.cloud.google.com/billing)**.
2.  Click "Create billing account" and follow the instructions to register your credit card information.
    *   *Note: You may receive free credits if you are signing up for the first time.*

### Creating a New Project
1.  Click "New Project" from the project selection tool at the top of the screen.
2.  Enter a project name, select the billing account you just registered, and choose the parent organization to create the project.

---

## 2. API Enablement and Secret Management

Enable the necessary APIs for the project and use Secret Manager to securely store sensitive information like API keys.

### Enabling APIs
In the console's search bar, search for the following API names and click the "Enable" button for each.

-   **Secret Manager API**: Required for managing sensitive information.
-   **IAM Service Account Credentials API**: Required to impersonate service accounts.

### Creating Secrets
Register the sensitive information required by the application.

1.  Click "Create secret."
2.  **Name**: Enter a descriptive name, such as `LINE_CHANNEL_ACCESS_TOKEN`.
3.  **Secret value**: Paste the actual token string issued from the LINE Developers Console or other services.
4.  Click the "Create secret" button to save.
5.  Repeat this process for all necessary secrets, such as `LINE_CHANNEL_SECRET` and `GOOGLE_API_CREDENTIALS`.

---

## 3. Service Accounts and Authentication

Create a service account, which acts as an "identity certificate" for your program to access GCP resources.

### Creating a Service Account
1.  Go to **[IAM & Admin > Service Accounts](https://console.cloud.google.com/iam-admin/serviceaccounts)**.
2.  Click "**+ CREATE SERVICE ACCOUNT**."
3.  **Service account name**: Enter a unique and descriptive name (e.g., `frlineagent-app`).
4.  Click "**CREATE AND CONTINUE**." Roles (permissions) will be configured later, so you can proceed without selecting any here.
5.  Click "**DONE**" to finish creating the account.

### Issuing a JSON Key (for Initial Setup)
To access GCP from a local environment before a CI/CD pipeline is established, issue a temporary authentication key (in JSON format).

> **Warning:** Service account keys (JSON files) grant powerful access to your GCP resources. Handle them with extreme care, as a leak can have serious security implications. For long-term use, **Workload Identity** (described later) is strongly recommended.

#### a. Modifying Organization Policy (Allowing Key Creation)
By default, key creation may be disabled by security policies. You need to temporarily allow it.

1.  In the project selector at the top of the console, select the **Organization** that is the parent of your project.
2.  From the navigation menu, go to **[IAM & Admin] > [Organization Policies]**.
3.  In the filter box, enter `iam.disableServiceAccountKeyCreation` to find the policy.
4.  Click on the policy named "**Disable service account key creation**" and then select "Manage policy."
5.  Set the rule to "**Off**" (i.e., allow key creation) and save the policy.
    *   *Note: You need the "Organization Policy Administrator" role to change this policy. If you don't have the required permissions, ask your organization administrator or add the role to your account.*

#### b. Issuing the JSON Key
1.  Open the details page of the service account you just created and select the **[KEYS]** tab.
2.  Select **[ADD KEY] > [Create new key]**.
3.  Choose **[JSON]** as the key type and click [CREATE]. The key file will be downloaded automatically.

#### c. Restoring Organization Policy (Recommended)
Immediately after issuing the key, revert the organization policy to its original, secure state.

1.  Go back to the **[IAM & Admin] > [Organization Policies]** screen and search for `iam.disableServiceAccountKeyCreation` again.
2.  Click "Manage policy" and set the rule back to "**On**" (i.e., disable key creation) and save the policy.

### Storing the Downloaded JSON Key in Secret Manager
The content of the downloaded key file is also sensitive information and should be stored in Secret Manager.

1.  Go to the Secret Manager page and click "Create secret."
2.  **Name**: Give it a descriptive name, such as `GOOGLE_API_CREDENTIALS`.
3.  **Secret value**: **Copy and paste the entire content** of the downloaded JSON file.
4.  Save the secret.

> **CRITICAL:** Once you have stored the secret in Secret Manager, **immediately delete the original JSON file** from your local machine. Never commit it to a Git repository or any other version control system.

### (Optional) Granting Access to Google Sheets
If you need the service account to interact with Google Sheets, follow these steps:

1.  Open the issued JSON key file and copy the value of `client_email` (e.g., `your-service-account@your-project-id.iam.gserviceaccount.com`).
2.  Open the Google Sheet you want to grant access to and click the "**Share**" button in the top right.
3.  Paste the copied email address, grant it "**Editor**" permissions, and save.

---

## 4. Integrating GitHub Actions and GCP (Workload Identity)

This setup allows your GitHub Actions CI/CD workflow to securely access GCP without using static keys.

### Launching Cloud Shell and Setting Environment Variables
Launch Cloud Shell by clicking the "**>_**" icon in the top right of the GCP console and set the following environment variables.

```shell
# --- Modify the values below to match your environment ---
# GCP Project ID
export PROJECT_ID="your-project-id"
# GitHub Repository (e.g., "owner/repo")
export REPO="sallyluenoa/FRLineAgent"
# The name of the service account created earlier (not the email)
export SERVICE_NAME="frlineagent-app"
# --------------------------------------------------------

# The following variables are set automatically
export PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format='value(projectNumber)')
export SERVICE_ACCOUNT="${SERVICE_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

# Verify the settings
echo "PROJECT_ID: ${PROJECT_ID}"
echo "PROJECT_NUMBER: ${PROJECT_NUMBER}"
echo "SERVICE_ACCOUNT: ${SERVICE_ACCOUNT}"
echo "REPO: ${REPO}"
```

### Creating a Workload Identity Pool and Provider
Configure an endpoint to accept authentication requests from GitHub.

```shell
# Create a Workload Identity Pool
gcloud iam workload-identity-pools create "github-pool" 
    --project="${PROJECT_ID}" 
    --location="global" 
    --display-name="GitHub Actions Pool"

# Create a Workload Identity Provider (integrates with the GitHub repository)
gcloud iam workload-identity-pools providers create-oidc "github-provider" 
    --project="${PROJECT_ID}" 
    --location="global" 
    --workload-identity-pool="github-pool" 
    --display-name="GitHub Actions Provider" 
    --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" 
    --attribute-condition="attribute.repository == '${REPO}'" 
    --issuer-uri="https://token.actions.githubusercontent.com"
```

### Configuring Service Account Permissions
Link access from GitHub to be accepted as the specified service account.

```shell
# Bind access via Workload Identity to the service account
gcloud iam service-accounts add-iam-policy-binding "${SERVICE_ACCOUNT}" 
    --project="${PROJECT_ID}" 
    --role="roles/iam.workloadIdentityUser" 
    --member="principalSet://iam.googleapis.com/projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/github-pool/attribute.repository/${REPO}"

# Grant permissions required for deploying to Cloud Run
gcloud projects add-iam-policy-binding ${PROJECT_ID} 
    --member="serviceAccount:${SERVICE_ACCOUNT}" 
    --role="roles/run.admin"

# Grant write permissions to Artifact Registry
gcloud projects add-iam-policy-binding ${PROJECT_ID} 
    --member="serviceAccount:${SERVICE_ACCOUNT}" 
    --role="roles/artifactregistry.writer"

# Grant permission to assign the service account to the Cloud Run service during deployment
gcloud projects add-iam-policy-binding ${PROJECT_ID} 
    --member="serviceAccount:${SERVICE_ACCOUNT}" 
    --role="roles/iam.serviceAccountUser"

# Grant permission for the deployed Cloud Run service to act as its own service account
gcloud iam service-accounts add-iam-policy-binding "${SERVICE_ACCOUNT}" 
    --project="${PROJECT_ID}" 
    --member="serviceAccount:${SERVICE_ACCOUNT}" 
    --role="roles/iam.serviceAccountUser"
```

### Confirming Values for GitHub Actions Configuration
Run the following command and copy the output string. This value should be set as `GCP_WORKLOAD_IDENTITY_PROVIDER` in your GitHub repository's secrets.

```shell
gcloud iam workload-identity-pools providers describe "github-provider" 
    --project="${PROJECT_ID}" 
    --location="global" 
    --workload-identity-pool="github-pool" 
    --format='value(name)'
```

---

## 5. Container Image Storage (Artifact Registry)

Create a repository to store your built Docker images.

### Creating an Artifact Registry Repository
```shell
# --- Modify the values below to match your environment ---
# The name for your Artifact Registry repository
export ARTIFACT_REGISTRY="fr-line-agent"
# The region
export LOCATION="asia-northeast1"
# --------------------------------------------------------

gcloud artifacts repositories create ${ARTIFACT_REGISTRY} 
    --project=${PROJECT_ID} 
    --repository-format=docker 
    --location=${LOCATION} 
    --description="Docker repository for FRLineAgent"
```

### Granting Additional Permissions to the Service Account
Grant the permissions that the application needs at runtime to the service account.

```shell
# Permission to access secrets in Secret Manager
gcloud projects add-iam-policy-binding ${PROJECT_ID} 
    --member="serviceAccount:${SERVICE_ACCOUNT}" 
    --role="roles/secretmanager.secretAccessor"

# Permission to write logs to Cloud Logging
gcloud projects add-iam-policy-binding ${PROJECT_ID} 
    --member="serviceAccount:${SERVICE_ACCOUNT}" 
    --role="roles/logging.logWriter"

# Permission required for the Google Auth Action to get an OIDC token
gcloud iam service-accounts add-iam-policy-binding ${SERVICE_ACCOUNT} 
    --project=${PROJECT_ID} 
    --role="roles/iam.serviceAccountTokenCreator" 
    --member="serviceAccount:${SERVICE_ACCOUNT}"
```

---

## 6. Final Configuration Checks

### Verifying the Artifact Registry Repository
```shell
gcloud artifacts repositories list 
    --project=${PROJECT_ID} 
    --location=${LOCATION} 
    --filter="name~${ARTIFACT_REGISTRY}"
```
> **Expected Result:** The repository name you configured should be displayed, and its `FORMAT` should be `DOCKER`.

### Verifying Service Account Roles (Project-Level)
Verify the roles granted to the service account at the project level.

```shell
gcloud projects get-iam-policy ${PROJECT_ID} 
    --flatten="bindings[].members" 
    --format="table(bindings.role)" 
    --filter="bindings.members:serviceAccount:${SERVICE_ACCOUNT}"
```
> **Expected Result:** Verify that the following roles are displayed:
> - `roles/run.admin`
> - `roles/artifactregistry.writer`
> - `roles/secretmanager.secretAccessor`
> - `roles/logging.logWriter`
> - `roles/iam.serviceAccountUser`

### Verifying Service Account Roles (On the Service Account Itself)
Verify the roles granted to the service account on itself. This is necessary for the service account to impersonate itself or create tokens.

```shell
gcloud iam service-accounts get-iam-policy ${SERVICE_ACCOUNT} 
    --project=${PROJECT_ID} 
    --flatten="bindings[].members" 
    --format="table(bindings.role)" 
    --filter="bindings.members:serviceAccount:${SERVICE_ACCOUNT}"
```
> **Expected Result:** Verify that the following roles are displayed:
> - `roles/iam.serviceAccountTokenCreator`
> - `roles/iam.serviceAccountUser`

### Verifying Workload Identity Pool Status
```shell
gcloud iam workload-identity-pools providers list 
    --workload-identity-pool="github-pool" 
    --location="global" 
    --project=${PROJECT_ID}
```
> **Expected Result:** Verify that the `STATE` is `ACTIVE` and that the GitHub repository specified in `attributeCondition` is displayed.

---

## 7. Post-Deployment Configuration

These steps should be performed after the initial successful deployment via GitHub Actions.

### 7.1. Allowing Public Access
To accept webhook requests from the LINE Platform, you must allow unauthenticated access to your Cloud Run service.

-   **Reason**: LINE's servers do not have Google Cloud credentials. If public access is not allowed, webhook requests will be blocked, and your LINE Bot will not function.

#### Configuration Steps
1.  In the GCP console, go to **Cloud Run** and select your deployed service.
2.  Navigate to the **"Security"** tab.
3.  In the **"Authentication"** section, select **"Allow unauthenticated invocations"**.
4.  Click **"Save"**.

### 7.2. Updating the Service Account
Verify that the deployed Cloud Run service is running with the intended service account and update it if necessary.

-   **Reason**: By default, the service may run with an unintended service account (like the Compute Engine default service account), which may violate the principle of least privilege.

#### Configuration Steps
1.  In the GCP console, go to **Cloud Run** and select your deployed service.
2.  Navigate to the **"Revisions"** tab and check the **"Security"** details of the latest revision.
3.  Verify that the **"Service account"** is the one created in this guide.
4.  If a different service account is configured, run the following command in Cloud Shell to update it.

```shell
gcloud run services update ${ARTIFACT_REGISTRY} 
    --region=${LOCATION} 
    --service-account="${SERVICE_ACCOUNT}" 
    --project=${PROJECT_ID}
```
> **Note:** After running the command, a new revision will be created. Verify in the console that the service account has been correctly updated.
