package com.oneasad.journalApp.service;

import com.oneasad.journalApp.entity.JournalEntry;
import com.oneasad.journalApp.entity.User;
import com.oneasad.journalApp.repository.JournalEntryRepository;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class JournalEntryService {

    @Autowired
    private JournalEntryRepository journalEntryRepository;
    @Autowired
    private UserService userService;

    @Transactional
    public void saveEntry(JournalEntry journalEntry, String userName) {
        User user = userService.findUserByUsername(userName);
        JournalEntry savedEntry = journalEntryRepository.save(journalEntry);
        user.getJournalEntries().add(savedEntry);
        userService.saveUser(user);
    }

    public void saveEntry(JournalEntry journalEntry) {
        journalEntryRepository.save(journalEntry);
    }

    public Optional<JournalEntry> getJournalEntryById(ObjectId id) {
        return journalEntryRepository.findById(id);
    }

    @Transactional
    public boolean deleteById(ObjectId id, String userName){
        boolean removed = false;
        try {
            User user = userService.findUserByUsername(userName);
            removed = user.getJournalEntries().removeIf(journalEntry -> journalEntry.getId().equals(id));
            if (removed){
                userService.saveUser(user);
                journalEntryRepository.deleteById(id);
            }
        } catch (Exception e){
            throw new RuntimeException("Error deleting journal entry: " + e.getMessage());
        }
        return removed;
    }


}
