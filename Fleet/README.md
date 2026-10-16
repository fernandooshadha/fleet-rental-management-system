# Fleet Rental Management System (FRMS)

ITS1033 Object-Oriented Programming - HDSE 77 coursework.
A terminal-based Java application for managing a vehicle fleet, customers and rental agreements.
All types live in the single package `lk.ijse.fleet`.

## Requirements
- JDK 21 or later (developed and tested on JDK 21; the brief asks for JDK 25+, no newer language features are used)

## Build & Run
From the project root (the folder containing `src`):

    javac -d bin $(find src -name "*.java")
    java -cp bin lk.ijse.fleet.Main

On Windows (PowerShell / cmd):

    dir /s /b src\*.java > sources.txt
    javac -d bin @sources.txt
    java -cp bin lk.ijse.fleet.Main

## First run
1. Choose `[2] Register New User`, then `[1] Login`.
2. Add at least one vehicle (`[1]`) and one customer (`[2]`).
3. Create a rental (`[4]`) using the generated IDs (e.g. `VEH-1001`, `CUS-1001`).

## Menus
Auth menu: `[1] Login`, `[2] Register New User`, `[0] Exit`.

Main menu: `[1] Add Vehicle`, `[2] Add Customer`, `[3] View Available Vehicles`,
`[4] Create Rental Agreement`, `[5] Return Vehicle`, `[6] View Revenue Report`,
`[7] View Vehicles by Fuel Type`, `[8] View Combined Fleet & Customer Report`,
`[9] View All Customers`, `[10] View All Rental Agreements`, `[0] Logout`.

## Business rules
| Item | Rule |
|------|------|
| Car | `dailyRate * days * fuelMultiplier` |
| Van | car formula + one-time `dailyRate * 0.20` surcharge (not per day) |
| Truck | car formula + `cargoTons * 10 * days` (per day) |
| IndividualCustomer | 0% below 100 points, 5% for 100-299, 10% for 300+ |
| CorporateCustomer | flat 15% discount; rental rejected if `outstandingBalance + cost > creditLimit` |

Fuel multipliers: PETROL 1.00, DIESEL 0.90, ELECTRIC 0.75, HYBRID 0.85.

## Concepts demonstrated
- Abstraction / inheritance / polymorphism: `Vehicle` -> `Car`, `Van`, `Truck`; `Customer` -> `IndividualCustomer`, `CorporateCustomer`
- Rich enum `FuelType`, plain enum `RentalStatus`
- Composition: `RentalAgreement` has-a `Vehicle` and `Customer`; `FleetManager` owns the lists
- Static counters for auto-generated IDs (`VEH-`, `CUS-`, `AGR-`)
- Custom checked exceptions: `VehicleNotAvailableException`, `InsufficientCreditException`
- Functional interface `RentalEventListener` implemented with a lambda in `Main`
- Interface polymorphism: `Reportable` implemented directly by `Vehicle` and `Customer`
- Non-static inner class `FleetManager.FleetSnapshot`
- Two daemon threads (`VehicleStatusMonitor`, `OverdueReminderWorker`) running every 5 seconds
- Synchronization: all `FleetManager` methods that touch the shared lists are `synchronized`,
  so the background threads never read the lists while the menu thread modifies them
- Streams + lambdas: available vehicles, fuel-type filter, sorted customers, revenue total

## Notes
- The background workers print every 5 seconds, so status/overdue lines may appear between menu prompts. This is expected.
- `OverdueReminderWorker` ages only *active* agreements; returned agreements stop ageing.
- Passwords are stored in plain text, as required by the brief. Do not reuse this as a production login design.
- No role-based access control or file persistence (out of scope).

## AI Assistance Disclosure
AI assistance (Claude, by Anthropic) was used to review the project against the coursework brief, fix
deviations (discount tiers, receipt format, menu numbering, overdue logic, status output format, exception
flow), add the two background worker classes, add thread-safety, and update this README.
The author is responsible for understanding and being able to explain all submitted code.
