package lk.ijse.fleet;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Main {
    private static final String LINE = "========================================================";

    private FleetManager fleetManager = new FleetManager();
    private UserManager userManager = new UserManager();
    private Scanner scanner = new Scanner(System.in);
    private SystemUser loggedInUser;

    public static void main(String[] args) {
        try {
            new Main().run();
        } catch (NoSuchElementException e) {
            System.out.println("\nInput closed. Goodbye!");
        }
    }

    public void run() {
        fleetManager.setRentalEventListener(agreement ->
                System.out.println("[EVENT] New rental created: " + agreement.getAgreementId())
        );

        Thread statusThread = new Thread(new VehicleStatusMonitor(fleetManager));
        statusThread.setDaemon(true);
        statusThread.start();

        Thread overdueThread = new Thread(new OverdueReminderWorker(fleetManager));
        overdueThread.setDaemon(true);
        overdueThread.start();

        while (true) {
            showAuthMenu();
            showMainMenu();
        }
    }

    // --------------------- input helpers

    private String prompt(String label) {
        System.out.print(label);
        return scanner.nextLine().trim();
    }

    private String promptNonEmpty(String label) {
        while (true) {
            String s = prompt(label);
            if (!s.isEmpty()) return s;
            System.out.println("This field cannot be empty.");
        }
    }

    private double readPositiveDouble(String label) {
        while (true) {
            try {
                double d = Double.parseDouble(prompt(label));
                if (d > 0) return d;
                System.out.println("Value must be greater than zero.");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid number.");
            }
        }
    }

    private int readInt(String label, int min) {
        while (true) {
            try {
                int n = Integer.parseInt(prompt(label));
                if (n >= min) return n;
                System.out.println("Value must be at least " + min + ".");
            } catch (NumberFormatException e) {
                System.out.println("Please enter a valid whole number.");
            }
        }
    }

    private FuelType readFuelType() {
        while (true) {
            String s = prompt("Fuel type (PETROL/DIESEL/ELECTRIC/HYBRID) : ").toUpperCase();
            try {
                return FuelType.valueOf(s);
            } catch (IllegalArgumentException e) {
                System.out.println("Invalid fuel type.");
            }
        }
    }

    // ----------------------- auth menu

    public void showAuthMenu() {
        while (true) {
            System.out.println(LINE);
            System.out.println("FLEET RENTAL MANAGEMENT SYSTEM (FRMS)");
            System.out.println(LINE);
            System.out.println("[1] Login");
            System.out.println("[2] Register New User");
            System.out.println("[0] Exit");
            String choice = prompt("Enter choice > ");
            System.out.println(LINE);

            switch (choice) {
                case "1": {
                    String username = prompt("Username : ");
                    String password = prompt("Password : ");
                    SystemUser user = userManager.login(username, password);
                    if (user != null) {
                        loggedInUser = user;
                        return;
                    }
                    System.out.println("Invalid username or password");
                    break;
                }
                case "2": {
                    String username = prompt("Username : ");
                    String password = prompt("Password : ");
                    String fullName = prompt("Full name : ");
                    userManager.registerUser(username, password, fullName);
                    break;
                }
                case "0":
                    System.out.println("Goodbye!");
                    System.exit(0);
                    return;
                default:
                    System.out.println("Invalid choice. Please enter 1, 2 or 0.");
            }
        }
    }

    // --------------------------------------- main menu

    public void showMainMenu() {
        System.out.println("Welcome, " + loggedInUser.getFullName() + "! Loading fleet dashboard...");

        while (loggedInUser != null) {
            System.out.println(LINE);
            System.out.println("FRMS MAIN MENU | Logged in as: " + loggedInUser.getFullName());
            System.out.println(LINE);
            System.out.println("[1] Add Vehicle");
            System.out.println("[2] Add Customer");
            System.out.println("[3] View Available Vehicles");
            System.out.println("[4] Create Rental Agreement");
            System.out.println("[5] Return Vehicle");
            System.out.println("[6] View Revenue Report");
            System.out.println("[7] View Vehicles by Fuel Type");
            System.out.println("[8] View Combined Fleet & Customer Report");
            System.out.println("[9] View All Customers");
            System.out.println("[10] View All Rental Agreements");
            System.out.println("[0] Logout");
            String choice = prompt("Enter choice > ");
            System.out.println(LINE);

            try {
                switch (choice) {
                    case "1": addVehicle(); break;
                    case "2": addCustomer(); break;
                    case "3": printVehicles(fleetManager.getAvailableVehicles()); break;
                    case "4": createRental(); break;
                    case "5": fleetManager.returnVehicle(prompt("Agreement ID : ")); break;
                    case "6": showRevenueReport(); break;
                    case "7": showVehiclesByFuelType(); break;
                    case "8": fleetManager.printAllReportables(); break;
                    case "9": printCustomers(); break;
                    case "10": printAgreements(); break;
                    case "0":
                        loggedInUser = null;
                        System.out.println("Logged out.");
                        break;
                    default:
                        System.out.println("Invalid choice. Please enter a number from the menu.");
                }
            } catch (NumberFormatException e) {
                System.out.println("Invalid number entered. Please try again.");
            }
        }
    }

    // ------------------------------------------ operations

    private void addVehicle() {
        String type = prompt("Type (1=Car, 2=Van, 3=Truck) : ");
        if (!type.equals("1") && !type.equals("2") && !type.equals("3")) {
            System.out.println("Invalid vehicle type.");
            return;
        }
        String reg = promptNonEmpty("Registration number : ");
        String brand = promptNonEmpty("Brand : ");
        String model = promptNonEmpty("Model : ");
        double rate = readPositiveDouble("Daily rate : ");
        FuelType fuel = readFuelType();

        Vehicle v;
        if (type.equals("1")) {
            v = new Car(reg, brand, model, rate, fuel);
        } else if (type.equals("2")) {
            v = new Van(reg, brand, model, rate, fuel, readInt("Passenger capacity : ", 1));
        } else {
            v = new Truck(reg, brand, model, rate, fuel, readPositiveDouble("Cargo capacity (tons) : "));
        }
        fleetManager.addVehicle(v);
        System.out.println("Vehicle added: " + v.getVehicleId());
    }

    private void addCustomer() {
        String type = prompt("Type (1=Individual, 2=Corporate) : ");
        if (!type.equals("1") && !type.equals("2")) {
            System.out.println("Invalid customer type.");
            return;
        }
        String name = promptNonEmpty("Full name : ");
        String contact = promptNonEmpty("Contact number : ");
        Customer c;
        if (type.equals("1")) {
            c = new IndividualCustomer(name, contact, readInt("Loyalty points : ", 0));
        } else {
            c = new CorporateCustomer(name, contact, readPositiveDouble("Credit limit : "));
        }
        fleetManager.addCustomer(c);
        System.out.println("Customer added: " + c.getCustomerId());
    }

    private void createRental() {
        String vehicleId = promptNonEmpty("Vehicle ID (e.g. VEH-1001) : ");
        String customerId = promptNonEmpty("Customer ID (e.g. CUS-1001) : ");
        int days = readInt("Rental days : ", 1);
        try {
            RentalAgreement agreement =
                    fleetManager.createRentalAgreement(vehicleId, customerId, days);
            if (agreement != null) {
                System.out.println(agreement);
            }
        } catch (VehicleNotAvailableException | InsufficientCreditException e) {
            System.out.println("Rental failed: " + e.getMessage());
        }
    }

    private void showRevenueReport() {
        System.out.println("================================================================");
        System.out.println("FLEET REVENUE REPORT");
        System.out.println("================================================================");
        System.out.printf("Total Revenue: %,.2f%n", fleetManager.generateRevenueReport());
        System.out.println("----------------------------------------------------------------");
    }

    private void showVehiclesByFuelType() {
        FuelType type = readFuelType();
        printVehicles(fleetManager.getVehiclesByFuelType(type));
    }

    private void printVehicles(List<Vehicle> list) {
        if (list.isEmpty()) {
            System.out.println("No vehicles found.");
            return;
        }
        for (Vehicle v : list) {
            System.out.println(v);
        }
    }

    private void printCustomers() {
        List<Customer> list = fleetManager.getCustomersSortedByName();
        if (list.isEmpty()) {
            System.out.println("No customers found.");
            return;
        }
        for (Customer c : list) {
            System.out.println(c);
        }
    }

    private void printAgreements() {
        List<RentalAgreement> list = fleetManager.getAllAgreements();
        if (list.isEmpty()) {
            System.out.println("No rental agreements found.");
            return;
        }
        for (RentalAgreement a : list) {
            System.out.println(a);
        }
    }
}
