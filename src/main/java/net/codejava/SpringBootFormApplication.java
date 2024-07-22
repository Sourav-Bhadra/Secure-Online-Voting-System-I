package net.codejava;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import net.codejava.model.User;
import net.codejava.repository.UserRepo;

@SpringBootApplication
public class SpringBootFormApplication implements CommandLineRunner{

	public static void main(String[] args) {
		SpringApplication.run(SpringBootFormApplication.class, args);
	}
	@Autowired
	UserRepo userRepo;
	
	@Autowired
	private BCryptPasswordEncoder passwordEncoder;
	@Override
	public void run(String... args) throws Exception {
	
		// Admin id and password

		User user1= new User();
		user1.setUsername("admin");
		
		user1.setEmail("admin@gmail.com");
		user1.setPassword(passwordEncoder.encode("1234"));
		user1.setRole("ROLE_ADMIN");
		
		this.userRepo.save(user1);
		
	}

}
