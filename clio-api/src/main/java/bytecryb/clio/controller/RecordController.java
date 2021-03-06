package bytecryb.clio.controller;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.transaction.annotation.Transactional;

import bytecryb.clio.repository.JobRepository;
import bytecryb.clio.repository.PDFRepository;
import bytecryb.clio.repository.RecordRepository;
import bytecryb.clio.service.PDFService;
import bytecryb.clio.exception.ResourceNotFoundException;
import bytecryb.clio.model.Job;
import bytecryb.clio.model.PDF;
import bytecryb.clio.model.Record;

@RestController
@RequestMapping("/api/v1")
public class RecordController {
    @Autowired
    private RecordRepository recordRepo;

    @Autowired
    private PDFService pdfService;

    @Autowired
    private PDFRepository pdfRepo;

    @Autowired
    private JobRepository jobRepo;

    // get all records
    @GetMapping("/records/all")
    public List<Record> getAll() {
        return this.recordRepo.findAll();
    }

    @GetMapping("/records/{id}")
    public ResponseEntity<Record> getById(@PathVariable Long id) throws ResourceNotFoundException {
        Record result = this.recordRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Record " + id + " was not found"));
        return ResponseEntity.ok().body(result);
    }

    // get first incomplete record pdf link
    @GetMapping("/records/pop")
    @Transactional
    public ResponseEntity<Record> popByJobId(@RequestParam(name = "job_id") Long jobId) {
        // get a list of records with matching job id
        List<Record> filteredRecords = this.recordRepo.findByJobId(jobId);

        Record result = null;

        for (Record r : filteredRecords) {
            // if record is incomplete (status == 0)
            if (!r.isSubmitted()) {
                // change status to in progress
                r.setCheckedOut(true);

                result = r;
            }
        }

        // No incomplete record available, invalid result record returned
        return ResponseEntity.ok().body(result);
    }

    // get first incomplete record pdf link
    @GetMapping("/records/job/{job_id}")
    public ResponseEntity<List<Record>> getByJobId(@PathVariable Long jobId) {
        // get a list of records with matching job id
        List<Record> filteredRecords = this.recordRepo.findByJobId(jobId);
        return ResponseEntity.ok().body(filteredRecords);
    }

    @PostMapping("/records")
    public ResponseEntity<String> push(@RequestBody Record input) {
        Record result = this.recordRepo.save(input);
        return ResponseEntity.ok().body(new String("Successfully Created Record: " + result.getId()));
    }

    @PostMapping("/jobs/record")
    public ResponseEntity<List<Record>> pushRecords(@RequestParam(name = "job_id") Long id,
            @RequestParam(name = "files") MultipartFile[] files) throws Exception {
        // check job exits
        Job job = this.jobRepo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job " + id + " does not exist!"));

        // example for path
        Record record = this.recordRepo.findFirstByJobId(id);
        PDF example = null;
        UUID folder = null;
        if (record == null) {
            folder = UUID.randomUUID();
        } else {
            example = this.pdfRepo.findById(record.getPdfId()).orElse(null);
            System.out.println(example.toString());
            String path = example.getPath();
            System.out.println(path);
            String[] pathDivided = path.split("/");
            System.out.println(pathDivided[pathDivided.length - 2]);
            folder = UUID.fromString(pathDivided[pathDivided.length - 2]);
        }

        List<PDF> filesUploaded = new ArrayList<>();
        List<Record> records = new ArrayList<>();

        for (MultipartFile file : files) {
            System.out.println(file.toString());
            filesUploaded.add(this.pdfService.uploadToLocal(file, folder));
        }

        for (PDF file : filesUploaded) {
            records.add(this.recordRepo.save(new Record(id, file.getId(), false, false, false, "{}")));
            job.setSize(job.getSize() + 1);
        }

        this.jobRepo.save(job);

        return ResponseEntity.ok(records);
    }

    @PutMapping("/records")
    @Transactional
    public ResponseEntity<Record> update(@RequestBody Record input) throws ResourceNotFoundException {
        Record result = this.recordRepo.findById(input.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Record not found for id: " + input.getId()));

        result.setCheckedOut(input.isCheckedOut());
        result.setSubmitted(input.isSubmitted());
        result.setApproved(input.isApproved());
        result.setJson(input.getJson());
        final Record update = this.recordRepo.save(result);
        return ResponseEntity.ok().body(update);
    }
}
