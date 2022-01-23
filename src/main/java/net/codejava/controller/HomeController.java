package net.codejava.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.security.Principal;

import java.util.Random;

import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import net.codejava.helper.Message;
import net.codejava.model.User;
import net.codejava.repository.UserRepo;
import net.codejava.service.EmailService;
import net.codejava.service.UserService;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/public")
public class HomeController {

	Random random = new Random(1000);

	@Autowired
	UserService userservice;

	@Autowired
	EmailService emailservice;

	@GetMapping("/home")
	public String getHome(Principal principle, Model model) {

		String username = principle.getName();
		User user = userservice.getUser(username);
		model.addAttribute("currentUser", user);
		return "index2.html";
	}

	@GetMapping("/register")
	public String getRegister() {
		return "this is register";
	}

	@GetMapping("/home/account")
	public String getAccountdetails(Principal principle, Model model) {
		String username = principle.getName();
		User user = userservice.getUser(username);
		model.addAttribute("currentUser", user);
		return "account.html";
	}

	@GetMapping("/contact")
	public String getContact() {
		return "this is contact";
	}

	@GetMapping("/home/faceverification")
	public String page(Principal principle) throws IOException, InterruptedException {
		// String path = "C:\\Users\\Sourav\\Desktop\\Project
		// Vote\\demo\\python_folder/main.py";
		String username = principle.getName();
		String param2 = username;
		ProcessBuilder pb = new ProcessBuilder("python",
				"C:\\Users\\Sourav\\Desktop\\Springboot vote\\src\\main\\java\\net\\codejava\\controller/main.py",
				"" + param2).inheritIO();
		Process p = pb.start();
		p.waitFor();
		File file = new File("C:\\Users\\Sourav\\Desktop\\Springboot vote\\src\\main\\resources\\templates/out.txt");
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String st;
		char c = '\0';
		// Condition holds true till
		// there is character in a string
		while ((st = reader.readLine()) != null) {
			System.out.println(st.length());
			c = st.charAt(12);
		}
		// Print the string
		System.out.println(c);
		reader.close();
		if (c == '1') {
			return "redirect:/public/otpsend";
		}

		return "redirect:/public/home";
	}

	@GetMapping("/otpsend")
	public String otpsend(Principal principle, HttpSession session) {

		String username = principle.getName();
		User user = userservice.getUser(username);

		String email = user.getEmail();

		System.out.println("Email: " + email);
		// generating otp
		int min = 100000;
		int max = 999999;
		Integer otp = (int) (Math.random() * (max - min + 1) + min);
		System.out.println(otp);

		// write code for send otp to email...

		String subject = "OTP from SVOC";
		String message = " " +
				"Your OTP is "+
				 "<b>" +
				otp +
				"</b>" +
				" ";
		String to = email;
		boolean flag = this.emailservice.sendEmail(subject, message, to);

		if (flag) {
			session.setAttribute("otp", otp);
			session.setAttribute("email", email);
			// Integer check=(int) session.getAttribute("otp");
			return "verify_OTP.html";
		} else {
			return "redirect:/public/otpsend";
		}
	}

	@PostMapping("/verify-otp")
	public String verify(@RequestParam("digit-1") String d1, @RequestParam("digit-2") String d2,
			@RequestParam("digit-3") String d3, @RequestParam("digit-4") String d4, @RequestParam("digit-5") String d5,
			@RequestParam("digit-6") String d6, HttpSession session) {
		// TODO: process POST request
		String res = d1 + d2 + d3 + d4 + d5 + d6;

		Integer otp = Integer.parseInt(res);
		Integer old_otp = (Integer) session.getAttribute("otp");

		System.out.println(old_otp + " " + otp);

		if (old_otp.equals(otp)) {
			// String email=(String)session.getAttribute("email");
			System.out.println("succesfull");

			return "vote.html";
		}
		return "redirect:/logout";
	}

	@GetMapping("/home/match")
	public String isMatch() {
		return "match.html";
	}

	// @GetMapping("/notmatch")
	// public String isNotMatch() {
	// return "notmatch.html";
	// }

	// @PostMapping("/updatePassword")
	// @PreAuthorize("hasRole('ROLE_USER')")
	// public GenericResponse changeUserPassword(Locale locale,
	// @RequestParam("password") String password,
	// @RequestParam("oldpassword") String oldPassword) {
	// User user = userService.findUserByEmail(
	// SecurityContextHolder.getContext().getAuthentication().getName());

	// if (!userService.checkIfValidOldPassword(user, oldPassword)) {
	// throw new InvalidOldPasswordException();
	// }
	// userService.changeUserPassword(user, password);
	// return new GenericResponse(messages.getMessage("message.updatePasswordSuc",
	// null, locale));
	// }

	@Autowired
	BCryptPasswordEncoder passwordEncoder;

	@Autowired
	UserRepo repo;

	@PostMapping("home/update/update_password")
	public String changePassword(@RequestParam("oldPassword") String oldPassword,
			@RequestParam("newPassword") String newPassword, Principal principal, HttpSession session) {

		oldPassword = oldPassword.substring(0, oldPassword.length() - 1);

		System.out.println("Old Password " + oldPassword);
		System.out.println("New Password " + newPassword);

		String username = principal.getName();
		System.out.println(username);
		User currentuser = userservice.getUser(username);

		if (this.passwordEncoder.matches(oldPassword, currentuser.getPassword())) {
			currentuser.setPassword(this.passwordEncoder.encode(newPassword));
			this.repo.save(currentuser);

			session.setAttribute("message", new Message("your password is changed", "success"));
			// session.setAttribute("message", new Message);
		} else {
			//
			session.setAttribute("message", new Message("wrong password", "danger"));
		}

		// System.out.println(currentuser);

		return "redirect:/public/home/account";
	}
}
