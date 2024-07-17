package com.oneasad.journalApp.controller;

import com.oneasad.journalApp.entity.JournalEntry;
import com.oneasad.journalApp.entity.User;
import com.oneasad.journalApp.service.JournalEntryService;
import com.oneasad.journalApp.service.UserService;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/journal")
public class JournalEntryController {

    @Autowired
    private JournalEntryService journalEntryService;
    @Autowired
    private UserService userService;

    public User authenticateUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userName = authentication.getName();
        return userService.findUserByUsername(userName);
    }
    @GetMapping
    public ResponseEntity<List<JournalEntry>> getAllJournalEntries() {
        User user = authenticateUser();
        List<JournalEntry> journalList =  user.getJournalEntries();
        if(journalList != null && !journalList.isEmpty())
            return new ResponseEntity<>(journalList, HttpStatus.OK);
        else
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping
    public ResponseEntity<JournalEntry> createJournalEntry(@RequestBody JournalEntry journalEntry) {
        try{
            User user = authenticateUser();
            journalEntryService.saveEntry(journalEntry, user.getUserName());
            return new ResponseEntity<>(journalEntry, HttpStatus.CREATED);
        }catch(Exception e){
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("id/{objectId}")
    public ResponseEntity<?> getJournalEntryById(@PathVariable ObjectId objectId) {
        User user = authenticateUser();
        List<JournalEntry> collect = user.getJournalEntries()
                .stream()
                .filter(x -> x.getId().equals(objectId))
                .toList();
        if(!collect.isEmpty()){
            Optional<JournalEntry> journalEntry = journalEntryService.getJournalEntryById(objectId);
            return new ResponseEntity<>(journalEntry, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping("id/{objectId}")
    public ResponseEntity<?> deleteJournalEntryById(@PathVariable ObjectId objectId) {
        User user = authenticateUser();
        Optional<JournalEntry> journalEntry = journalEntryService.getJournalEntryById(objectId);
        boolean removed = journalEntryService.deleteById(objectId, user.getUserName());
        return removed ? new ResponseEntity<>(HttpStatus.ACCEPTED) : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("id/{objectId}")
    public ResponseEntity<?> updateJournalEntry(
            @PathVariable ObjectId objectId,
            @RequestBody JournalEntry newEntry
    ) {
        User user = authenticateUser();
        List<JournalEntry> collect = user.getJournalEntries()
                .stream()
                .filter(x -> x.getId().equals(objectId))
                .toList();
        if(!collect.isEmpty()){
            JournalEntry oldEntry = journalEntryService.getJournalEntryById(objectId).get();
            oldEntry.setTitle(newEntry.getTitle());
            oldEntry.setContent(newEntry.getContent());
            journalEntryService.saveEntry(oldEntry);
            return new ResponseEntity<>(oldEntry, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
