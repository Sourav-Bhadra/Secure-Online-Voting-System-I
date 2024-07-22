package net.codejava.controller;

// ----------------------------------------------------------------------------------//

import java.io.File;
import java.io.IOException;
import java.security.Principal;
import java.util.Arrays;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import net.codejava.helper.Message;
import net.codejava.model.User;
import net.codejava.repository.UserRepo;
import net.codejava.service.EmailService;
import net.codejava.service.UserService;

// --------------------------------------------------------------------------------------------- //

@Controller
public class MainController {

	// Beans of other classes so that we can use their methods \\

	@Autowired
	UserRepo repo;

	@Autowired
	UserService userservice;

	@Autowired
	EmailService emailservice;

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	// ----------------------------------------------------------------------------//

	// After loging in, users will be redirected to their respective pages
	// if admin logs in admin will be redirected to admin page("/users") similarly
	// for users to the user home page("/public/home")

	@RequestMapping("/default")
	public String defaultAfterLogin(HttpServletRequest request) {
		if (request.isUserInRole("ROLE_ADMIN")) {
			return "redirect:/users/";
		}

		return "redirect:/public/home";
	}

	// --------------------------------------------------------------------------------------------------------------
	// //

	// This url will return the home page of our website
	@GetMapping("/index")
	public String signIn() {
		return "index.html";
	}

	// This url will return the news page of our website
	@GetMapping("/news")
	public String news() {
		return "news.html";
	}

	// This url will return the contact page of our website
	@GetMapping("/contact")
	public String contact() {
		return "contact.html";
	}

	// This url will be executed when a new user tries to register with a existing
	// username
	@GetMapping("/usernameexist")
	public String usernameexist() {
		return "exist.html";
	}

	// ----------------------REGISTER(GET)--------------------------------------------------------------
	// //

	// This url will be executed when users tries to register
	@GetMapping("/register")
	public String showForm(Model model) {

		// We are using model to store the list of gender and listates
		// These data will be used in the register page

		User user = new User();
		model.addAttribute("user", user);

		List<String> listgender = Arrays.asList("Choose", "Male", "Female", "Others");
		model.addAttribute("listgender", listgender);

		List<String> liststates = Arrays.asList("Choose", "Andhra Pradesh", "Maharashtra", "Tamil Nadu", "Uttarakhand",
				"West Bengal");
		model.addAttribute("liststates", liststates);

		return "register_new";
	}

	// ----------------------REGISTER(POST)--------------------------------------------------------------
	// //

	// This url will be executed when the users fills in all the fields in the
	// register page
	// Since this is a post request, post request = when we are giving data to the
	// server
	// MultipartFile is for fetching any file, Modelattritube indicates the user
	// model, the application
	// will automatically fill the parameters with the details given by user.

	@PostMapping("/register")
	public String submitForm(@ModelAttribute("user") User user,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {

		// Checking if username already exists
		if (!(userservice.userExists(user.getUsername()))) {
			String fileName = multipartFile.getOriginalFilename();

			// String Adhar=user.getUsername();

			user.setRole("ROLE_USER");

			// decrypting the password before putting it in the db
			user.setPassword(passwordEncoder.encode(user.getPassword()));

			// we are also storing the filename in db and storing the file in our local HD
			user.setPhotos(fileName);

			// saving the user in the database
			// repo is the bean of User repository

			User savedUser = repo.save(user);
			// Storing the entire path of the file
			String uploadDir = "user-photos/" + savedUser.getUsername();

			// saving the file
			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

			System.out.println(user);

			return "redirect:/index";
			// return "index.html";
		} else {
			// if username already exists, then return this url
			return "redirect:/usernameexist";
		}
	}

	// ----------------------UPDATE(POST)--------------------------------------------------------------
	// //

	// Similar to Register url
	// Principle is used to fetch the details of the logged in user and session is
	// used to display message
	// explained later
	@PostMapping("/update")
	public String updateForm(@ModelAttribute("user") User user,
			@RequestParam("image") MultipartFile multipartFile, Principal principle, HttpSession session)
			throws IOException {

		try {
			// fetching the user object from db
			String username = principle.getName();
			User olduser = userservice.getUser(username);

			// If the user has updated the picture
			if (!multipartFile.isEmpty()) {
				String fileName = multipartFile.getOriginalFilename();
				user.setPhotos(fileName);
				String uploadDir = "user-photos/" + olduser.getUsername();

				// delete the old file and save the new one
				FileUtils.deleteDirectory(new File(uploadDir));

				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
			} else {

				// if the user is not updating the image then image field must be empty
				// in that case we'll fetch the old image details and insert it into this new
				// user object
				// Reason : If we dont do that, then the image field will remain empty when
				// inserted into the db
				// Thus to preserve the old data, we need to store that into the new object

				user.setPhotos(olduser.getPhotos());
			}

			user.setRole("ROLE_USER");

			User savedUser = repo.save(user);

			System.out.println(user);

			// This is a popup message giving a user feedback about the changes
			session.setAttribute("message", new Message("Your details are updated Successfully", "success"));

		} catch (Exception e) {
			session.setAttribute("message", new Message("Some error occured", "danger"));
			e.printStackTrace();
		}
		return "redirect:/public/home/account";
	}

	// This will return the error page
	@GetMapping("/error")
	public String errorpage() {
		return "error.html";
	}

	// ---------------------send mail from contact us (Bug is there)
	// ------------------------------------------------- //

	@PostMapping("/sendemail")
	public String sendEmail(@RequestParam("fullname") String name, @RequestParam("email") String email,
			@RequestParam("message") String message) {

		// sending email to our email Id with the message entered by the user

		System.out.println("Email: " + email);
		String subject = "Message from " + name + " from Contact Us section";
		String to = "sharereport3@gmail.com";
		boolean f = this.emailservice.sendEmail(subject, message, to);
		System.out.println(f);

		if (f == true)
			return "redirect:/index";
		else
			return "redirect:/contact";

	}

}
