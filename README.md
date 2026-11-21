# Notifi

System do planowania i symulowanej wysyłki powiadomień (EMAIL, PUSH).
Spring Boot 4.1, PostgreSQL, RabbitMQ 4.1, Docker Compose.

## Architektura

```
POST /notifications ──▶ API ──▶ Postgres (PENDING)
                        │
                        └─ Dispatcher (co 5 s): PENDING → QUEUED ──▶ RabbitMQ
                                                                      ├─ email.queue ──▶ email-worker
                                                                      └─ push.queue  ──▶ push-worker
                                                                                          │
                                                               SENT / FAILED (po 3 próbach) ◀─┘
```

| Kontener | Profil | Rola |
|---|---|---|
| `api` | `api` | REST API, zapis do bazy, dispatcher |
| `email-worker` | `email-worker` | obsługa kolejki `email.queue` |
| `push-worker` | `push-worker` | obsługa kolejki `push.queue` |
| `postgres` | | baza powiadomień |
| `rabbitmq` | | kolejki, panel: http://localhost:15672 (guest / guest) |

## Uruchomienie

Wymagany Docker Desktop.

```powershell
docker compose up --build
```

API działa pod `http://localhost:8080/api/v1/notifications`.

Logi workerów w osobnym terminalu:

```powershell
docker compose logs -f email-worker push-worker
```

Zatrzymanie i wyczyszczenie (również kolejek):

```powershell
docker compose down
```

Testy jednostkowe:

```powershell
./mvnw test -Dtest='NotificationServiceTest,QuietHoursPolicyTest,DeliveryServiceTest'
```

## API

| Metoda | Ścieżka | Opis |
|---|---|---|
| `POST` | `/api/v1/notifications` | utworzenie powiadomienia |
| `GET` | `/api/v1/notifications` | lista |
| `GET` | `/api/v1/notifications/{id}` | szczegóły |
| `POST` | `/api/v1/notifications/{id}/send-now` | wymuszenie wysyłki (tylko `PENDING`) |
| `POST` | `/api/v1/notifications/{id}/cancel` | anulowanie (`PENDING` / `QUEUED`) |

Przykładowe body:

```json
{
  "recipient": "jan.kowalski@example.com",
  "content": "Testowe powiadomienie",
  "channel": "EMAIL",
  "priority": "HIGH",
  "timeZone": "Europe/Warsaw",
  "scheduledAt": "2026-12-01T10:00:00"
}
```

`scheduledAt` to czas lokalny w strefie `timeZone`. `priority` jest opcjonalne (domyślnie `LOW`).

Statusy: `PENDING → QUEUED → SENT`, a także `FAILED` i `CANCELLED`.

## Testowanie wymagań

Gotowa kolekcja Bruno jest w katalogu `bruno/` (otwórz ją w Bruno: *Open Collection*).
Żądania tworzące zapisują id w zmiennej `notificationId`, z której korzystają *Get by id*, *Send now* i *Cancel*.

Przykłady poniżej używają PowerShella. Najpierw wklej pomocniczą funkcję:

```powershell
$api = "http://localhost:8080/api/v1/notifications"
function New-Notif($channel, $priority, $minutes, $zone = "Europe/Warsaw", $at = $null) { if (-not $at) { $at = (Get-Date).AddMinutes($minutes).ToString("yyyy-MM-ddTHH:mm:ss") }; $body = @{ recipient = "jan"; content = "test $channel $priority"; channel = $channel; priority = $priority; timeZone = $zone; scheduledAt = $at } | ConvertTo-Json; Invoke-RestMethod -Method Post -Uri $api -ContentType "application/json" -Body $body }
```

Podgląd statusów:

```powershell
Invoke-RestMethod $api | Format-Table id, channel, priority, status, attempts, sentAt
```

### 1. Tworzenie i planowanie wysyłki

```powershell
New-Notif EMAIL HIGH 1
```

Od razu status `PENDING`. Po ok. minucie w logach `email-worker` pojawi się `Dostarczono powiadomienie …`, a status zmieni się na `SENT`.

### 2. Kanały działają niezależnie, wiadomości się nie gubią

```powershell
docker compose stop email-worker
```
```powershell
New-Notif EMAIL LOW 1; New-Notif PUSH LOW 1
```

PUSH dochodzi (`SENT`), EMAIL czeka w kolejce (`QUEUED`). Po uruchomieniu workera EMAIL też dochodzi:

```powershell
docker compose start email-worker
```

### 3. Dokładnie raz i 3 próby

W `compose.yaml` ustaw dla `email-worker` `NOTIFI_SIMULATION_FAILURE_RATE: "1.0"` (każda próba kończy się błędem):

```powershell
docker compose up -d email-worker
```
```powershell
New-Notif EMAIL HIGH 1
```

W logach: próba `1/3`, `2/3`, `3/3`, potem status `FAILED`, `attempts = 3`.
Przy wartości `"0.5"` część powiadomień dochodzi w 2. lub 3. próbie, żadne nie jest dostarczone dwa razy.
Na koniec przywróć `"0.0"`.

### 4. Priorytety

```powershell
docker compose stop push-worker
```
```powershell
1..3 | % { New-Notif PUSH LOW 1 }; 1..3 | % { New-Notif PUSH HIGH 1 }
```

Po ok. minucie, gdy wszystkie mają status `QUEUED`:

```powershell
docker compose start push-worker
```

W logach najpierw przychodzą powiadomienia `HIGH`, potem `LOW`.

### 5. Wymuszenie wysyłki i anulowanie

```powershell
$n = New-Notif EMAIL LOW 60; Invoke-RestMethod -Method Post "$api/$($n.id)/send-now"
```

Powiadomienie zaplanowane za godzinę jest wysyłane od razu.

```powershell
$n = New-Notif EMAIL LOW 60; Invoke-RestMethod -Method Post "$api/$($n.id)/cancel"
```

Status `CANCELLED`, powiadomienie nie trafia do workera. Ponowne `cancel` lub `send-now` zwraca `409 Conflict`.

### 6. Ciche godziny (22:00–08:00 czasu odbiorcy)

```powershell
New-Notif PUSH LOW 0 "Europe/Warsaw" "2026-12-01T23:30:00"
```

`scheduledAtLocal` = `2026-12-02T08:00+01:00`, a `originalScheduledAt` zawiera pierwotny termin.

```powershell
New-Notif PUSH LOW 0 "America/New_York" "2026-12-01T15:00:00"
```

15:00 w Nowym Jorku to dzień, więc termin się nie zmienia.

### 7. Walidacja

Nieistniejąca strefa, termin w przeszłości albo puste `content` zwracają `400 Bad Request`. Nieistniejące id zwraca `404 Not Found`.

## Konfiguracja

`src/main/resources/application.properties` (w Dockerze nadpisywane zmiennymi środowiskowymi):

| Właściwość | Domyślnie | Opis |
|---|---|---|
| `notifi.quiet-hours.start` | `22:00` | początek cichych godzin |
| `notifi.quiet-hours.end` | `08:00` | koniec cichych godzin |
| `notifi.simulation.failure-rate` | `0.0` | prawdopodobieństwo nieudanej próby (0.0–1.0) |
