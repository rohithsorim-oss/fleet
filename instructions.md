# Cloud Build to Cloud Run Deployment Instructions

This document provides step-by-step instructions to configure GCP Cloud Build for automatic deployment to Cloud Run on every commit to any branch.

## Prerequisites

- Google Cloud Project with billing enabled
- gcloud CLI installed and authenticated
- Owner or Editor role on the GCP project
- Source code repository (GitHub, Bitbucket, or Cloud Source Repositories)

---

## Step 1: Enable Required GCP APIs

Run the following commands in your terminal:

```bash
# Replace YOUR_PROJECT_ID with your actual GCP project ID
export PROJECT_ID="YOUR_PROJECT_ID"

gcloud config set project $PROJECT_ID

# Enable Cloud Build API
gcloud services enable cloudbuild.googleapis.com

# Enable Cloud Run API
gcloud services enable run.googleapis.com

# Enable Artifact Registry API
gcloud services enable artifactregistry.googleapis.com

# Enable Cloud SQL Admin API (for database connection)
gcloud services enable sqladmin.googleapis.com

# Enable Secret Manager API
gcloud services enable secretmanager.googleapis.com
```

---

## Step 2: Create Cloud Storage Bucket for Build Logs

Create a bucket to store Cloud Build logs:

```bash
gsutil mb -p $PROJECT_ID -l us-central1 gs://$PROJECT_ID-cloudbuild-logs
```

## Step 3: Create Artifact Registry Repository

Create a Docker repository in Artifact Registry to store your container images:

```bash
gcloud artifacts repositories create fleet-repo \
    --repository-format=docker \
    --location=us-central1 \
    --description="Fleet Management Docker repository"
```

---

## Step 4: Set Up Cloud Build Permissions

Grant Cloud Build service account permission to deploy to Cloud Run and access Artifact Registry:

```bash
# Get the Cloud Build service account email
CLOUD_BUILD_SA=$(gcloud projects get-iam-policy $PROJECT_ID \
    --filter="(bindings.role:roles/cloudbuild.builds.builder)" \
    --flatten="bindings[].members" \
    --format="value(bindings.members)" | grep -o "serviceAccount:[^,]*" | cut -d: -f2)

echo "Cloud Build Service Account: $CLOUD_BUILD_SA"

# Grant Cloud Run Admin role
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$CLOUD_BUILD_SA" \
    --role="roles/run.admin"

# Grant Service Account User role (to deploy Cloud Run services)
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$CLOUD_BUILD_SA" \
    --role="roles/iam.serviceAccountUser"

# Grant Artifact Registry Reader role
gcloud artifacts repositories add-iam-policy-binding fleet-repo \
    --location=us-central1 \
    --member="serviceAccount:$CLOUD_BUILD_SA" \
    --role="roles/artifactregistry.reader"

# Grant Cloud SQL Client role (for database connection)
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$CLOUD_BUILD_SA" \
    --role="roles/cloudsql.client"

# Grant Secret Manager Secret Accessor role
gcloud projects add-iam-policy-binding $PROJECT_ID \
    --member="serviceAccount:$CLOUD_BUILD_SA" \
    --role="roles/secretmanager.secretAccessor"
```

---

## Step 5: Create Secrets in Secret Manager

Store sensitive configuration as secrets:

```bash
# Database URL (Cloud SQL connection string)
# Format: jdbc:postgresql://google/<DATABASE_NAME>?cloudSqlInstance=<PROJECT_ID>:<REGION>:<INSTANCE_NAME>&socketFactory=com.google.cloud.sql.postgres.SocketFactory
gcloud secrets create fleet-db-url --data-file=- <<< "jdbc:postgresql://google/fleet?cloudSqlInstance=$PROJECT_ID:us-central1:fleet-instance&socketFactory=com.google.cloud.sql.postgres.SocketFactory"

# Database username
gcloud secrets create fleet-db-username --data-file=- <<< "your_db_username"

# Database password
gcloud secrets create fleet-db-password --data-file=- <<< "your_db_password"

# JWT Secret
gcloud secrets create fleet-jwt-secret --data-file=- <<< "your_jwt_secret_key_here"
```

---

## Step 6: Create Cloud SQL Instance (if not already created)

```bash
# Create PostgreSQL instance
gcloud sql instances create fleet-instance \
    --database-version=POSTGRES_15 \
    --tier=db-f1-micro \
    --region=us-central1 \
    --storage-auto-increase \
    --storage-size=10GB

# Set root password
gcloud sql users set-password postgres \
    --instance=fleet-instance \
    --password="your_root_password"

# Create the fleet database
gcloud sql databases create fleet --instance=fleet-instance

# Create a dedicated user for the application
gcloud sql users create fleet_user \
    --instance=fleet-instance \
    --password="your_db_password"
```

---

## Step 7: Configure Cloud Build Trigger

### Option A: Using Google Cloud Console

1. Go to **Cloud Build** > **Triggers** in the Google Cloud Console
2. Click **Create Trigger**
3. Configure the trigger:
   - **Name**: `fleet-deploy-trigger`
   - **Event**: `Push to a branch`
   - **Source**: Connect your GitHub/Bitbucket repository
   - **Branch**: `.*` (regex to match all branches)
   - **Configuration**: `Cloud Build configuration file (yaml or json)`
   - **Location**: `Repository` > Select your repo > `cloudbuild.yaml`
4. Click **Create**

### Option B: Using gcloud CLI

```bash
# First, connect your repository to Cloud Build
# This step varies based on your Git provider (GitHub, Bitbucket, etc.)

# Example for GitHub (requires Cloud Build GitHub app installation)
gcloud builds triggers create github \
    --name="fleet-deploy-trigger" \
    --repo-name="your-org/fleetmanagement" \
    --repo-owner="your-org" \
    --branch-pattern=".*" \
    --build-config="cloudbuild.yaml" \
    --substitutions=_REGION="us-central1",_SERVICE_NAME="fleet-app",_REPO_NAME="fleet-repo"
```

