package lk.ijse.fleet;

public class RentalAgreement {
    private static int counter = 1000;

    private String agreementId;
    private Vehicle vehicle;    
    private Customer customer; 
    private int rentalDays;
    private double totalCost;
    private int daysElapsed;   

    public RentalAgreement(Vehicle vehicle, Customer customer, int rentalDays) {
        this.agreementId = "AGR-" + (++counter);
        this.vehicle = vehicle;
        this.customer = customer;
        this.rentalDays = rentalDays;
        this.daysElapsed = 0;
        double subtotal = vehicle.calculateRentalCost(rentalDays);
        this.totalCost = customer.applyDiscount(subtotal);
    }

    public static int getCounter() {
        return counter;
    }

    public static void setCounter(int counter) {
        RentalAgreement.counter = counter;
    }

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public Vehicle getVehicle() {
        return vehicle;
    }

    public void setVehicle(Vehicle vehicle) {
        this.vehicle = vehicle;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public int getRentalDays() {
        return rentalDays;
    }

    public void setRentalDays(int rentalDays) {
        this.rentalDays = rentalDays;
    }

    public double getTotalCost() {
        return totalCost;
    }

    public void setTotalCost(double totalCost) {
        this.totalCost = totalCost;
    }

    public int getDaysElapsed() {
        return daysElapsed;
    }

    public void setDaysElapsed(int daysElapsed) {
        this.daysElapsed = daysElapsed;
    }

    

    @Override
    public String toString() {
        String line = "================================================================";
        String vehicleType = vehicle.getClass().getSimpleName();
        String customerType = customer.getClass().getSimpleName().replace("Customer", "");
        StringBuilder sb = new StringBuilder();
        sb.append(line).append(System.lineSeparator());
        sb.append("RENTAL AGREEMENT RECEIPT").append(System.lineSeparator());
        sb.append(line).append(System.lineSeparator());
        sb.append(String.format("Agreement ID : %s%n", agreementId));
        sb.append(String.format("Vehicle      : %s - %s %s (%s)%n",
                vehicle.getVehicleId(), vehicle.getBrand(), vehicle.getModel(), vehicleType));
        sb.append(String.format("Customer     : %s - %s (%s)%n",
                customer.getCustomerId(), customer.getFullName(), customerType));
        sb.append(String.format("Rental Days  : %d%n", rentalDays));
        sb.append(String.format("Total Cost   : %,.2f%n", totalCost));
        sb.append(line);
        return sb.toString();
    }
}
