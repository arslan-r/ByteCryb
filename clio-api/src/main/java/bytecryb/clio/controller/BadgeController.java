package bytecryb.clio.model;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;

import bytecryb.clio.repository.BadgeRepository;
import bytecryb.clio.repository.Badge;

import org.springframework.web.bind.annotation.RequestMapping;

@RestController
@RequestMapping("/api/badges")
public class BadgeController {

    @Autowired
    private BadgeRepository badgeRepo;
    //get badges
    @GetMapping("/all") 
    public List<Badge> getAllBadge() {
        return this.badgeRepo.findAll();
    }

    //get badge by score
    @GetMapping("/{score}")
    public ResponseEntity<Badge> getBadgeByScore(@PathVariable(value = "score") Integer score)
        throws ResourceNotFoundException {
            Badge badge = badgeRepo.findByScore(score)
                .orElseThrow(() -> new ResourceNotFoundException("Badge not found for this score: " + badge));       
        
        return ResponseEntity.ok().body(badge);
        }

}