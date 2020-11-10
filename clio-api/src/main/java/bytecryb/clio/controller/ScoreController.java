package bytecryb.clio.controller;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import bytecryb.clio.model.CustomUser;
import bytecryb.clio.model.ResultScore;
import bytecryb.clio.model.Score;
import bytecryb.clio.repository.ScoreRepository;
import bytecryb.clio.repository.UserRepository;
import bytecryb.clio.util.JwtUtil;

@RestController
@RequestMapping("/api/scores")
public class ScoreController {

	@Autowired
	private ScoreRepository scoreRepo;

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private JwtUtil jwtUtil;

	// GET DAILY
	@GetMapping("/daily")
	public ResponseEntity<List<ResultScore>> getDailyTopScores(HttpServletRequest request) {
		
		List<Score> query = this.scoreRepo.findAll(Sort.by("day").descending());
		List<ResultScore> result = new ArrayList<ResultScore>();

		String jwtToken = extractJwtFromRequest(request);
		String username = jwtUtil.getUsernameFromToken(jwtToken);
		CustomUser curUser = this.userRepo.findByUsername(username);
		Score userScore = this.scoreRepo.findByUserId(curUser.getUserId());
		result.add(new ResultScore(0, username, userScore.getDay()));

		Iterator<Score> scoreIterator = query.iterator();

		int i = 1;
		while (scoreIterator.hasNext() && i < 10) {
			Score tmp = scoreIterator.next();
			long tmpUserId = tmp.getUserId();
			CustomUser user = this.userRepo.findById(tmpUserId);
			result.add(new ResultScore(i, user.getUsername(), tmp.getDay()));
			i++;
		}
		return ResponseEntity.ok().body(result);
	}

	// GET MONTHLY
	@GetMapping("/month")
	public ResponseEntity<List<ResultScore>> getMonthlyTopScores(HttpServletRequest request) {
		
		List<Score> query = this.scoreRepo.findAll(Sort.by("month").descending());
		List<ResultScore> result = new ArrayList<ResultScore>();

		String jwtToken = extractJwtFromRequest(request);
		String username = jwtUtil.getUsernameFromToken(jwtToken);
		CustomUser curUser = this.userRepo.findByUsername(username);
		Score userScore = this.scoreRepo.findByUserId(curUser.getUserId());
		result.add(new ResultScore(0, username, userScore.getMonth()));

		Iterator<Score> scoreIterator = query.iterator();

		int i = 1;
		while (scoreIterator.hasNext() && i < 10) {
			Score tmp = scoreIterator.next();
			long tmpUserId = tmp.getUserId();
			CustomUser user = this.userRepo.findById(tmpUserId);
			result.add(new ResultScore(i, user.getUsername(), tmp.getMonth()));
			i++;
		}
		return ResponseEntity.ok().body(result);
	}

	// GET ALL TIME
	@GetMapping("/alltime")
	public ResponseEntity<List<ResultScore>> getAllTimeTopScores(HttpServletRequest request) {
		
		List<Score> query = this.scoreRepo.findAll(Sort.by("score").descending());
		List<ResultScore> result = new ArrayList<ResultScore>();

		String jwtToken = extractJwtFromRequest(request);
		String username = jwtUtil.getUsernameFromToken(jwtToken);
		CustomUser curUser = this.userRepo.findByUsername(username);
		Score userScore = this.scoreRepo.findByUserId(curUser.getUserId());
		result.add(new ResultScore(0, username, userScore.getScore()));

		Iterator<Score> scoreIterator = query.iterator();

		int i = 1;
		while (scoreIterator.hasNext() && 1 < 10) {
			Score tmp = scoreIterator.next();
			long tmpUserId = tmp.getUserId();
			CustomUser user = this.userRepo.findById(tmpUserId);
			result.add(new ResultScore(i, user.getUsername(), tmp.getScore()));
			i++;
		}
		return ResponseEntity.ok().body(result);
	}

	// GET SCORE BY USER
	@GetMapping("/{user_id}")
	public ResponseEntity<ResultScore> getUserScore(@PathVariable(name = "user_id") Long userId) {

		// find all scores in descending order
		List<Score> allScores = this.scoreRepo.findAll(Sort.by("score").descending());

		ResultScore result = null;

		// iterate to find matching user, index to obtain user's score rank
		for (int i = 0; i < allScores.size(); i++) {
			Score currScore = allScores.get(i);
			long currUserId = currScore.getUserId();
			if (currUserId == userId) {
				CustomUser currUser = this.userRepo.findById(currUserId);
				result = new ResultScore(i, currUser.getUsername(), currScore.getScore());
			}
		}
		/*
		// get score of user w/ no rank
		Score query = this.scoreRepo.findByUserId(userId);
		long currUserId = query.getUserId();
		CustomUser currUser = this.userRepo.findById(currUserId);

		ResultScore result = new ResultScore(0, currUser.getUsername(), query.getScore());
		*/
		return ResponseEntity.ok().body(result);
	}

	private String extractJwtFromRequest(HttpServletRequest request) {
		String bearerToken = request.getHeader("Authorization");
		if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
			return bearerToken.substring(7, bearerToken.length());
		}
		return null;
	}


}
