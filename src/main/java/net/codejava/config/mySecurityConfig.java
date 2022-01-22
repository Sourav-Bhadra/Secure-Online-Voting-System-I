package net.codejava.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import net.codejava.service.CustomUserDetailsService;



@Configuration

public class mySecurityConfig extends WebSecurityConfigurerAdapter{

	@Override
	protected void configure(HttpSecurity http) throws Exception {
		
		http
			.csrf().disable()
			.authorizeRequests()
			.antMatchers("/index","/register","/js/**","/css/**","/fonts/**","/images/**","/inc/**","/scss/**","/plugin/**","/templates/register","/SweetAlert/**","/Register","/error","/forgotpassword/**").permitAll()
			.antMatchers("/public/**").hasRole("USER")//only people with role user can access these pages
			.antMatchers("/users/**").hasRole("ADMIN")
			//.antMatchers("/public/**").permitAll() doesnt authenticate these pages
			.anyRequest()
			.authenticated()
			.and()
			//.httpBasic();
			.formLogin()
			.loginPage("/index")
			.loginProcessingUrl("/dologin")
			.defaultSuccessUrl("/default",true)
			.and()
			.logout().logoutRequestMatcher(new AntPathRequestMatcher("/logout")).logoutSuccessUrl("/index");
			
	}
	
	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Override
	protected void configure(AuthenticationManagerBuilder auth) throws Exception {
	
		auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
		//auth.inMemoryAuthentication().withUser("gunjan").password(this.passwordEncoder().encode("1234")).roles("ADMIN");
		//auth.inMemoryAuthentication().withUser("surya").password(this.passwordEncoder().encode("1234")).roles("USER");
	}
	
	@Bean
	public BCryptPasswordEncoder passwordEncoder() {
		//for encrypting the password using Bcryt object
		return new BCryptPasswordEncoder(10);
	}

}
