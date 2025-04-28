package ru.kraser.technical_helper.main_server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.kraser.technical_helper.main_server.model.User;

@Repository
public interface UserRepository extends JpaRepository<User, String> {

}
