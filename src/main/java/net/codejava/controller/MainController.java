package net.codejava.controller;

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

@Controller
public class MainController {

	@Autowired
	private BCryptPasswordEncoder passwordEncoder;

	@RequestMapping("/default")
	public String defaultAfterLogin(HttpServletRequest request) {
		if (request.isUserInRole("ROLE_ADMIN")) {
			return "redirect:/users/";
		}

		return "redirect:/public/home";
	}

	@GetMapping("/index")
	public String signIn() {
		return "index.html";
	}
	@GetMapping("/news")
	public String news(){
		return "news.html";
	}

	@GetMapping("/contact")
	public String contact(){
		return  "contact.html";
	}

	@GetMapping("/usernameexist")
	public String usernameexist() {
		return "exist.html";
	}

	@GetMapping("/register")
	public String showForm(Model model) {
		User user = new User();
		model.addAttribute("user", user);

		// List<String> listProfession = Arrays.asList("Developer", "Tester",
		// "Architect");
		// model.addAttribute("listProfession", listProfession);

		List<String> listgender = Arrays.asList("Choose", "Male", "Female", "Others");
		model.addAttribute("listgender", listgender);

		List<String> liststates = Arrays.asList("Choose", "Andhra Pradesh", "Maharashtra", "Tamil Nadu", "Uttarakhand",
				"West Bengal");
		model.addAttribute("liststates", liststates);

		return "register_new";
	}

	@Autowired
	UserRepo repo;

	@Autowired
	UserService userservice;

	@PostMapping("/register")
	public String submitForm(@ModelAttribute("user") User user,
			@RequestParam("image") MultipartFile multipartFile) throws IOException {
		// String fileName = StringUtils.cleanPath(multipartFile.getOriginalFilename());

		if (!(userservice.userExists(user.getUsername()))) {
			String fileName = multipartFile.getOriginalFilename();

			// String Adhar=user.getUsername();

			user.setRole("ROLE_USER");

			user.setPassword(passwordEncoder.encode(user.getPassword()));

			user.setPhotos(fileName);
			User savedUser = repo.save(user);
			String uploadDir = "user-photos/" + savedUser.getUsername();

			FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);

			System.out.println(user);
			// repo.save(user);
			return "redirect:/index";
			// return "index.html";
		} else {
			return "redirect:/usernameexist";
		}
	}

	@PostMapping("/update")
	public String updateForm(@ModelAttribute("user") User user,
			@RequestParam("image") MultipartFile multipartFile, Principal principle, HttpSession session) throws IOException {
		
		try{
			String username = principle.getName();
			User olduser = userservice.getUser(username);
			
			if(!multipartFile.isEmpty())
			{
				String fileName = multipartFile.getOriginalFilename();
				user.setPhotos(fileName);
				String uploadDir = "user-photos/" + olduser.getUsername();

				FileUtils.deleteDirectory(new File(uploadDir));

				FileUploadUtil.saveFile(uploadDir, fileName, multipartFile);
			}
			else{

				user.setPhotos(olduser.getPhotos());
			}

			user.setRole("ROLE_USER");

			// user.setPassword(passwordEncoder.encode(user.getPassword()));
			
			User savedUser = repo.save(user);

			System.out.println(user);

			session.setAttribute("message", new Message("Your details are updated Successfully","success"));
			
	}
		catch(Exception e){
			session.setAttribute("message", new Message("Some error occured","danger"));
			e.printStackTrace();
		}
			return "redirect:/public/home/account";
	}

	@GetMapping("/error")
	public String errorpage() {
		return "error.html";
	}

	//send mail from contact us -------------------------------------------------------------
	@Autowired
	EmailService emailservice;

	@PostMapping("/sendemail")
	public String sendEmail(@RequestParam("fullname") String name, @RequestParam("email") String email,@RequestParam("message") String message ) {


		System.out.println("Email: " + email);
		

		String subject = "Message from "+name+" from Contact Us section";
		
		String to = "sharereport3@gmail.com";
		boolean f =this.emailservice.sendEmail(subject, message, to);

		if (f==true)
			return "redirect:/index";

		else
			return "redirect:/contact";
		
		
	}

}
