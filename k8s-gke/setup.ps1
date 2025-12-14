# GKE Deployment Script for Belote Microservices (PowerShell)
# Version: Using Aiven Databases + Keycloak

$ErrorActionPreference = "Stop"

Write-Host "========================================" -ForegroundColor Green
Write-Host "Belote Microservices GKE Deployment" -ForegroundColor Green
Write-Host "Using Aiven Databases + Keycloak" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# Check if PROJECT_ID is set
if (-not $env:PROJECT_ID) {
    Write-Host "ERROR: PROJECT_ID environment variable is not set" -ForegroundColor Red
    Write-Host "Please set it with: `$env:PROJECT_ID='your-gcp-project-id'" -ForegroundColor Yellow
    exit 1
}

Write-Host "Using PROJECT_ID: $env:PROJECT_ID" -ForegroundColor Yellow
Write-Host ""

# Function to wait for pods to be ready
function Wait-ForPods {
    param(
        [string]$Namespace,
        [string]$Label
    )
    Write-Host "Waiting for pods with label $Label to be ready..." -ForegroundColor Yellow
    kubectl wait --for=condition=ready pod -l $Label -n $Namespace --timeout=300s
}

# Step 1: Create namespace
Write-Host "Step 1: Creating namespace..." -ForegroundColor Green
kubectl apply -f 01-namespace.yaml
Write-Host ""

# Step 2: Create secrets (using Aiven credentials)
Write-Host "Step 2: Creating secrets with Aiven database credentials..." -ForegroundColor Green
Write-Host "⚠ Make sure you've updated 02-secrets-aiven.yaml with your Aiven credentials!" -ForegroundColor Yellow
Start-Sleep -Seconds 2
kubectl apply -f 02-secrets-aiven.yaml
Write-Host ""

# Step 3: Deploy infrastructure (Redis, RabbitMQ, Keycloak)
Write-Host "Step 3: Deploying infrastructure (Redis, RabbitMQ, Keycloak)..." -ForegroundColor Green
Write-Host "Note: Skipping database deployments - using Aiven hosted databases" -ForegroundColor Cyan
kubectl apply -f 03-redis.yaml
kubectl apply -f 04-rabbitmq.yaml
kubectl apply -f 13-keycloak.yaml
Write-Host ""

# Wait for infrastructure to be ready
Write-Host "Waiting for infrastructure to be ready (Keycloak may take 2-3 minutes)..." -ForegroundColor Yellow
Start-Sleep -Seconds 30
Wait-ForPods -Namespace "microservices" -Label "app=redis"
Wait-ForPods -Namespace "microservices" -Label "app=rabbitmq"
Write-Host "Waiting for Keycloak (this can take up to 3 minutes)..." -ForegroundColor Yellow
Wait-ForPods -Namespace "microservices" -Label "app=keycloak"
Write-Host "Infrastructure is ready!" -ForegroundColor Green
Write-Host ""

# Get Keycloak URL
Write-Host "Keycloak deployed with ClusterIP (internal only)" -ForegroundColor Cyan
Write-Host "Users Microservice will access Keycloak at: http://keycloak:8080" -ForegroundColor Yellow
Write-Host ""
Write-Host "To access Keycloak Admin UI for configuration:" -ForegroundColor Yellow
Write-Host "  kubectl port-forward -n microservices svc/keycloak 8180:8080" -ForegroundColor Cyan
Write-Host "  Then open: http://localhost:8180/admin" -ForegroundColor Cyan
Write-Host "  Username: admin" -ForegroundColor Yellow
Write-Host "  Password: admin123" -ForegroundColor Yellow
Write-Host ""
Write-Host "⚠ IMPORTANT: Configure Keycloak before deploying microservices!" -ForegroundColor Yellow
Write-Host "1. Port-forward to Keycloak (command above)" -ForegroundColor Cyan
Write-Host "2. Create realm: belote-game (or import your realm config)" -ForegroundColor Cyan
Write-Host "3. Create client: belote-backend" -ForegroundColor Cyan
Write-Host "4. Copy client secret to 02-secrets-aiven.yaml" -ForegroundColor Cyan
Write-Host "5. Create users" -ForegroundColor Cyan
Write-Host ""
$response = Read-Host "Have you configured Keycloak? Continue with deployment? (y/n)"
if ($response -ne 'y' -and $response -ne 'Y') {
    Write-Host "Deployment paused. Configure Keycloak and run this script again." -ForegroundColor Yellow
    Write-Host ""
    Write-Host "Quick setup:" -ForegroundColor Cyan
    Write-Host "  1. Open new terminal: kubectl port-forward -n microservices svc/keycloak 8180:8080" -ForegroundColor Yellow
    Write-Host "  2. Open browser: http://localhost:8180/admin" -ForegroundColor Yellow
    Write-Host "  3. Login: admin / admin123" -ForegroundColor Yellow
    Write-Host "  4. Configure realm and client" -ForegroundColor Yellow
    Write-Host "  5. Come back and continue" -ForegroundColor Yellow
    exit 0
}
Write-Host ""

