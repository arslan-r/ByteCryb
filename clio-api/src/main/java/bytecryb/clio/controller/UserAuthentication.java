package bytecryb.clio.controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import bytecryb.clio.model.AuthenticationRequest;
import bytecryb.clio.model.AuthenticationResponse;
import bytecryb.clio.model.CustomUser;
import bytecryb.clio.model.ResultUser;
import bytecryb.clio.model.Role;
import bytecryb.clio.model.Score;
import bytecryb.clio.repository.RoleRepository;
import bytecryb.clio.repository.UserRepository;
import bytecryb.clio.service.CustomUserDetailsService;
import bytecryb.clio.service.ScoreService;
import bytecryb.clio.util.JwtUtil;


@RestController
@RequestMapping("/auth")
public class UserAuthentication {
    @Autowired
	private UserRepository userRepo;
	
	@Autowired
	private RoleRepository roleRepo;

	@Autowired
	private AuthenticationManager authenticationManager;

	@Autowired
	private CustomUserDetailsService userDetailsService;

	@Autowired
	private ScoreService scoreService;

	@Autowired
	private JwtUtil jwtTokenUtil;
    
	// {username, password}
	@RequestMapping(value = "/login", method = RequestMethod.POST)
	public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest)
			throws Exception {
		try {
			authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
					authenticationRequest.getUsername(), authenticationRequest.getPassword()));
		} catch (DisabledException e) {
			throw new Exception("USER_DISABLED", e);
		} catch (BadCredentialsException e) {
			throw new Exception("INVALID_CREDENTIALS", e);
		}

		final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
		final String token = jwtTokenUtil.generateToken(userDetails);
		return ResponseEntity.ok(new AuthenticationResponse(token));
	}
	
	// {username, email, password}
	@RequestMapping(value = "/signup", method = RequestMethod.POST)
	public ResponseEntity<?> saveUser(@RequestBody CustomUser user) throws Exception {
        if(userRepo.existsByUsername(user.getUsername())) {
            throw new Exception("USERNAME TAKEN");
        }
        if(userRepo.existsByEmail(user.getEmail())) {
        	throw new Exception("EMAIL ALREADY IN USE");
		}
		Role role = roleRepo.findByRoleName("rookie");
		user.setRole(role);
		CustomUser savedUser = userDetailsService.save(user);
		Score defaultScore = new Score();
		defaultScore.setUserId(savedUser.getUserId());

		//DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
		Date today = new Date();
		
		defaultScore.setDate(today);//formatter.format(today)
		defaultScore.setScore(0);
		scoreService.save(defaultScore);
		ResultUser resUser = new ResultUser(savedUser.getUserId(), savedUser.getUsername(), savedUser.getEmail(), "rookie");
		final UserDetails userDetails = userDetailsService.loadUserByUsername(resUser.getUsername());
		final String token = jwtTokenUtil.generateToken(userDetails);
		resUser.setAuthToken(token);
		return ResponseEntity.ok(resUser);
	}

}