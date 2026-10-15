package com.austinbordeaux.irrigation.service;

import com.austinbordeaux.irrigation.model.Feedback;
import com.austinbordeaux.irrigation.repository.FeedbackRepository;
import com.austinbordeaux.irrigation.web.form.ContactForm;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class FeedbackService {

    private final FeedbackRepository feedbackRepository;

    public FeedbackService(FeedbackRepository feedbackRepository) {
        this.feedbackRepository = feedbackRepository;
    }

    public Feedback submit(ContactForm form, String username) {
        Feedback feedback = new Feedback(form.getName(), form.getEmail(), form.getSubject(),
                form.getMessage(), username);
        return feedbackRepository.save(feedback);
    }

    @Transactional(readOnly = true)
    public List<Feedback> all() {
        return feedbackRepository.findAllByOrderByCreatedAtDesc();
    }
}