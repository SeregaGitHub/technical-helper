package ru.kraser.technical_helper;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kraser.technical_helper.common_module.enums.Role;
import ru.kraser.technical_helper.main_server.model.Department;
import ru.kraser.technical_helper.main_server.model.User;
import ru.kraser.technical_helper.main_server.repository.DepartmentRepository;
import ru.kraser.technical_helper.main_server.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;

@SpringBootApplication
public class MainServer implements CommandLineRunner {

	@Autowired
	private UserRepository userRepository;

	@Autowired
	private DepartmentRepository departmentRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(MainServer.class, args);
	}

	@Override
	public void run(String... args) throws Exception {

		Optional<User> optionalUser = userRepository.findTop1ByRoleAndEnabledTrue(Role.ADMIN);

		if (optionalUser.isEmpty()) {
			LocalDateTime now = LocalDateTime.now().withNano(0);

			Optional<Department> departmentOptional = departmentRepository.findByName("admin_department");
			Department adminDepartment;

			if (departmentOptional.isPresent()) {
				adminDepartment = departmentOptional.get();
				if (!adminDepartment.isEnabled()) {
					adminDepartment.setEnabled(true);
					adminDepartment.setLastUpdatedBy("own_id");
					adminDepartment.setLastUpdatedDate(now);
					departmentRepository.save(adminDepartment);
				}
			} else {
				adminDepartment = new Department("admin_department", true);
				adminDepartment.setCreatedBy("own_id");
				adminDepartment.setCreatedDate(now);
				adminDepartment.setLastUpdatedBy("own_id");
				adminDepartment.setLastUpdatedDate(now);
				departmentRepository.save(adminDepartment);
			}

			Optional<User> userOptional = userRepository.findUserByUsername("admin");
			User admin;

			if (userOptional.isPresent()) {
				admin = userOptional.get();
				if (!admin.isEnabled()) {
					admin.setEnabled(true);
					admin.setLastUpdatedBy("own_id");
					admin.setLastUpdatedDate(now);
					userRepository.save(admin);
				}
			} else {
				admin = new User(
						"admin",
						passwordEncoder.encode("adminpassword"),
						true,
						adminDepartment,
						Role.ADMIN);
				admin.setCreatedBy("own_id");
				admin.setCreatedDate(now);
				admin.setLastUpdatedBy("own_id");
				admin.setLastUpdatedDate(now);
				userRepository.save(admin);
			}
		}
	}
}
