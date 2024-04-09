package com.smart.controller;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.smart.dao.UserRepository;
import com.smart.entities.User;
import com.smart.helper.Message;
import com.smart.service.EmailService;

import jakarta.servlet.http.HttpSession;

@Controller
public class ForgotController {

	Random random = new Random(1000);

	@Autowired
	private EmailService emailService;

	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private BCryptPasswordEncoder bCryptPasswordEncoder;

	// email id form open handler
	@RequestMapping("/forgot")
	public String openEmailForm() {
		return "forgot_email_form";
	}

	@PostMapping("/send-otp")
	public String sendOTP(@RequestParam("email") String email, HttpSession session) {

		System.out.println("EMAIL " + email);

		// generating otp of 6 digit
		int otp = random.nextInt(999999);
		System.out.println("OTP " + otp);

		// code for send otp to email
		String subject = "OTP from SCM";
		String message = "" + "<div style='border:1px solid #dddee1; padding:20px'>" + "<h2>" + "OTP is " + "<b>" + otp
				+ "</b>" + "</h2>" + "</div>";
		String to = email;

		boolean flag = this.emailService.sendEmail(subject, message, to);

		if (flag) {
			session.setAttribute("myotp", otp);
			session.setAttribute("email", email);

			return "verify_otp";

		} else {
			session.setAttribute("message", new Message("Check your email!", "danger"));
			return "forgot_email_form";
		}

	}

	// verify otp
	@PostMapping("/verify-otp")
	public String verifyOtp(@RequestParam("otp") int otp, HttpSession session) {
		int myOtp = (int) session.getAttribute("myotp");
		String email = (String) session.getAttribute("email");

		if (myOtp == otp) {
			// password change form
			User user = this.userRepository.getUserByUserName(email);

			if (user == null) {
				// send error message
				session.setAttribute("message", new Message("User does not exists with this email!", "danger"));
				return "forgot_email_form";
			} else {
				// send change password form
				

			}

			return "password_change_form";

		} else {
			session.setAttribute("message", new Message("The OTP you entered is invalid. Please enter the correct OTP!", "danger"));

			return "verify_otp";
		}
	}
	
	//change password
	@PostMapping("/change-password")
	public String changePassword(@RequestParam("newpassword") String newpassword, HttpSession session) {
		String email = (String) session.getAttribute("email");
		
		User user = this.userRepository.getUserByUserName(email);
		user.setPassword(this.bCryptPasswordEncoder.encode(newpassword));
		
		this.userRepository.save(user);
		
		return "redirect:/signin?change=Password Changed Succesfully...";
		
	}

}
