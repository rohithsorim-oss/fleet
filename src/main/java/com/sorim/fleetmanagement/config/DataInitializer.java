package com.sorim.fleetmanagement.config;

import com.sorim.fleetmanagement.entity.*;
import com.sorim.fleetmanagement.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final VehicleCategoryRepository categoryRepository;
    private final VehicleRepository vehicleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() == 0) {
            System.out.println("Initializing seed data...");
            initializeUsers();
            //initializeCategories();
            initializeVehicles();
            System.out.println("Seed data initialization completed.");
        }
    }

    private void initializeUsers() {
        User admin = new User();
        admin.setEmail("admin@fleetflow.com");
        admin.setPasswordHash(passwordEncoder.encode("Admin123!"));
        admin.setFullName("Admin User");
        admin.setPhone("+91-9876543210");
        admin.setRole(Role.ROLE_ADMIN);
        admin.setIsActive(true);
        userRepository.save(admin);
        System.out.println("Created admin user: admin@fleetflow.com");

        User user = new User();
        user.setEmail("user@fleetflow.com");
        user.setPasswordHash(passwordEncoder.encode("User123!"));
        user.setFullName("Regular User");
        user.setPhone("+91-9876543211");
        user.setRole(Role.ROLE_USER);
        user.setIsActive(true);
        userRepository.save(user);
        System.out.println("Created regular user: user@fleetflow.com");
    }

    private void initializeCategories() {
        VehicleCategory sedan = new VehicleCategory();
        sedan.setName("Sedan");
        sedan.setCode("SEDAN");
        sedan.setDescription("Four-door passenger vehicles");
        categoryRepository.save(sedan);

        VehicleCategory suv = new VehicleCategory();
        suv.setName("SUV");
        suv.setCode("SUV");
        suv.setDescription("Sport Utility Vehicles with high ground clearance");
        categoryRepository.save(suv);

        VehicleCategory truck = new VehicleCategory();
        truck.setName("Truck");
        truck.setCode("TRUCK");
        truck.setDescription("Light and heavy payload pickup trucks");
        categoryRepository.save(truck);

        VehicleCategory ev = new VehicleCategory();
        ev.setName("Electric Vehicle");
        ev.setCode("EV");
        ev.setDescription("Battery-powered zero-emission vehicles");
        categoryRepository.save(ev);

        System.out.println("Created 4 vehicle categories");
    }

    private void initializeVehicles() {
        User admin = userRepository.findByEmail("admin@fleetflow.com").orElseThrow();
        VehicleCategory sedan = categoryRepository.findByCode("SEDAN").orElseThrow();
        VehicleCategory suv = categoryRepository.findByCode("SUV").orElseThrow();
        VehicleCategory truck = categoryRepository.findByCode("TRUCK").orElseThrow();
        VehicleCategory ev = categoryRepository.findByCode("EV").orElseThrow();

        Vehicle vehicle1 = new Vehicle();
        vehicle1.setVin("1HGCR2F83HA123456");
        vehicle1.setMake("Honda");
        vehicle1.setModel("Accord");
        vehicle1.setYear(2023);
        vehicle1.setLicensePlate("CA-7XYZ99");
        vehicle1.setColor("Sonic Gray Pearl");
        vehicle1.setMileage(14200);
        vehicle1.setDailyRentalRate(new BigDecimal("65.00"));
        vehicle1.setStatus(VehicleStatus.AVAILABLE);
        vehicle1.setImageUrl("https://images.unsplash.com/photo-1590362891988-3069b2d86f7b");
        vehicle1.setCategory(sedan);
        vehicle1.setCreatedBy(admin);
        vehicleRepository.save(vehicle1);

        Vehicle vehicle2 = new Vehicle();
        vehicle2.setVin("1HGCR2F83HA789012");
        vehicle2.setMake("Toyota");
        vehicle2.setModel("RAV4");
        vehicle2.setYear(2024);
        vehicle2.setLicensePlate("CA-8ABC12");
        vehicle2.setColor("Magnetic Gray");
        vehicle2.setMileage(5200);
        vehicle2.setDailyRentalRate(new BigDecimal("85.00"));
        vehicle2.setStatus(VehicleStatus.AVAILABLE);
        vehicle2.setImageUrl("https://images.unsplash.com/photo-1568605117036-5fe5e7bab0b7");
        vehicle2.setCategory(suv);
        vehicle2.setCreatedBy(admin);
        vehicleRepository.save(vehicle2);

        Vehicle vehicle3 = new Vehicle();
        vehicle3.setVin("1HGCR2F83HA345678");
        vehicle3.setMake("Ford");
        vehicle3.setModel("F-150");
        vehicle3.setYear(2023);
        vehicle3.setLicensePlate("CA-9DEF34");
        vehicle3.setColor("Oxford White");
        vehicle3.setMileage(28000);
        vehicle3.setDailyRentalRate(new BigDecimal("120.00"));
        vehicle3.setStatus(VehicleStatus.AVAILABLE);
        vehicle3.setImageUrl("https://images.unsplash.com/photo-1583121274602-3e2820c69888");
        vehicle3.setCategory(truck);
        vehicle3.setCreatedBy(admin);
        vehicleRepository.save(vehicle3);

        Vehicle vehicle4 = new Vehicle();
        vehicle4.setVin("1HGCR2F83HA901234");
        vehicle4.setMake("Tesla");
        vehicle4.setModel("Model 3");
        vehicle4.setYear(2024);
        vehicle4.setLicensePlate("CA-0GHI56");
        vehicle4.setColor("Midnight Silver");
        vehicle4.setMileage(3100);
        vehicle4.setDailyRentalRate(new BigDecimal("95.00"));
        vehicle4.setStatus(VehicleStatus.AVAILABLE);
        vehicle4.setImageUrl("https://images.unsplash.com/photo-1560958089-b8a1929cea89");
        vehicle4.setCategory(ev);
        vehicle4.setCreatedBy(admin);
        vehicleRepository.save(vehicle4);

        Vehicle vehicle5 = new Vehicle();
        vehicle5.setVin("1HGCR2F83HA567890");
        vehicle5.setMake("BMW");
        vehicle5.setModel("X5");
        vehicle5.setYear(2023);
        vehicle5.setLicensePlate("CA-1JKL78");
        vehicle5.setColor("Alpine White");
        vehicle5.setMileage(18500);
        vehicle5.setDailyRentalRate(new BigDecimal("150.00"));
        vehicle5.setStatus(VehicleStatus.UNDER_MAINTENANCE);
        vehicle5.setImageUrl("https://images.unsplash.com/photo-1555215695-3004980ad54e");
        vehicle5.setCategory(suv);
        vehicle5.setCreatedBy(admin);
        vehicleRepository.save(vehicle5);

        System.out.println("Created 5 sample vehicles");
    }
}
