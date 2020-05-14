package com.example.account.Dao;

import com.example.account.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.List;

@Repository
@EnableTransactionManagement
public interface UserDaoRepository extends JpaRepository<User, Integer> {
    User findByEmailAndPasswordAndUserPermission(String email, String password, String userPermission);
    List<User> findAllByEmailAndUserPermission(String email, String userPermission);
}