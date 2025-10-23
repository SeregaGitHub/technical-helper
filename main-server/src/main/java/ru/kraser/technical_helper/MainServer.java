package ru.kraser.technical_helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;
import ru.kraser.technical_helper.main_server.util.data_sours.DataSours;

@SpringBootApplication
public class MainServer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    //@Autowired
    //private PasswordEncoder passwordEncoder;

    @Autowired
    DataSours dataSours;  // NEED FOR DELETE THIS CLASS !!!

    public static void main(String[] args) {
        SpringApplication.run(MainServer.class, args);
    }

    @Override
    public void run(String... args) throws Exception {

        //System.out.println("Start MainServer...");

        /*Optional<User> optionalUser = userRepository.findTop1ByRoleAndEnabledTrue(Role.ADMIN);

        if (optionalUser.isEmpty()) {
            LocalDateTime now = LocalDateTime.now().withNano(0);
            String temporaryAdminId = "temporary_admin_id";

            String dropConstraints = "ALTER TABLE IF EXISTS users " +
                    "DROP CONSTRAINT IF EXISTS fk_user_created_by; " +
                    "ALTER TABLE IF EXISTS users " +
                    "DROP CONSTRAINT IF EXISTS fk_user_last_updated_by; " +
                    "ALTER TABLE IF EXISTS department " +
                    "DROP CONSTRAINT IF EXISTS fk_department_created_by; " +
                    "ALTER TABLE IF EXISTS department " +
                    "DROP CONSTRAINT IF EXISTS fk_department_last_updated_by;";

            Connection conn = null;
            PreparedStatement prst = null;
            CallableStatement clst = null;

            try {
                conn = dataSours.getConnection();

                prst = conn.prepareStatement(dropConstraints);
                prst.executeUpdate();

                Optional<Department> departmentOptional = departmentRepository.findByName("admin_department");
                Department adminDepartment;

                if (departmentOptional.isPresent()) {
                    adminDepartment = departmentOptional.get();
                    if (!adminDepartment.isEnabled()) {
                        adminDepartment.setEnabled(true);
                        adminDepartment.setLastUpdatedBy(temporaryAdminId);
                        adminDepartment.setLastUpdatedDate(now);
                        adminDepartment = departmentRepository.save(adminDepartment);
                    }
                } else {
                    adminDepartment = new Department("admin_department", true);
                    adminDepartment.setCreatedBy(temporaryAdminId);
                    adminDepartment.setCreatedDate(now);
                    adminDepartment.setLastUpdatedBy(temporaryAdminId);
                    adminDepartment.setLastUpdatedDate(now);
                    adminDepartment = departmentRepository.save(adminDepartment);
                }

                Optional<User> userOptional = userRepository.findUserByUsername("admin");
                User admin;

                if (userOptional.isPresent()) {
                    admin = userOptional.get();
                    admin.setEnabled(true);
                    admin.setRole(Role.ADMIN);
                    admin.setLastUpdatedBy(temporaryAdminId);
                    admin.setLastUpdatedDate(now);
                    admin = userRepository.save(admin);
                } else {
                    admin = new User(
                            "admin",
                            passwordEncoder.encode("adminpassword"),
                            true,
                            adminDepartment,
                            Role.ADMIN);
                    admin.setCreatedBy(temporaryAdminId);
                    admin.setCreatedDate(now);
                    admin.setLastUpdatedBy(temporaryAdminId);
                    admin.setLastUpdatedDate(now);
                    admin = userRepository.save(admin);
                }

                clst = conn.prepareCall("CALL set_current_id(?, ?, ?, ?, ?, ?, ?)");
                clst.setString(1, adminDepartment.getId());
                clst.setString(2, admin.getId());
                clst.setString(3, adminDepartment.getCreatedBy());
                clst.setString(4, admin.getCreatedBy());
                clst.setString(5, adminDepartment.getLastUpdatedBy());
                clst.setString(6, admin.getLastUpdatedBy());
                clst.setString(7, temporaryAdminId);
                clst.execute();

            } catch (SQLException e) {
                throw new ServerException("Неверные параметры подключения к базе данных !!!");
            } finally {
                if (prst != null) prst.close();
                if (clst != null) clst.close();
                if (conn != null) conn.close();
            }
        }*/
    }
}
