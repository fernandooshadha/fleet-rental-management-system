package lk.ijse.fleet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class FleetManager {
    private List<Vehicle> vehicles = new ArrayList<>();
    private List<Customer> customers = new ArrayList<>();
    private List<RentalAgreement> agreements = new ArrayList<>();
    private RentalEventListener rentalEventListener; // association, may be null

    public synchronized void addVehicle(Vehicle v) {
        vehicles.add(v);
    }

    public synchronized void addCustomer(Customer c) {
        customers.add(c);
    }

    public synchronized Vehicle findVehicleById(String id) {
        for (Vehicle v : vehicles) {
            if (v.getVehicleId().equalsIgnoreCase(id)) return v;
        }
        return null;
    }

    public synchronized Customer findCustomerById(String id) {
        for (Customer c : customers) {
            if (c.getCustomerId().equalsIgnoreCase(id)) return c;
        }
        return null;
    }

    public synchronized void setRentalEventListener(RentalEventListener listener) {
        this.rentalEventListener = listener;
    }

    public synchronized List<Vehicle> getAvailableVehicles() {
        return vehicles.stream()
                .filter(v -> v.getStatus() == RentalStatus.AVAILABLE)
                .collect(Collectors.toList());
    }

    public synchronized RentalAgreement createRentalAgreement(String vehicleId, String customerId, int rentalDays)
            throws VehicleNotAvailableException, InsufficientCreditException {
        if (rentalDays <= 0) {
            System.out.println("Rental days must be greater than zero.");
            return null;
        }

        Vehicle vehicle = findVehicleById(vehicleId);
        if (vehicle == null) {
            System.out.println("Vehicle not found: " + vehicleId);
            return null;
        }
        if (vehicle.getStatus() != RentalStatus.AVAILABLE) {
            throw new VehicleNotAvailableException(
                    "Vehicle " + vehicle.getVehicleId() + " is not available (currently "
                            + vehicle.getStatus() + ").");
        }

        Customer customer = findCustomerById(customerId);
        if (customer == null) {
            System.out.println("Customer not found: " + customerId);
            return null;
        }

        double subtotal = vehicle.calculateRentalCost(rentalDays);
        double total = customer.applyDiscount(subtotal);

        if (customer instanceof CorporateCustomer) {
            CorporateCustomer corp = (CorporateCustomer) customer;
            if (!corp.hasSufficientCredit(total)) {
                throw new InsufficientCreditException(String.format(
                        "Insufficient credit for %s. Cost: %,.2f, remaining credit: %,.2f",
                        corp.getFullName(), total,
                        corp.getCreditLimit() - corp.getOutstandingBalance()));
            }
        }

        RentalAgreement agreement = new RentalAgreement(vehicle, customer, rentalDays);
        vehicle.setStatus(RentalStatus.RENTED);
        agreements.add(agreement);

        if (rentalEventListener != null) {
            rentalEventListener.onRentalCreated(agreement);
        }
        return agreement;
    }

    public synchronized void returnVehicle(String agreementId) {
        for (RentalAgreement a : agreements) {
            if (a.getAgreementId().equalsIgnoreCase(agreementId)) {
                if (isActive(a)) {
                    a.getVehicle().setStatus(RentalStatus.AVAILABLE);
                    System.out.println("Vehicle " + a.getVehicle().getVehicleId()
                            + " returned. Agreement " + a.getAgreementId() + " closed.");
                } else {
                    System.out.println("Agreement " + a.getAgreementId() + " has already been closed.");
                }
                return;
            }
        }
        System.out.println("Agreement not found: " + agreementId);
    }

    private boolean isActive(RentalAgreement agreement) {
        if (agreement.getVehicle().getStatus() != RentalStatus.RENTED) return false;
        int idx = agreements.indexOf(agreement);
        for (int i = idx + 1; i < agreements.size(); i++) {
            if (agreements.get(i).getVehicle() == agreement.getVehicle()) return false;
        }
        return true;
    }

    // ---- Mission 3 ------------

    public synchronized FleetSnapshot generateSnapshot() {
        return new FleetSnapshot();
    }

    public synchronized void printFleetStatus() {
        generateSnapshot().printSummary();
    }

    public synchronized void checkOverdueAgreements() {
        for (RentalAgreement a : agreements) {
            if (!isActive(a)) continue;
            a.setDaysElapsed(a.getDaysElapsed() + 1);
            if (a.getDaysElapsed() > a.getRentalDays()) {
                System.out.println("[OVERDUE ALERT] Agreement " + a.getAgreementId()
                        + " has exceeded its " + a.getRentalDays() + "-day rental period.");
            }
        }
    }

    // ---- Mission 4 -----------------------------

    public synchronized List<Vehicle> getVehiclesByFuelType(FuelType type) {
        return vehicles.stream()
                .filter(v -> v.getFuelType() == type)
                .collect(Collectors.toList());
    }

    public synchronized List<Customer> getCustomersSortedByName() {
        return customers.stream()
                .sorted(Comparator.comparing(c -> c.getFullName()))
                .collect(Collectors.toList());
    }

    public synchronized double generateRevenueReport() {
        return agreements.stream()
                .mapToDouble(a -> a.getTotalCost())
                .sum();
    }

    public synchronized void printAllReportables() {
        List<Reportable> reportables = new ArrayList<>();
        reportables.addAll(vehicles);
        reportables.addAll(customers);

        for (Reportable item : reportables) {
            System.out.println(item.getReportCategory() + ": " + item.getSummary());
        }
    }

    public synchronized List<Customer> getAllCustomers() {
        List<Customer> result = new ArrayList<>();
        for (Customer c : customers) {
            result.add(c);
        }
        return result;
    }

    public synchronized List<RentalAgreement> getAllAgreements() {
        List<RentalAgreement> result = new ArrayList<>();
        for (RentalAgreement a : agreements) {
            result.add(a);
        }
        return result;
    }

    public class FleetSnapshot {
        private int availableCount;
        private int rentedCount;
        private double totalRevenue;

        public FleetSnapshot() {
            for (Vehicle v : vehicles) {
                if (v.getStatus() == RentalStatus.AVAILABLE) availableCount++;
                else rentedCount++;
            }
            for (RentalAgreement a : agreements) {
                totalRevenue += a.getTotalCost();
            }
        }

        public void printSummary() {
            System.out.printf("[FLEET STATUS] Available: %d  Rented: %d  Total Revenue So Far: %,.2f%n",
                    availableCount, rentedCount, totalRevenue);
        }
    }
}
