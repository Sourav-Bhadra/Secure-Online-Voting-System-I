package net.codejava.controller;

import java.io.File;
import java.security.Principal;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.ui.Model;
import net.codejava.model.User;
import net.codejava.service.UserService;

@Controller
@RequestMapping("/users")
public class UserSecurityController {
	
	@Autowired
	UserService userService;

	//all users
	@GetMapping("/")
	public String getUsers(Principal principle,Model model){

		String username = principle.getName();
		User user = userService.getUser(username);
		model.addAttribute("currentUser", user);
		List<User> users = userService.getAllUsers();
		model.addAttribute("users", users);
		return "users.html";
	}

	
	//return single user
	@GetMapping("/{userName}")
	public User getUser(@PathVariable("userName") String userName) {
		return this.userService.getUser(userName);
	}
	
	// when update photo delete previous photo from directory
	@GetMapping("/removeFile/{username}/{fileName}")
	public String removeFileHandler (@PathVariable("username") String username, @PathVariable("fileName") String fileName, Model model) {		
		//ModelAndView mav = new ModelAndView("redirect:/image-upload/employees");
		String path = null;
		File file =null;

		try{
			path = "user-photos/" + username;
			System.out.println(path);
			file=new File(path);
			if(file.exists())
			{
				boolean status = userService.deleteUser(username,fileName,path);
				List<User> users = userService.getAllUsers();
				model.addAttribute("users", users);
				return "redirect:/users/";
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/users/";
	}
		
}
