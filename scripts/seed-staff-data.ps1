param(
    [string]$HostName,
    [int]$Port,
    [string]$Database,
    [string]$Username,
    [string]$Password,
    [string]$StaffPassword = "staff123"
)

$ErrorActionPreference = "Stop"

$projectRoot = Split-Path -Parent $PSScriptRoot
$propertiesPath = Join-Path $projectRoot "backend\src\main\resources\application.properties"

function Get-PropertyValue {
    param(
        [string[]]$Lines,
        [string]$Key
    )

    $line = $Lines | Where-Object { $_.StartsWith("$Key=") } | Select-Object -First 1
    if (-not $line) {
        return $null
    }

    return ($line -split "=", 2)[1].Trim()
}

if (-not (Test-Path $propertiesPath)) {
    throw "Could not find application.properties at $propertiesPath"
}

$properties = Get-Content $propertiesPath
$jdbcUrl = Get-PropertyValue -Lines $properties -Key "spring.datasource.url"
$defaultUsername = Get-PropertyValue -Lines $properties -Key "spring.datasource.username"
$defaultPassword = Get-PropertyValue -Lines $properties -Key "spring.datasource.password"

if (-not $jdbcUrl) {
    throw "spring.datasource.url is missing in application.properties"
}

$jdbcMatch = [regex]::Match($jdbcUrl, '^jdbc:postgresql://(?<host>[^:/]+)(:(?<port>\d+))?/(?<database>[^?]+)')
if (-not $jdbcMatch.Success) {
    throw "Unable to parse PostgreSQL connection details from '$jdbcUrl'"
}

if (-not $HostName) {
    $HostName = $jdbcMatch.Groups["host"].Value
}

if (-not $Port) {
    $Port = if ($jdbcMatch.Groups["port"].Success) { [int]$jdbcMatch.Groups["port"].Value } else { 5432 }
}

if (-not $Database) {
    $Database = $jdbcMatch.Groups["database"].Value
}

if (-not $Username) {
    $Username = $defaultUsername
}

if (-not $Password) {
    $Password = $defaultPassword
}

if (-not $Username -or -not $Password) {
    throw "Database username/password could not be resolved. Pass -Username and -Password explicitly."
}

if (-not (Get-Command psql -ErrorAction SilentlyContinue)) {
    throw "psql is not available on PATH. Install PostgreSQL client tools or add psql to PATH."
}

$sql = @"
CREATE EXTENSION IF NOT EXISTS pgcrypto;

INSERT INTO shifts (shift_code, shift_name, start_time, end_time, duration_minutes, created_at, updated_at)
VALUES
    ('MORN', 'Morning Shift', TIME '08:00', TIME '12:00', 240, NOW(), NOW()),
    ('MID', 'Mid Shift', TIME '12:00', TIME '16:00', 240, NOW(), NOW()),
    ('EVE', 'Evening Shift', TIME '16:00', TIME '20:00', 240, NOW(), NOW())
ON CONFLICT (shift_code) DO UPDATE
SET
    shift_name = EXCLUDED.shift_name,
    start_time = EXCLUDED.start_time,
    end_time = EXCLUDED.end_time,
    duration_minutes = EXCLUDED.duration_minutes,
    updated_at = NOW();

WITH seed_staff AS (
    SELECT *
    FROM (
        VALUES
            ('staff',   'Staff Member 01', '+1555000001', 'staff01@housekeeping.local', 'MORN'),
            ('staff01', 'Staff Member 02', '+1555000002', 'staff02@housekeeping.local', 'MID'),
            ('staff02', 'Staff Member 03', '+1555000003', 'staff03@housekeeping.local', 'EVE'),
            ('staff03', 'Staff Member 04', '+1555000004', 'staff04@housekeeping.local', 'MORN'),
            ('staff04', 'Staff Member 05', '+1555000005', 'staff05@housekeeping.local', 'MID'),
            ('staff05', 'Staff Member 06', '+1555000006', 'staff06@housekeeping.local', 'EVE'),
            ('staff06', 'Staff Member 07', '+1555000007', 'staff07@housekeeping.local', 'MORN'),
            ('staff07', 'Staff Member 08', '+1555000008', 'staff08@housekeeping.local', 'MID'),
            ('staff08', 'Staff Member 09', '+1555000009', 'staff09@housekeeping.local', 'EVE'),
            ('staff09', 'Staff Member 10', '+1555000010', 'staff10@housekeeping.local', 'MORN')
    ) AS seed(username, full_name, phone, email, shift_code)
)
INSERT INTO users (username, password, role, created_at, updated_at)
SELECT
    seed.username,
    crypt(:'staff_password', gen_salt('bf')),
    'STAFF',
    NOW(),
    NOW()
