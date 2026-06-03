param(
    [string]$Service = "",
    [int]$Tail = 200
)

if ($Service) {
    docker compose --env-file .env logs -f --tail $Tail $Service
}
else {
    docker compose --env-file .env logs -f --tail $Tail
}