---

## Step 8: Application Configuration Changes

### Add GCP-specific application properties

Create or update `src/main/resources/application-gcp.properties`:

```properties
# Server Configuration
server.port=${SERVER_PORT:8080}

# Database Configuration (Cloud SQL)
spring.datasource.url=${DB_URL}
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=org.postgresql

# JPA/Hibernate Configuration
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=false
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# HikariCP Connection Pool
spring.datasource.hikari.connection-timeout=30000
spring.datasource.hikari.maximum-pool-size=10

# JWT Configuration
jwt.secret=${JWT_SECRET}
jwt.expiration=${JWT_EXPIRATION_MS:86400000}

# Actuator Configuration
management.endpoints.web.exposure.include=health,info,metrics
management.endpoint.health.show-details=when-authorized
```

### Update Dockerfile (if needed)

The current Dockerfile is already configured for Cloud Run with:
- Health check endpoint at `/actuator/health`
- Port 8080 exposure
- Non-root user for security

No changes needed to the Dockerfile.

### Important: No key.json Required

For Cloud Run deployment, **do not use key.json files**. Instead:
- Cloud Run uses IAM-based authentication automatically
- The Cloud SQL socket factory (`com.google.cloud.sql.postgres.SocketFactory`) uses the instance's service account
- Ensure the Cloud Run service account has the `cloudsql.client` IAM role (granted in Step 3)
- Database credentials are stored in Secret Manager (DB_USERNAME, DB_PASSWORD)

---

## Step 9: Verify Deployment

After pushing a commit, verify the deployment:

```bash
# List Cloud Run services
gcloud run services list --region=us-central1

# Get service URL
gcloud run services describe fleet-app --region=us-central1 --format="value(status.url)"

# View logs
gcloud run logs read fleet-app --region=us-central1 --limit=50

# View Cloud Build logs
gcloud builds list --limit=5
gcloud builds log [BUILD_ID]
```

---

## Step 10: Configure Branch-Specific Deployments (Optional)

If you want different deployments for different branches (e.g., dev, staging, prod), modify `cloudbuild.yaml` to use branch-specific service names:

```yaml
# In cloudbuild.yaml, replace the deploy step with:
- name: 'gcr.io/cloud-builders/gcloud'
    entrypoint: 'bash'
    args:
      - '-c'
      - |
        if [ "$BRANCH_NAME" = "main" ] || [ "$BRANCH_NAME" = "master" ]; then
          SERVICE_NAME="fleet-app-prod"
        elif [ "$BRANCH_NAME" = "develop" ]; then
          SERVICE_NAME="fleet-app-staging"
        else
          SERVICE_NAME="fleet-app-$BRANCH_NAME"
        fi
        gcloud run deploy $SERVICE_NAME \
          --image=us-central1-docker.pkg.dev/$PROJECT_ID/fleet-repo/fleet-app:$COMMIT_SHA \
          --region=us-central1 \
          --platform=managed \
          --allow-unauthenticated \
          --port=8080 \
          --cpu=1 \
          --memory=512Mi \
          --max-instances=100 \
          --min-instances=0 \
          --timeout=300s \
          --set-env-vars=SPRING_PROFILES_ACTIVE=gcp \
          --set-secrets=DB_URL=fleet-db-url:latest,DB_USERNAME=fleet-db-username:latest,DB_PASSWORD=fleet-db-password:latest,JWT_SECRET=fleet-jwt-secret:latest
```

---

## Step 11: Set Up Monitoring and Alerts (Optional)

```bash
# Create a log-based alert for deployment failures
gcloud logging sinks create fleet-build-failures \
    bigquery.googleapis.com/projects/$PROJECT_ID/datasets/fleet_logs \
    --log-filter='resource.type="build" AND severity="ERROR"'

# Enable Cloud Monitoring
gcloud services enable monitoring.googleapis.com
```

---

## Troubleshooting

### Build fails with permission denied
Ensure Cloud Build service account has the required IAM roles (Step 3).

### Database connection fails
- Verify Cloud SQL instance is running
- Check that the connection string format is correct
- Ensure the Cloud Build service account has `cloudsql.client` role
- Verify the secret values are correct

### Deployment succeeds but app crashes
- Check Cloud Run logs: `gcloud run logs read fleet-app --region=us-central1`
- Verify all required secrets are set
- Ensure the application can connect to Cloud SQL

### Trigger not firing
- Verify the webhook is properly configured in your Git provider
- Check Cloud Build trigger settings for branch pattern
- Ensure the repository connection is active

---

## Cost Optimization Tips

1. **Set minimum instances to 0** for non-production environments to save costs
2. **Use appropriate CPU/memory allocation** - start with 1 CPU / 512Mi and scale based on usage
3. **Enable Cloud Run autoscaling** - the configuration already includes this
4. **Set up budget alerts** in GCP Billing to monitor costs

---

## Security Best Practices

1. **Never commit secrets** to the repository - use Secret Manager
2. **Use least privilege IAM roles** - grant only necessary permissions
3. **Enable VPC Service Controls** for sensitive data
4. **Regularly rotate secrets** - especially JWT secrets and database passwords
5. **Use Cloud Armor** for DDoS protection on production deployments
6. **Enable binary authorization** to ensure only trusted images are deployed

---

## Next Steps

1. Complete all GCP setup steps above
2. Push a commit to your repository to trigger the first deployment
3. Verify the deployment in Cloud Run console
4. Test the application endpoints
5. Set up monitoring and alerting for production use