# Step 4: Deploy microservices (they will connect to Aiven databases and Keycloak)
Write-Host "Step 4: Deploying microservices..." -ForegroundColor Green
Write-Host "Note: Microservices will connect to Aiven databases and Keycloak" -ForegroundColor Cyan
kubectl apply -f 07-users-microservice.yaml
kubectl apply -f 08-games-microservice.yaml
kubectl apply -f 09-lobby-microservice.yaml
Write-Host ""

# Wait for microservices to be ready
Write-Host "Waiting for microservices to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 20
Wait-ForPods -Namespace "microservices" -Label "app=users-microservice"
Wait-ForPods -Namespace "microservices" -Label "app=games-microservice"
Wait-ForPods -Namespace "microservices" -Label "app=lobby-microservice"
Write-Host "Microservices are ready!" -ForegroundColor Green
Write-Host ""

# Step 5: Deploy API Gateway
Write-Host "Step 5: Deploying API Gateway..." -ForegroundColor Green
kubectl apply -f 10-api-gateway.yaml
Write-Host ""

# Wait for API Gateway to be ready
Write-Host "Waiting for API Gateway to be ready..." -ForegroundColor Yellow
Start-Sleep -Seconds 15
Wait-ForPods -Namespace "microservices" -Label "app=api-gateway"
Write-Host "API Gateway is ready!" -ForegroundColor Green
Write-Host ""

# Step 6: Deploy monitoring (optional)
$response = Read-Host "Do you want to deploy Prometheus and Grafana? (y/n)"
if ($response -eq 'y' -or $response -eq 'Y') {
    Write-Host "Step 6: Deploying monitoring..." -ForegroundColor Green
    kubectl apply -f 11-prometheus.yaml
    kubectl apply -f 12-grafana.yaml
    Write-Host ""

    Write-Host "Waiting for monitoring to be ready..." -ForegroundColor Yellow
    Start-Sleep -Seconds 15
    Wait-ForPods -Namespace "microservices" -Label "app=prometheus"
    Wait-ForPods -Namespace "microservices" -Label "app=grafana"
    Write-Host "Monitoring is ready!" -ForegroundColor Green
    Write-Host ""
}

# Display deployment status
Write-Host "========================================" -ForegroundColor Green
Write-Host "Deployment Summary" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

Write-Host "All Pods:" -ForegroundColor Yellow
kubectl get pods -n microservices
Write-Host ""

Write-Host "All Services:" -ForegroundColor Yellow
kubectl get svc -n microservices
Write-Host ""

# Check for database connection errors
Write-Host "Checking for database connection issues..." -ForegroundColor Yellow
Write-Host ""

$usersPod = kubectl get pods -n microservices -l app=users-microservice -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($usersPod) {
    $usersErrors = kubectl logs $usersPod -n microservices --tail=50 2>$null | Select-String -Pattern "error|exception|failed" -CaseSensitive:$false
    if ($usersErrors) {
        Write-Host "⚠ Users microservice has some errors. Check logs:" -ForegroundColor Yellow
        Write-Host "  kubectl logs $usersPod -n microservices" -ForegroundColor Cyan
    } else {
        Write-Host "✓ Users microservice logs look good!" -ForegroundColor Green
    }
}