FROM seed_staff seed
ON CONFLICT (username) DO UPDATE
SET
    password = EXCLUDED.password,
    role = 'STAFF',
    updated_at = NOW();

WITH seed_staff AS (
    SELECT *
    FROM (
        VALUES
            ('staff',   'Staff Member 01', '+1555000001', 'staff01@housekeeping.local', 'MORN'),
            ('staff01', 'Staff Member 02', '+1555000002', 'staff02@housekeeping.local', 'MID'),
            ('staff02', 'Staff Member 03', '+1555000003', 'staff03@housekeeping.local', 'EVE'),
            ('staff03', 'Staff Member 04', '+1555000004', 'staff04@housekeeping.local', 'MORN'),
            ('staff04', 'Staff Member 05', '+1555000005', 'staff05@housekeeping.local', 'MID'),
            ('staff05', 'Staff Member 06', '+1555000006', 'staff06@housekeeping.local', 'EVE'),
            ('staff06', 'Staff Member 07', '+1555000007', 'staff07@housekeeping.local', 'MORN'),
            ('staff07', 'Staff Member 08', '+1555000008', 'staff08@housekeeping.local', 'MID'),
            ('staff08', 'Staff Member 09', '+1555000009', 'staff09@housekeeping.local', 'EVE'),
            ('staff09', 'Staff Member 10', '+1555000010', 'staff10@housekeeping.local', 'MORN')
    ) AS seed(username, full_name, phone, email, shift_code)
)
INSERT INTO staff_profiles (
    user_id,
    full_name,
    phone,
    email,
    current_shift_id,
    availability_status,
    created_at,
    updated_at
)
SELECT
    users.id,
    seed.full_name,
    seed.phone,
    seed.email,
    shifts.id,
    'OFF_DUTY',
    NOW(),
    NOW()
FROM seed_staff seed
JOIN users ON users.username = seed.username
JOIN shifts ON shifts.shift_code = seed.shift_code
ON CONFLICT (user_id) DO UPDATE
SET
    full_name = EXCLUDED.full_name,
    phone = EXCLUDED.phone,
    email = EXCLUDED.email,
    current_shift_id = EXCLUDED.current_shift_id,
    availability_status = 'OFF_DUTY',
    updated_at = NOW();

SELECT
    users.username,
    staff_profiles.full_name,
    staff_profiles.email,
    shifts.shift_code,
    staff_profiles.availability_status
FROM staff_profiles
JOIN users ON users.id = staff_profiles.user_id
LEFT JOIN shifts ON shifts.id = staff_profiles.current_shift_id
WHERE users.username IN ('staff', 'staff01', 'staff02', 'staff03', 'staff04', 'staff05', 'staff06', 'staff07', 'staff08', 'staff09')
ORDER BY users.username;
"@

$tempSqlPath = Join-Path ([System.IO.Path]::GetTempPath()) "housekeeping-seed-staff-data.sql"
Set-Content -Path $tempSqlPath -Value $sql -Encoding ASCII

try {
    $env:PGPASSWORD = $Password

    & psql `
        --host $HostName `
        --port $Port `
        --username $Username `
        --dbname $Database `
        --set staff_password="$StaffPassword" `
        --file $tempSqlPath

    if ($LASTEXITCODE -ne 0) {
        throw "psql exited with code $LASTEXITCODE"
    }

    Write-Host ""
    Write-Host "Seed complete."
    Write-Host "Created or updated 10 staff accounts with linked staff_profiles."
    Write-Host "Login password for all seeded staff users: $StaffPassword"
    Write-Host "Usernames: staff, staff01, staff02, staff03, staff04, staff05, staff06, staff07, staff08, staff09"
}
finally {
    Remove-Item $tempSqlPath -ErrorAction SilentlyContinue
    Remove-Item Env:PGPASSWORD -ErrorAction SilentlyContinue
}
