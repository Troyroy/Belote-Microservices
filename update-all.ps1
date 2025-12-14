# update-all.ps1
Write-Host "Updating all microservices..." -ForegroundColor Green

& minikube -p minikube docker-env --shell powershell | Invoke-Expression

$services = @(
    @{name="users-microservice"; dockerfile="microservices/users-microservice/Dockerfile"},
    @{name="games-microservice"; dockerfile="microservices/games-microservice/Dockerfile"},
    @{name="lobby-microservice"; dockerfile="microservices/lobby-microservice/Dockerfile"},
    @{name="api-gateway"; dockerfile="apigateway/Dockerfile"}
)

foreach ($svc in $services) {
    Write-Host "`n========================================" -ForegroundColor Cyan
    Write-Host "Building $($svc.name)..." -ForegroundColor Yellow
    Write-Host "========================================" -ForegroundColor Cyan

    docker build -t "$($svc.name):latest" -f $svc.dockerfile .

    if ($LASTEXITCODE -eq 0) {
        Write-Host "[OK] Build successful" -ForegroundColor Green
    } else {
        Write-Host "[FAILED] Build failed for $($svc.name)" -ForegroundColor Red
        exit 1
    }
}

Write-Host "`n========================================" -ForegroundColor Cyan
Write-Host "Restarting all deployments..." -ForegroundColor Yellow
Write-Host "========================================" -ForegroundColor Cyan

kubectl rollout restart deployment -n microservices

Write-Host "`nWaiting for all deployments..." -ForegroundColor Yellow
kubectl rollout status deployment/users-microservice -n microservices
kubectl rollout status deployment/games-microservice -n microservices
kubectl rollout status deployment/lobby-microservice -n microservices
kubectl rollout status deployment/api-gateway -n microservices

Write-Host "`n[SUCCESS] All services updated!" -ForegroundColor Green
kubectl get pods -n microservices