$gamesPod = kubectl get pods -n microservices -l app=games-microservice -o jsonpath='{.items[0].metadata.name}' 2>$null
if ($gamesPod) {
    $gamesErrors = kubectl logs $gamesPod -n microservices --tail=50 2>$null | Select-String -Pattern "error|exception|failed" -CaseSensitive:$false
    if ($gamesErrors) {
        Write-Host "⚠ Games microservice has some errors. Check logs:" -ForegroundColor Yellow
        Write-Host "  kubectl logs $gamesPod -n microservices" -ForegroundColor Cyan
    } else {
        Write-Host "✓ Games microservice logs look good!" -ForegroundColor Green
    }
}
Write-Host ""

# Get API Gateway IP
Write-Host "Waiting for API Gateway External IP (this may take a minute)..." -ForegroundColor Yellow
$GATEWAY_IP = $null
for ($i = 1; $i -le 30; $i++) {
    $GATEWAY_IP = kubectl get svc api-gateway -n microservices -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>$null
    if ($GATEWAY_IP) {
        break
    }
    Start-Sleep -Seconds 2
}

if ($GATEWAY_IP) {
    Write-Host "✓ API Gateway is accessible at: http://$GATEWAY_IP:8080" -ForegroundColor Green
    Write-Host "Test with: curl http://$GATEWAY_IP:8080/actuator/health" -ForegroundColor Yellow
} else {
    Write-Host "⚠ API Gateway IP not yet assigned. Check with: kubectl get svc api-gateway -n microservices" -ForegroundColor Yellow
}
Write-Host ""

# Get Grafana IP if deployed
if ($response -eq 'y' -or $response -eq 'Y') {
    $GRAFANA_IP = kubectl get svc grafana -n microservices -o jsonpath='{.status.loadBalancer.ingress[0].ip}' 2>$null
    if ($GRAFANA_IP) {
        Write-Host "✓ Grafana is accessible at: http://$GRAFANA_IP:3000" -ForegroundColor Green
        Write-Host "  Username: admin" -ForegroundColor Yellow
        Write-Host "  Password: admin123" -ForegroundColor Yellow
    }
    Write-Host ""
}

Write-Host "========================================" -ForegroundColor Green
Write-Host "Deployment Complete! 🚀" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "Infrastructure Used:" -ForegroundColor Cyan
Write-Host "✓ Aiven MySQL (Games Database)" -ForegroundColor Green
Write-Host "✓ Aiven PostgreSQL (Users Database + Keycloak)" -ForegroundColor Green
Write-Host "✓ GKE Keycloak (Authentication)" -ForegroundColor Green
Write-Host "✓ GKE Redis (In-cluster)" -ForegroundColor Green
Write-Host "✓ GKE RabbitMQ (In-cluster)" -ForegroundColor Green
Write-Host ""
Write-Host "Access Points:" -ForegroundColor Cyan
Write-Host "Keycloak (Admin): kubectl port-forward -n microservices svc/keycloak 8180:8080" -ForegroundColor Yellow
Write-Host "                  Then: http://localhost:8180/admin" -ForegroundColor Yellow
if ($GATEWAY_IP) {
    Write-Host "API Gateway: http://$GATEWAY_IP:8080" -ForegroundColor Yellow
}
if ($GRAFANA_IP) {
    Write-Host "Grafana: http://$GRAFANA_IP:3000" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "Next Steps:" -ForegroundColor Yellow
Write-Host "1. Verify Aiven IP whitelist includes your GKE cluster"
Write-Host "2. Verify Keycloak realm and client configuration"
Write-Host "3. Test authentication flow"
Write-Host "4. Test your API endpoints"
Write-Host "5. Set up custom domain and SSL/TLS"
Write-Host ""
Write-Host "Useful Commands:" -ForegroundColor Yellow
Write-Host "- View logs: kubectl logs -f POD_NAME -n microservices"
Write-Host "- Check pods: kubectl get pods -n microservices"
Write-Host "- Port forward: kubectl port-forward -n microservices svc/SERVICE_NAME LOCAL_PORT:REMOTE_PORT"
Write-Host "- Keycloak logs: kubectl logs -f deployment/keycloak -n microservices"
Write-Host ""
