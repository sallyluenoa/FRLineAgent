# Google Cloud Platform (GCP) 構築手順書

このドキュメントは、本プロジェクトをGoogle Cloud Platform上で動作させるために必要なインフラ構築手順をまとめたものです。

## 目次
1.  [初期設定](#1-初期設定)
    -   [請求先アカウントの登録](#請求先アカウントの登録)
    -   [プロジェクトの新規作成](#プロジェクトの新規作成)
2.  [APIの有効化とシークレット管理](#2-apiの有効化とシークレット管理)
    -   [各種APIの有効化](#各種apiの有効化)
    -   [シークレットの作成](#シークレットの作成)
3.  [サービスアカウントと認証](#3-サービスアカウントと認証)
    -   [サービスアカウントの作成](#サービスアカウントの作成)
    -   [JSONキーの発行（初回セットアップ用）](#jsonキーの発行初回セットアップ用)
    -   [ダウンロードしたJSONキーをSecret Managerへ保管](#ダウンロードしたjsonキーをsecret-managerへ保管)
    -   [（オプション）Googleスプレッドシートへのアクセス許可](#オプションgoogleスプレッドシートへのアクセス許可)
4.  [GitHub ActionsとGCPの連携 (Workload Identity)](#4-github-actionsとgcpの連携-workload-identity)
    -   [Cloud Shellの起動と環境変数の設定](#cloud-shellの起動と環境変数の設定)
    -   [Workload Identityプールとプロバイダの作成](#workload-identityプールとプロバイダの作成)
    -   [サービスアカウントの権限設定](#サービスアカウントの権限設定)
    -   [GitHub Actionsに必要な設定値の確認](#github-actionsに必要な設定値の確認)
5.  [コンテナイメージの保管場所 (Artifact Registry)](#5-コンテナイメージの保管場所-artifact-registry)
    -   [Artifact Registryリポジトリの作成](#artifact-registryリポジトリの作成)
    -   [サービスアカウントへの追加権限付与](#サービスアカウントへの追加権限付与)
6.  [設定の最終確認](#6-設定の最終確認)
    -   [Artifact Registryリポジトリの確認](#artifact-registryリポジトリの確認)
    -   [サービスアカウントのロール確認](#サービスアカウントのロール確認)
    -   [Workload Identityプールの状態確認](#workload-identityプールの状態確認)

---

## 1. 初期設定

### 請求先アカウントの登録
Google Cloudの多くのAPIを利用するには、プロジェクトに有効な請求先アカウントを紐付ける必要があります。

1.  **[Google Cloud Consoleのお支払いマネージャ](https://console.cloud.google.com/billing)** へアクセスします。
2.  「請求先アカウントを作成」をクリックし、画面の指示に従ってクレジットカード情報などを登録します。
    *   *Note: 初めて登録する場合、無料クレジットが付与されることがあります。*

### プロジェクトの新規作成
1.  画面上部のプロジェクト選択ツールから「新しいプロジェクト」をクリックします。
2.  プロジェクト名を入力し、先ほど登録した請求先アカウントと、所属する組織を選択してプロジェクトを作成します。

---

## 2. APIの有効化とシークレット管理

プロジェクトで利用する各種APIを有効化し、APIキーなどの機密情報をSecret Managerで安全に管理します。

### 各種APIの有効化
コンソールの検索窓で以下のAPI名を検索し、それぞれ「有効にする」ボタンをクリックします。

-   **Secret Manager API**: 機密情報を管理するために必要です。
-   **IAM Service Account Credentials API**: サービスアカウントの権限を借用するために必要です。

### シークレットの作成
アプリケーションで必要となる機密情報を登録します。

1.  Secret Managerの画面を開き、「シークレットを作成」をクリックします。
2.  **名前**: `LINE_CHANNEL_ACCESS_TOKEN` のように、一目で内容がわかる名前を入力します。
3.  **シークレットの値**: LINE Developersコンソールなどで発行した実際のトークン文字列を貼り付けます。
4.  「シークレットを作成」ボタンを押して保存します。
5.  この手順を、`LINE_CHANNEL_SECRET` や `GOOGLE_API_CREDENTIALS` など、必要な情報の数だけ繰り返します。

---

## 3. サービスアカウントと認証

プログラムがGCPリソースへアクセスする際の「身分証明書」となるサービスアカウントを作成します。

### サービスアカウントの作成
1.  **[IAMと管理 > サービスアカウント](https://console.cloud.google.com/iam-admin/serviceaccounts)** を開きます。
2.  「**+ サービスアカウントを作成**」をクリックします。
3.  **サービスアカウント名**: 用途がわかる一意の名前（例: `frlineagent-app`）を入力します。
4.  「**作成して続行**」をクリックします。ロール（権限）は後ほどまとめて設定するため、ここでは何も選択せずに次に進みます。
5.  「**完了**」をクリックして作成を完了します。

### JSONキーの発行（初回セットアップ用）
CI/CD環境が整う前に、ローカル環境などからGCPへアクセスするために一時的な認証キー（JSON形式）を発行します。

> **Warning:** サービスアカウントキー（JSONファイル）は、漏洩するとGCPリソースへの強力なアクセス権を第三者に与えてしまうため、取り扱いには最大限の注意が必要です。恒久的な運用では、後述する**Workload Identity**の使用を強く推奨します。

#### a. 組織ポリシーの変更（キー作成の許可）
デフォルトのセキュリティ設定ではキーの発行が禁止されている場合があるため、一時的に許可します。

1.  コンソール上部のプロジェクト選択で、プロジェクトの親にあたる「**組織**」を選択します。
2.  ナビゲーションメニューから **[IAMと管理] > [組織のポリシー]** を開きます。
3.  フィルタ欄に `iam.disableServiceAccountKeyCreation` と入力してポリシーを検索します。
4.  表示された「**サービス アカウント キーの作成を無効にする**」をクリックし、[ポリシーを管理] を選択します。
5.  ルールを「**オフ**」に設定し（＝キー作成を許可する）、ポリシーを保存します。
    *   *Note: ポリシーの変更には「組織ポリシー管理者」ロールが必要です。自身のIAMロールに必要な権限がない場合は、組織の管理者に依頼するか、権限を追加してください。*

#### b. JSONキーの発行
1.  先ほど作成したサービスアカウントの詳細画面を開き、[**キー**] タブを選択します。
2.  **[キーを追加] > [新しい鍵を作成]** を選択します。
3.  キーのタイプとして **[JSON]** を選択し、[作成] をクリックすると、キーファイルが自動的にダウンロードされます。

#### c. 組織ポリシーの復元（推奨）
キーの発行が完了したら、ただちに組織ポリシーを元の安全な状態に戻します。

1.  再度、**[IAMと管理] > [組織のポリシー]** 画面で `iam.disableServiceAccountKeyCreation` を検索します。
2.  [ポリシーを管理] をクリックし、ルールを「**オン**」に設定（＝キー作成を無効化する）してポリシーを保存します。

### ダウンロードしたJSONキーをSecret Managerへ保管
ダウンロードしたキーファイルの内容も機密情報としてSecret Managerで管理します。

1.  Secret Managerの画面で、「シークレットを作成」をクリックします。
2.  **名前**: `GOOGLE_API_CREDENTIALS` などの分かりやすい名前を付けます。
3.  **シークレットの値**: ダウンロードしたJSONファイルの内容を**すべてコピー＆ペースト**して貼り付けます。
4.  シークレットを保存します。

> **CRITICAL:** Secret Managerへの保管が完了したら、ローカルPC上にある元のJSONファイルは**直ちに削除**してください。Gitリポジトリなどにコミットすることは絶対に避けてください。

### （オプション）Googleスプレッドシートへのアクセス許可
サービスアカウントを利用してGoogleスプレッドシートを操作する場合、以下の設定が必要です。

1.  発行したJSONキーファイルを開き、`client_email` の値（例: `your-service-account@your-project-id.iam.gserviceaccount.com`）をコピーします。
2.  アクセスしたいGoogleスプレッドシートを開き、右上の「**共有**」ボタンをクリックします。
3.  コピーしたメールアドレスを共有先に追加し、「**編集者**」の権限を付与して保存します。

---

## 4. GitHub ActionsとGCPの連携 (Workload Identity)

GitHub ActionsのCI/CDワークフローから、静的なキーを使わずに安全にGCPへアクセスするための設定です。

### Cloud Shellの起動と環境変数の設定
GCPコンソール右上の「**>_**」アイコンをクリックしてCloud Shellを起動し、以下のコマンドで環境変数を設定します。

```shell
# 自分の環境に合わせて値を変更してください
# ----------------------------------------
# GCPプロジェクトID
export PROJECT_ID="your-project-id"
# GitHubリポジトリ (例: "owner/repo")
export REPO="sallyluenoa/FRLineAgent"
# 先ほど作成したサービスアカウント名 (メールアドレスではない)
export SERVICE_NAME="frlineagent-app"
# ----------------------------------------

# 以下の変数は自動的に設定されます
export PROJECT_NUMBER=$(gcloud projects describe $PROJECT_ID --format='value(projectNumber)')
export SERVICE_ACCOUNT="${SERVICE_NAME}@${PROJECT_ID}.iam.gserviceaccount.com"

# 設定内容の確認
echo "PROJECT_ID: ${PROJECT_ID}"
echo "PROJECT_NUMBER: ${PROJECT_NUMBER}"
echo "SERVICE_ACCOUNT: ${SERVICE_ACCOUNT}"
echo "REPO: ${REPO}"
```

### Workload Identityプールとプロバイダの作成
GitHubからの認証リクエストを受け付けるための窓口を設定します。

```shell
# Workload Identity プールの作成
gcloud iam workload-identity-pools create "github-pool" \
    --project="${PROJECT_ID}" \
    --location="global" \
    --display-name="GitHub Actions Pool"

# Workload Identity プロバイダの作成 (GitHubリポジトリとの連携)
gcloud iam workload-identity-pools providers create-oidc "github-provider" \
    --project="${PROJECT_ID}" \
    --location="global" \
    --workload-identity-pool="github-pool" \
    --display-name="GitHub Actions Provider" \
    --attribute-mapping="google.subject=assertion.sub,attribute.repository=assertion.repository" \
    --attribute-condition="attribute.repository == '${REPO}'" \
    --issuer-uri="https://token.actions.githubusercontent.com"
```

### サービスアカウントの権限設定
GitHubからのアクセスを、指定したサービスアカウントとして受け入れるように紐付けます。

```shell
# Workload Identity経由のアクセスをサービスアカウントに紐付ける
gcloud iam service-accounts add-iam-policy-binding "${SERVICE_ACCOUNT}" \
    --project="${PROJECT_ID}" \
    --role="roles/iam.workloadIdentityUser" \
    --member="principalSet://iam.googleapis.com/projects/${PROJECT_NUMBER}/locations/global/workloadIdentityPools/github-pool/attribute.repository/${REPO}"

# Cloud Runへのデプロイに必要な権限を付与
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/run.admin"

# Artifact Registryへの書き込み権限
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/artifactregistry.writer"

# デプロイ時に、Cloud Runサービスにサービスアカウントを割り当てる権限
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/iam.serviceAccountUser"

# デプロイしたCloud Runが自分自身のサービスアカウントとして動作するための権限
gcloud iam service-accounts add-iam-policy-binding "${SERVICE_ACCOUNT}" \
    --project="${PROJECT_ID}" \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/iam.serviceAccountUser"
```

### GitHub Actionsに必要な設定値の確認
以下のコマンドを実行し、表示された文字列をコピーします。この値は、GitHubリポジトリのSecretsに `GCP_WORKLOAD_IDENTITY_PROVIDER` として設定します。

```shell
gcloud iam workload-identity-pools providers describe "github-provider" \
    --project="${PROJECT_ID}" \
    --location="global" \
    --workload-identity-pool="github-pool" \
    --format='value(name)'
```

---

## 5. コンテナイメージの保管場所 (Artifact Registry)

ビルドしたDockerイメージを保管するためのリポジトリを作成します。

### Artifact Registryリポジトリの作成
```shell
# 自分の環境に合わせて値を変更してください
# ----------------------------------------
# Artifact Registryのリポジトリ名
export ARTIFACT_REGISTRY="fr-line-agent"
# リージョン
export LOCATION="asia-northeast1"
# ----------------------------------------

gcloud artifacts repositories create ${ARTIFACT_REGISTRY} \
    --project=${PROJECT_ID} \
    --repository-format=docker \
    --location=${LOCATION} \
    --description="Docker repository for FRLineAgent"
```

### サービスアカウントへの追加権限付与
アプリケーションが実行時に必要な権限をサービスアカウントに付与します。

```shell
# Secret Managerのシークレットにアクセスする権限
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/secretmanager.secretAccessor"

# Cloud Loggingへのログ書き込み権限
gcloud projects add-iam-policy-binding ${PROJECT_ID} \
    --member="serviceAccount:${SERVICE_ACCOUNT}" \
    --role="roles/logging.logWriter"

# Google Auth ActionがOIDCトークンを取得するために必要な権限
gcloud iam service-accounts add-iam-policy-binding ${SERVICE_ACCOUNT} \
    --project=${PROJECT_ID} \
    --role="roles/iam.serviceAccountTokenCreator" \
    --member="serviceAccount:${SERVICE_ACCOUNT}"
```

---

## 6. 設定の最終確認

### Artifact Registryリポジトリの確認
```shell
gcloud artifacts repositories list \
    --project=${PROJECT_ID} \
    --location=${LOCATION} \
    --filter="name~${ARTIFACT_REGISTRY}"
```
> **期待される結果:** 設定したリポジトリ名が表示され、`FORMAT`が`DOCKER`であることを確認します。

### サービスアカウントのロール確認
```shell
gcloud projects get-iam-policy ${PROJECT_ID} \
    --flatten="bindings[].members" \
    --format="table(bindings.role)" \
    --filter="bindings.members:serviceAccount:${SERVICE_ACCOUNT}"
```
> **期待される結果:** これまで付与した以下のロールが表示されることを確認します。
> - `roles/run.admin`
> - `roles/artifactregistry.writer`
> - `roles/secretmanager.secretAccessor`
> - `roles/logging.logWriter`
> - `roles/iam.serviceAccountUser`

### Workload Identityプールの状態確認
```shell
gcloud iam workload-identity-pools providers list \
    --workload-identity-pool="github-pool" \
    --location="global" \
    --project=${PROJECT_ID}
```
> **期待される結果:** `STATE`が`ACTIVE`であり、`attributeCondition`に設定したGitHubリポジトリが表示されていることを確認します。
